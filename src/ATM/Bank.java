package ATM;

import java.util.Map;

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
    public static final int ERROR_BALANCE_OVERFLOW = 7;         //余额溢出
    public static final int ERROR_NOT_IN_DEBUG = -2;            //不处于调试模式



    private  boolean isDebug = false;//是否处于调试模式的状态位，true则处于调试模式
    public void setDebug(boolean debug) {
        isDebug = debug;
    }
    //用户集合
    private UserModel userModel;

    //当前用户
    User currentUser=null;

    public Bank(){
        userModel=new UserModel();
    }

    //调试的方法
    public int getTheNumberOfUsers() {
        return isDebug?userModel.getUserMap().size():ERROR_NOT_IN_DEBUG;
    }

    public int listAllUsers(){
        if(!isDebug){
            return ERROR_NOT_IN_DEBUG;
        }
        for (Map.Entry<String, User> entry : userModel.getUserMap().entrySet()) {
            String key = entry.getKey();
            User value = entry.getValue();
            System.out.printf("用户名: %-30s   uid: %-20d%n", key , value.getUid());
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
                //找到了该用户，匹配密码
                if (userModel.getUserMap().get(name).matchPassword(password)) {
                    currentUser = userModel.getUserMap().get(name);
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
                currentUser.changePasswordInModel(newPassword);
                return SUCCESS;//修改成功
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
            if(money>0) {
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
        //System.out.println("存钱失败，用户未登录");
        return ERROR_USER_NOT_LOGIN;
    }

    //取钱
    public int withdrawal(long money){
        if(currentUser!=null){
            if(money>0) {
                if(currentUser.getBalance()>=money) {
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
        //System.out.println("取钱失败，用户未登录");
        return ERROR_USER_NOT_LOGIN;
    }

    //查询余额
    public long query() {
        if (currentUser != null) {

            return currentUser.getBalance();

        }

        //System.out.println("查询失败，用户未登录");
        return -1;//这里不能用ERROR_USER_NOT_LOGIN,会和正常余额冲突
    }



}
