package ATM;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//这是MVC架构中的Control控制层，有所有操作数据的方法逻辑
public class Bank {
    //定义常量错误码
    public static final int SUCCESS = 0;                        // 成功
    public static final int ERROR_UNKNOWN = -1;                 // 未知错误
    public static final int ERROR_PASSWORD_WRONG = 1;           // 密码错误
    public static final int ERROR_USERNAME_EXIST = 2;           // 用户名已存在
    public static final int ERROR_USER_NOT_EXIST = 3;           // 用户不存在
    public static final int ERROR_USER_NOT_LOGIN = 4;           // 用户未登录
    public static final int ERROR_AMOUNT_INVALID = 5;           // 金额非法
    public static final int ERROR_BALANCE_INSUFFICIENT = 6;     // 余额不足
    public static final int ERROR_BALANCE_OVERFLOW = 7;         // 余额溢出
    public static final int ERROR_BALANCE_NOT_ZERO = 8;         // 余额不为0
    public static final int ERROR_USER_DELETED = 9;             // 用户状态处于删除
    public static final int ERROR_NOT_IN_DEBUG = -2;            // 不处于调试模式

    private  boolean isDebug = false;//是否处于调试模式的状态位，true则处于调试模式
    public void setDebug(boolean debug) {
        isDebug = debug;
    }
    //用户集合
    private UserModel userModel;

    //当前用户
    User currentUser=null;

    private ScheduledExecutorService scheduler;

