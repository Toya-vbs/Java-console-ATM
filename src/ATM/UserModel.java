package ATM;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//一个用户的信息
class User {
    //余额，初始为0,单位为分
    private long balance=0;

    //存储密码的哈希值，用SHA-256加密算法
    private String passwordHashCode;

    //用户名
    private String name;


    private static long uidAllocator=0;

    private long uid;



    //唯一的构造方法
    public User(String n,String password){
        name=n;

        //根据密码生成哈希值
        changePasswordInModel(password);

        uid=uidAllocator;
        uidAllocator+=1;
    }

    //getter和setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHashCode() {return passwordHashCode;}


    //用于改密码
    public void changePasswordInModel(String newPassword){
        this.passwordHashCode=hashPassword(newPassword);
    }

    //根据密码生成哈希值
    private String hashPassword(String passwordInput){
        //此处要写生成哈希值的算法
        return passwordInput;
    }

    //用于匹配密码，匹配成功返回true，失败返回false
    public boolean matchPassword(String passwordInput){
        return hashPassword(passwordInput).equals(passwordHashCode);
    }

    public long getBalance() {return balance;}

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public static long getUidAllocator() {
        return uidAllocator;
    }


    //uidAllocater的setter应该仅在读取文件中uidAllocater时使用
    public static void setUidAllocator(long uidAllocator) {
        User.uidAllocator = uidAllocator;
    }

    public long getUid() {
        return uid;
    }
}


//用户信息的集合
public class UserModel {

    // Key 是用户名 (String)，Value 是用户对象 (User)
    private Map<String, User> userMap = new ConcurrentHashMap<>();//使用多线程安全的ConcurrentHashMap

    //userMap的getter和setter
    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }








}
