package ATM;

import java.util.Scanner;

public class ATM {

    Scanner sc=new Scanner(System.in);

    Bank bank;

    public ATM(){
        bank=new Bank();
    }

    //登陆前的一级菜单
    public void menu1(){
        while(true){
            System.out.println("========欢迎========\n1：Login\n2：Create a new user\n3：Exit");
            String code=sc.nextLine().replaceAll("\\s", "");
            if(code.equals("3")){
                break;
            }
            switch(code){
                //登录
                case "1":
                    System.out.print("输入用户名: ");
                    String inputName=sc.nextLine();
                    System.out.print("输入密码: ");
                    String inputPassword=sc.nextLine();
                    switch(bank.login(inputName,inputPassword)){
                        case Bank.SUCCESS:
                            System.out.println("登录成功！");
                            menu2();
                            break;
                        case Bank.ERROR_PASSWORD_WRONG:
                            System.out.println("操作失败，密码错误");
                            break;
                        case Bank.ERROR_USER_NOT_EXIST:
                            System.out.println("操作失败，用户不存在");
                            break;
                        default:
                            System.out.println("操作失败，未知错误");
                    }
                    break;
                    //新建用户
                case "2":
                    System.out.print("输入用户名: ");
                    String newName=sc.nextLine();
                    System.out.print("输入密码: ");
                    String newPassword=sc.nextLine();
                    switch(bank.createNewUser(newName,newPassword)){
                        case Bank.SUCCESS -> {
                            System.out.println("用户创建成功");
                        }
                        case Bank.ERROR_USERNAME_EXIST-> System.out.println("用户创建失败，该用户名已被占用");
                        default -> System.out.println("用户创建失败，未知错误");
                    }
                    break;
                case "debug":
                    bank.setDebug(true);
                    System.out.println("debug模式已开启");
                    debugMenu();
                    break;
                default:
                    System.out.println("无效的输入");
            }

        }
    }

    //登录后的二级菜单
    public void menu2(){
        while(true)
        {
            System.out.println("用户名："+bank.currentUser.getName()+"\nuid: "+bank.currentUser.getUid()+
                    "\n请选择您要办理的业务:\n1: Change Password\n2: Query Balance\n3: Withdrawal\n4: Deposit\n5: Logout");
            String code=sc.nextLine().replaceAll("\\s", "");
            if(code.equals("5")){
                System.out.println("登出成功");
                break;
            }
            switch(code){
                case "1":
                    onChangePassword();break;
                case "2":
                    onQuery();break;
                case "3":
                    onWithdrawal();break;
                case "4":
                    onDeposit();break;
                default:
                    System.out.println("无效的输入");
            }
        }

    }

    private void debugMenu(){
        while(true)
        {
            System.out.println("========调试模式========\n请选择:\n1: get the number of users\n2: list all users\n3. exit");
            String code=sc.nextLine().replaceAll("\\s", "");
            if(code.equals("3")){
                bank.setDebug(false);
                System.out.println("已退出调试模式");
                break;
            }
            switch(code){
                case "1":
                    onGetTheNumberOfUsers();break;
                case "2":
                    onListAllUsers();break;
                default:
                    System.out.println("无效的输入");
            }
        }

    }

    //调试方法
    public void onGetTheNumberOfUsers(){
        int num=bank.getTheNumberOfUsers();
        if(num==Bank.ERROR_NOT_IN_DEBUG){
            System.out.println("错误：当前不处于调试模式");
            return;
        }
        System.out.println("当前共有 "+num+" 位用户\n");
    }

    public void onListAllUsers(){
        if(bank.listAllUsers()==Bank.ERROR_NOT_IN_DEBUG){
            System.out.println("错误：当前不处于调试模式");
            return;
        }
        return;
    }

    public void onChangePassword(){
        System.out.print("输入新密码：");
        String password1=sc.nextLine();
        System.out.print("再次输入以确认新密码：");
        String password2=sc.nextLine();
        if(!password2.equals(password1)){
            System.out.println("两次输入的新密码不一致，修改失败");
            return;
        }

        switch(bank.changePassword(password1)){
            case Bank.SUCCESS -> System.out.println("修改密码成功");
            default -> System.out.println("修改密码失败,未知错误");
        }


    }

    public void onQuery(){
        long currentBalance=bank.query();
        if(currentBalance==-1){
            System.out.println("用户未登录");
        }
        int decimalPart=(int)currentBalance%100;
        System.out.println("当前用户余额： "+currentBalance/100+" . "+
                (decimalPart<10?"0":"")
                +decimalPart+" 元");
    }

    public void onDeposit()
    {
        System.out.print("输入要存入的金额（元）：");
        String money=sc.nextLine().replaceAll("\\s", "");
        // 切割小数点（必须转义）
        String[] parts = money.split("\\.");
        String stringIntegerPart = parts[0]; // 整数部分
        String stringDecimalPart = parts.length > 1 ? parts[1] : "00"; // 小数部分（无小数则补00）
        //目前的问题：没有输入合法性验证，输入不是数字会抛异常崩溃
        long integerPart=Long.parseLong(stringIntegerPart);
        int decimalPart=Integer.parseInt(stringDecimalPart);
        if(integerPart<0||decimalPart>=100){
            System.out.println("输入的金额非法，本次操作失败");
            return;
        }


        System.out.println("debug：整数部分："+integerPart+"小数部分"+decimalPart);

        int state=bank.deposit(integerPart*100+decimalPart);
        switch(state){
            case Bank.SUCCESS -> System.out.println("存入成功");
            case Bank.ERROR_AMOUNT_INVALID -> System.out.println("存入失败，输入金额非法");
            case Bank.ERROR_USER_NOT_LOGIN -> System.out.println("存入失败，用户未登录");
            default -> System.out.println("存入失败，未知错误");
        }

    }

    public void onWithdrawal()
    {
        System.out.print("输入要取出的金额（元）：");
        String money=sc.nextLine().replaceAll("\\s", "");

        // 切割小数点（必须转义）
        String[] parts = money.split("\\.");
        String stringIntegerPart = parts[0]; // 整数部分
        String stringDecimalPart = parts.length > 1 ? parts[1] : "00"; // 小数部分（无小数则补00）
        long integerPart=Long.parseLong(stringIntegerPart);
        int decimalPart=Integer.parseInt(stringDecimalPart);

        if(integerPart<0||decimalPart>=100){
            System.out.println("输入的金额非法，本次操作失败");
            return;
        }
        long withdrawMoney=integerPart*100+decimalPart;
        int state=bank.withdrawal(withdrawMoney);
        switch(state){
            case Bank.SUCCESS -> System.out.println("取出成功");
            case Bank.ERROR_BALANCE_INSUFFICIENT -> System.out.println("取出失败，余额不足");
            case Bank.ERROR_AMOUNT_INVALID -> System.out.println("取出失败，输入金额非法");
            case Bank.ERROR_USER_NOT_LOGIN -> System.out.println("取出失败，用户未登录");
            default -> System.out.println("取出失败，未知错误");
        }
    }

    public static void main(String[] args){
        ATM ccb=new ATM();
        ccb.menu1();
        //还要销毁scanner
    }
}