    private void startInterestTask(){

        scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {

            for(User user : userModel.getUserMap().values()){
                //对每个user的操作是互斥的，如果在对一个user加利息，那么就不能对这个user存钱或取钱
                synchronized (user){
                    if(user.getState()==User.USER_STATE_DELETED){
                        continue;//用户已删除
                    }
                    long oldBalance = user.getBalance();

                    long interest = oldBalance * 5 / 100;//5%的利息

                    long tempBalance;
                    try {
                        // 先安全地相加
                        tempBalance = Math.addExact(oldBalance, interest);
                        // 如果走到这里，说明没有溢出
                        //System.out.println("没有溢出，tempBalance = "+tempBalance);
                    } catch (ArithmeticException e) {
                        user.setBalance(User.MAX_BALANCE);
                        //System.out.println("金额数值过大，超出系统支持范围。加利息失败，余额将维持最大值");
                        continue;
                    }

                    user.setBalance(tempBalance);
                }
            }

            //System.out.println("已为所有用户增加5%利息");

        },10,10, TimeUnit.SECONDS);//10秒后第一次执行，之后每10秒执行一次
    }

    public void shutdown(){
        scheduler.shutdown();
        try {

            scheduler.awaitTermination(5, TimeUnit.SECONDS);//最多等5秒完成任务

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public Bank(){
        userModel=new UserModel();

        startInterestTask();
    }

    //调试的方法
    public long getTheNumberOfUsers() {
        return isDebug?userModel.getUserMap().size():ERROR_NOT_IN_DEBUG;
    }

    public long getTheNumberOfNormalUsers(){
        return isDebug?userModel.getTheNumberOfNormalUser():ERROR_NOT_IN_DEBUG;
    }

    public int listAllUsers(){
        if(!isDebug){
            return ERROR_NOT_IN_DEBUG;
        }
        for (Map.Entry<String, User> entry : userModel.getUserMap().entrySet()) {
            String key = entry.getKey();
            User value = entry.getValue();
            System.out.printf("用户名: %-30s   uid: %-20d  balance: %-20d   state: %-10s%n",
                    key, value.getUid(), value.getBalance(),value.getState()==User.USER_STATE_NORMAL?"normal":"deleted");
        }

        return SUCCESS;
    }

    //创建新用户的方法，成功则返回0,失败返回各种错误码
    public int createNewUser(String name,String password){
        //先判空
        if(name!=null && password!=null) {
            if(findUserByName(name)==null){
                userModel.getUserMap().put(name,new User(name, password));
                //System.out.println("创建成功");
                synchronized (userModel){
                    userModel.setTheNumberOfNormalUser(userModel.getTheNumberOfNormalUser()+1);
                }
                return SUCCESS;
            }

            //System.out.println("创建失败，该用户名已被占用");
            return ERROR_USERNAME_EXIST;


        }

        //System.out.println("未知错误，创建失败");
        return ERROR_UNKNOWN;

    }


    //登录方法,成功则返回0,失败返回各种错误码
    public int login(String name,String password){

        if(name!=null && password!=null) {
            if(findUserByName(name)!=null){
                //找到了该用户
                User userToLogin=userModel.getUserMap().get(name);
                //先看这个用户是否已经被删除
                if(userToLogin.getState()==User.USER_STATE_DELETED){
                    return ERROR_USER_DELETED;//用户已删除
                }
                //没被删除，再匹配密码
                if (userToLogin.matchPassword(password)) {
                    currentUser = userToLogin;
                    //System.out.println("登录成功");
                    return SUCCESS;

                }
                //System.out.println("密码错误，登录失败");
                return ERROR_PASSWORD_WRONG;


            }

            //System.out.println("用户不存在，登录失败");
            return ERROR_USER_NOT_EXIST;

        }


        //System.out.println("未知错误，登录失败");
        return ERROR_UNKNOWN;


    }

    public int changePassword(String newPassword){
        if(newPassword!=null) {
            if (currentUser != null) {
                synchronized (currentUser) {
                    //先看这个用户是否已经被删除
                    if (currentUser.getState() == User.USER_STATE_DELETED) {
                        return ERROR_USER_DELETED;//用户已删除
                    }
                    currentUser.changePasswordInModel(newPassword);
                    return SUCCESS;//修改成功
                }
            }
            return ERROR_USER_NOT_LOGIN;//修改失败,未登录
        }
        return ERROR_UNKNOWN;//修改失败，未知错误

    }


    //根据用户名查找用户是否存在，存在则返回用户对象，不存在则返回空指针
    public User findUserByName(String name){
            return userModel.getUserMap().get(name);// O(1) 复杂度
    }

    //存钱，传入的参数以分为单位，把小数化成整数是view层的职责
    public int deposit(long money){
        if(currentUser!=null){
            //先看这个用户是否已经被删除
            if(currentUser.getState()==User.USER_STATE_DELETED){
                return ERROR_USER_DELETED;//用户已删除
            }
            //对每个user的操作是互斥的，如果在对一个user存钱，那么就不能对这个user加利息或取钱
            synchronized(currentUser) {
                if (money > 0) {
                    //需要判断本次存款是否会让余额溢出
                    long tempBalance;
                    try {
                        // 先安全地相加
                        tempBalance = Math.addExact(currentUser.getBalance(), money);
                        // 如果走到这里，说明没有溢出
                        //System.out.println("没有溢出，tempBalance = "+tempBalance);
                    } catch (ArithmeticException e) {
                        //System.out.println("金额数值过大，超出系统支持范围。本次操作失败");
                        return ERROR_BALANCE_OVERFLOW;
                    }

                    currentUser.setBalance(tempBalance);
                    //System.out.println("存钱成功");
                    return SUCCESS;
                }
                //System.out.println("存钱失败,存入金额非法");
                return ERROR_AMOUNT_INVALID;
            }
        }
        //System.out.println("存钱失败，用户未登录");
        return ERROR_USER_NOT_LOGIN;
    }

    //取钱
    public int withdrawal(long money){
        if(currentUser!=null){
            //先看这个用户是否已经被删除
            if(currentUser.getState()==User.USER_STATE_DELETED){
                return ERROR_USER_DELETED;//用户已删除
            }
            //对每个user的操作是互斥的，如果在对一个user取钱，那么就不能对这个user存钱或加利息
            synchronized(currentUser) {
                if (money > 0) {
                    if (currentUser.getBalance() >= money) {
                        currentUser.setBalance(currentUser.getBalance() - money);
                        //System.out.println("取钱成功");
                        return SUCCESS;
                    }
                    //System.out.println("取钱失败，余额不足");
                    return ERROR_BALANCE_INSUFFICIENT;

                }
                //System.out.println("取钱失败。取出金额非法");
                return ERROR_AMOUNT_INVALID;
            }
        }
        //System.out.println("取钱失败，用户未登录");
        return ERROR_USER_NOT_LOGIN;
    }

    //查询余额
    public long query() {
        if (currentUser != null) {
            //用户已经删除也允许查询，因为删除的用户余额一定为0
            return currentUser.getBalance();

        }

        //System.out.println("查询失败，用户未登录");
        return -1;//这里不能用ERROR_USER_NOT_LOGIN,会和正常余额冲突
    }

    //修改用户名
    public int changeUserName(String newUserName){
        if(newUserName!=null) {
            if (currentUser != null) {
                synchronized (currentUser) {
                    //先看这个用户是否已经被删除
                    if (currentUser.getState() == User.USER_STATE_DELETED) {
                        return ERROR_USER_DELETED;//用户已删除
                    }
                    if (findUserByName(newUserName) == null) {
                        //因为系统把用户名作为键索引，要先改map中的键
                        User value = userModel.getUserMap().remove(currentUser.getName());
                        userModel.getUserMap().put(newUserName, value);
                        value.changeUserNameInModel(newUserName);

                        return SUCCESS;//修改成功
                    }
                    //System.out.println("修改失败，该用户名已被占用");
                    return ERROR_USERNAME_EXIST;
                }
            }
            return ERROR_USER_NOT_LOGIN;//修改失败,未登录
        }
        return ERROR_UNKNOWN;//修改失败，未知错误

    }

    //删除用户（逻辑删除）
    public int deleteUser(){
        if (currentUser != null) {
            //先看这个用户是否已经被删除
            if(currentUser.getState()==User.USER_STATE_DELETED){
                return ERROR_USER_DELETED;//用户已删除
            }
            synchronized (currentUser) {
                if(currentUser.getBalance() != 0){
                    return ERROR_BALANCE_NOT_ZERO;//删除失败，余额不为0
                }
                currentUser.setState(User.USER_STATE_DELETED);
                //为了不让被删除的用户占用用户名，还应该把map里的用户名键标记成已删除
                User deletedUser = userModel.getUserMap().remove(currentUser.getName());
                userModel.getUserMap().put("__deleted__"+deletedUser.getName(),deletedUser);
                synchronized (userModel){
                    userModel.setTheNumberOfNormalUser(userModel.getTheNumberOfNormalUser()-1);
                }
                return SUCCESS;//删除成功
            }
        }
        return ERROR_USER_NOT_LOGIN;//删除失败,未登录

    }


    //AES 要求密钥必须是 16 / 24 / 32 字节
    private static final String KEY =
            "1234567890123456";//定义密钥，16字节 = AES-128，此处为硬编码，实际开发不应这么做，反编译后会被看到

    //保存model数据的方法，序列化 + AES加密
    public void saveToFile() {
        try {

            SecretKeySpec key =
                    new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");//显式指定UTF-8编码，与平台无关。此外使用AES算法
            //加密算法为对称加密AES，模式为CBC，填充方式为PKCS5Padding
            Cipher cipher =
                    Cipher.getInstance("AES/CBC/PKCS5Padding");//CBC比ECB安全，但需要初始化向量IV

            //生成IV
            byte[] iv = new byte[16];

            SecureRandom random = new SecureRandom();

            random.nextBytes(iv);

            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv);//IvParameterSpec 就是一个 IV 的包装类，作用是让 Cipher 能识别 IV
            //初始化
            cipher.init(Cipher.ENCRYPT_MODE, key,ivSpec);

            FileOutputStream fos =
                    new FileOutputStream("users.dat");

            CipherOutputStream cos =
                    new CipherOutputStream(fos, cipher);

            ObjectOutputStream oos =
                    new ObjectOutputStream(cos);

            //必须把 IV 写入文件,因为解密也需要同一个 IV,但 IV 不需要保密。
            //文件格式：[16字节IV][密文]
            fos.write(iv);
            //存文件前保存当前uidAllocator
            User.saveUidToModel(userModel);
            oos.writeObject(userModel);

            oos.close();//关闭最外层流，会自动级联关闭所有底层流

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取model数据的方法，AES解密 + 反序列化
    public void loadFromFile() {

        try {

            SecretKeySpec key =
                    new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");//显式指定UTF-8编码，与平台无关。此外使用AES算法

            Cipher cipher =
                    Cipher.getInstance("AES/CBC/PKCS5Padding");//CBC比ECB安全，但需要初始化向量IV

            FileInputStream fis =
                    new FileInputStream("users.dat");

            //先读取出IV
            byte[] iv = new byte[16];

            fis.read(iv);

            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv);

            //有了ivSpec后再初始化
            cipher.init(Cipher.DECRYPT_MODE, key,ivSpec);

            CipherInputStream cis =
                    new CipherInputStream(fis, cipher);

            ObjectInputStream ois =
                    new ObjectInputStream(cis);

            userModel = (UserModel) ois.readObject();

            ois.close();//关闭最外层流，会自动级联关闭所有底层流

            //读取文件后恢复uidAllocator
            User.loadUidFromModel(userModel);

        } catch (FileNotFoundException e) {

            System.out.println("首次运行，无存档");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
