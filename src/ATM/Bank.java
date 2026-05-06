package ATM;

//这是MVC架构中的Control控制层，有所有操作数据的方法逻辑
public class Bank {
    //定义常量错误码
    public static final int SUCCESS = 0;                 // 成功
    public static final int ERROR_UNKNOWN = -1;          // 未知错误
    public static final int ERROR_PASSWORD_WRONG = 1;    // 密码错误
    public static final int ERROR_USERNAME_EXIST = 2;        // 用户名已存在
    public static final int ERROR_USER_NOT_EXIST = 3;    // 用户不存在
    public static final int ERROR_USER_NOT_LOGIN = 4;    // 用户未登录
    public static final int ERROR_AMOUNT_INVALID = 5;    // 金额非法
    public static final int ERROR_BALANCE_INSUFFICIENT = 6;// 余额不足



    //用户集合
    private UserModel userModel;

    //当前用户在数组的下标，用于定位用户，不是用户uid
    int currentUserId=-1;

    public Bank(){
        userModel=new UserModel();
    }


    //创建新用户的方法，成功则返回0,失败返回各种错误码
    public int createNewUser(String n,String password){
        //先判空
        if(n!=null && password!=null) {
            if(findUserWithName(n)==-1){
                userModel.getUsers().add(new User(n, password));
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
    public int login(String n,String password){

        if(n!=null && password!=null) {
            int id=findUserWithName(n);
            if(id!=-1){

                //找到了该用户，匹配密码
                if (password.equals(userModel.getUsers().get(id).getPasswordHashCode())) {
                    currentUserId = id;
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
        if(newPassword!=null){
            userModel.getUsers().get(currentUserId).changePasswordInModel(newPassword);
            return SUCCESS;//修改成功
        }

        return ERROR_UNKNOWN;//修改失败

    }


    //根据用户名查找用户是否存在，存在则返回数组下标，不存在则返回-1
    public int findUserWithName(String n){
        if(n!=null) {
            for (int i = 0; i < userModel.getUsers().size(); i++) {
                if (n.equals(userModel.getUsers().get(i).getName()) ) {
                    return i;

                }
            }
        }

        return -1;

    }

    //存钱，传入的参数以分为单位，把小数化成整数是view层的职责
    public int deposit(long money){
        if(currentUserId!=-1){
            if(money>0) {
                userModel.getUsers().get(currentUserId).setBalance(userModel.getUsers().get(currentUserId).getBalance()+money);
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
        if(currentUserId!=-1){
            if(money>0) {
                if(userModel.getUsers().get(currentUserId).getBalance()>=money) {
                    userModel.getUsers().get(currentUserId).setBalance(userModel.getUsers().get(currentUserId).getBalance() - money);
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
        if (currentUserId != -1) {

            return userModel.getUsers().get(currentUserId).getBalance();

        }

        //System.out.println("查询失败，用户未登录");
        return -1;//这里不能用ERROR_USER_NOT_LOGIN,会和正常余额冲突
    }



}
