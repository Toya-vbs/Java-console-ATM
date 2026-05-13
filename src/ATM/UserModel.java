package ATM;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//一个用户的信息
class User {

    //余额，初始为0,单位为分
    private long balance=0;

    //余额最大值
    static final long MAX_BALANCE=Long.MAX_VALUE;

    //存储密码的哈希值，用SHA-256加密算法
    private String passwordHashCode;

    //想要保证密码安全就要加盐
    private byte[] salt;

    //用户名
    private String name;

    //分配uid的变量
    private static long uidAllocator=0;

    //用户uid
    private long uid;



    //唯一的构造方法
    public User(String n,String password){
        name=n;

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

    public String getPasswordHashCode() {return passwordHashCode;}

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
