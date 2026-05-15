package ATM;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//一个用户的信息
class User implements Serializable {
    //定义常量状态
    public static final int USER_STATE_NORMAL=0;     //正常
    public static final int USER_STATE_DELETED=1;    //已被删除

    //用户状态
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    //余额，初始为0,单位为分
    private long balance=0;

    //余额最大值
    static final long MAX_BALANCE=Long.MAX_VALUE;

    //存储密码的哈希值，用SHA-256加密算法
    private String passwordHashCode;

    //想要保证密码安全就要加盐
    private final byte[] salt;

    //用户名
    private String name;

    //分配uid的变量
    private static long uidAllocator=0;//由于static字段无法被序列化写入文件，故不在此处将其初始化为0，而是从UserModel读取

    //从文件加载后，恢复 uid 计数器
    public static void loadUidFromModel(UserModel model) {
        User.uidAllocator = model.getUidAllocator();
    }

    //保存文件前，把当前 uid 存进 model
    public static void saveUidToModel(UserModel model) {
        model.setUidAllocator(User.uidAllocator);
    }

    //用户uid，一个用户的历史永久身份，注销后也永远不能复用
    private final long uid;



    //唯一的构造方法
    public User(String n,String password){
        name=n;
        state = USER_STATE_NORMAL;
        salt = generateSalt();

        //根据密码生成哈希值
        changePasswordInModel(password);

        //如果是多线程创建用户，这个uid分配是不安全的
        //但是本程序只有两个线程，一个操作，一个只加利息，因此没关系
        uid=uidAllocator;
        uidAllocator+=1;
    }

    //用于改密码
    public void changePasswordInModel(String newPassword){
        this.passwordHashCode=hashPassword(newPassword,salt);
    }

    //生成salt
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    //根据密码生成哈希值
    private String hashPassword(String passwordInput, byte[] salt){
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    passwordInput.toCharArray(),
                    salt,
                    65536,     // 迭代次数
                    256        // 输出位数
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //用于匹配密码，匹配成功返回true，失败返回false
    public boolean matchPassword(String passwordInput){
        return hashPassword(passwordInput,salt).equals(passwordHashCode);
    }


    //getter和setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public long getBalance() {return balance;}

    public void setBalance(long balance) {
        this.balance = balance;
    }




    public long getUid() {
        return uid;
    }

    @Serial
    private static final long serialVersionUID = 1L;//序列化版本号,不同的类之间的序列化版本号不必相同
}


//用户信息的集合
public class UserModel implements Serializable{

    // Key 是用户名 (String)，Value 是用户对象 (User)
    private final Map<String, User> userMap = new ConcurrentHashMap<>();//使用多线程安全的ConcurrentHashMap

    //userMap的getter和setter
    public Map<String, User> getUserMap() {
        return userMap;
    }


    @Serial
    private static final long serialVersionUID = 1L;//序列化版本号

    private long theNumberOfNormalUser=0;

    public long getTheNumberOfNormalUser() {
        return theNumberOfNormalUser;
    }

    public void setTheNumberOfNormalUser(long theNumberOfNormalUser) {
        this.theNumberOfNormalUser = theNumberOfNormalUser;
    }

    private long uidAllocator = 0; // 这个uid分配器会被序列化保存

    public long getUidAllocator() {
        return uidAllocator;
    }

    public void setUidAllocator(long uidAllocator) {
        this.uidAllocator = uidAllocator;
    }


}
