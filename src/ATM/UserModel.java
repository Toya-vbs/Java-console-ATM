package ATM;

import java.util.ArrayList;
import java.util.List;


//一个用户的信息
class User {
    //余额，初始为0,单位为分
    private long balance=0;

    //存储密码的哈希值，用SHA-256加密算法
    private String passwordHashCode;

    //用户名
    private String name;


    private static long uidAllocater=0;

    private long uid;



    //唯一的构造方法
    public User(String n,String password){
        name=n;

        //根据密码生成哈希值
        changePasswordInModel(password);

        uid=uidAllocater;
        uidAllocater+=1;
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
        //此处要写生成哈希值的算法
        String newPasswordHashCode=newPassword;


        setPasswordHashCode(newPasswordHashCode);
    }
    //用于改密码
    public void setPasswordHashCode(String newPasswordHashCode) {this.passwordHashCode = newPasswordHashCode;}

    public long getBalance() {return balance;}

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public static long getUidAllocater() {
        return uidAllocater;
    }


    //uidAllocater的setter应该仅在读取文件中uidAllocater时使用
    public static void setUidAllocater(long uidAllocater) {
        User.uidAllocater = uidAllocater;
    }

    public long getUid() {
        return uid;
    }
}


//用户信息的集合
public class UserModel {

    private List<User> users;

    public UserModel(){
        users=new ArrayList<User>();
    }
    public List<User> getUsers() {
        return users;
    }




}
