package ATM;

import java.util.Scanner;

public class ATM {

    Scanner sc=new Scanner(System.in);

    Bank bank;

    public ATM(){
        bank=new Bank();

        bank.loadFromFile();//ATM启动时读取数据
    }

    //登陆前的一级菜单
    public void menu1(){
        while(true){
            System.out.println("========欢迎========\n1: Login\n2: Create a new user\n3: Exit");
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
                        case Bank.ERROR_USER_DELETED:
                            System.out.println("操作失败，用户已删除");
                            break;
                        default:
                            System.out.println("操作失败，未知错误");
                    }
                    break;
                    //新建用户
                case "2":
                    System.out.print("输入用户名: ");
                    String newName=sc.nextLine();
                    if (newName == null || newName.isBlank()) {
                        System.out.println("用户名不能为空或全空格,本次操作失败");
                        break;
                    } else if (newName.startsWith("__deleted_uid__")) {
                        //用户名不能以"__deleted_uid__"开头,因为__deleted_uid__是已删除的用户的键的前缀，
                        //如果一个用户以__deleted_uid__xxx作为用户名，当uid是xxx的用户删除账号后，
                        //会在map中插入一个键为__deleted_uid__xxx的条目，这会覆盖之前以__deleted_uid__xxx作为用户名的用户
                        //因此用户名不能以"__deleted_uid__"开头
                        System.out.println("用户名不能以\"__deleted_uid__\"开头,本次操作失败");
                        break;
                    }
                    System.out.print("输入密码: ");
                    String newPassword=sc.nextLine();
                    if (newPassword == null || newPassword.isBlank()) {
                        System.out.println("密码不能为空或全空格,本次操作失败");
                        break;
                    }
                    switch(bank.createNewUser(newName,newPassword)){
                        case Bank.SUCCESS -> System.out.println("用户创建成功");
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
                    "\n请选择您要办理的业务:\n1: Change Password\n2: Query Balance\n3: Withdrawal\n4: Deposit\n5: Logout\n6: Change User Name\n7: Delete User");
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
                case "6":
                    onChangeUserName();break;
                case "7":
                    boolean deleted = onDeleteUser();
                    if (deleted) {
                        return; //删除成功，直接退出 menu2，回到一级菜单
                    }
                    break;
                default:
                    System.out.println("无效的输入");
            }
        }

    }

    private void debugMenu(){
        while(true)
        {
            System.out.println("========调试模式========\n请选择:\n1: get the number of users\n2: list all users\n3: exit");
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
        long total = bank.getTheNumberOfUsers();
        long normal = bank.getTheNumberOfNormalUsers();
        if(total == Bank.ERROR_NOT_IN_DEBUG || normal == Bank.ERROR_NOT_IN_DEBUG){
            System.out.println("错误：当前不处于调试模式");
            return;
        }
        System.out.println("当前共有 " + total + " 位用户, 其中状态为正常的用户有 " + normal + " 位\n");
    }

    public void onListAllUsers(){
        if(bank.listAllUsers()==Bank.ERROR_NOT_IN_DEBUG){
            System.out.println("错误：当前不处于调试模式");
        }
    }

    public void onChangePassword(){
        System.out.print("输入原密码：");
        String oldPassword=sc.nextLine();
        switch(bank.matchPasswordAfterLogin(oldPassword)){
            case Bank.SUCCESS -> {}
            case Bank.ERROR_USER_DELETED ->{
                System.out.println("用户已删除，操作失败");
                return;
            }
            case Bank.ERROR_PASSWORD_WRONG -> {
                System.out.println("密码错误，操作失败");
                return;
            }
            default -> {
                System.out.println("未知错误，操作失败");
                return;
            }

        }
        System.out.print("输入新密码：");
        String password1=sc.nextLine();
        if (password1 == null || password1.isBlank()) {
            System.out.println("密码不能为空或全空格,本次操作失败");
            return;
        }
        System.out.print("再次输入以确认新密码：");
        String password2=sc.nextLine();
        if(!password2.equals(password1)){
            System.out.println("两次输入的新密码不一致，修改失败");
            return;
        }

        switch(bank.changePassword(password1)){
            case Bank.SUCCESS -> System.out.println("修改密码成功");
            case Bank.ERROR_PASSWORD_SAME -> System.out.println("修改失败，新密码不能和旧密码相同");
            case Bank.ERROR_USER_DELETED -> System.out.println("操作失败，用户已删除");
            default -> System.out.println("修改密码失败,未知错误");
        }


    }

    public void onChangeUserName(){
        System.out.print("输入新用户名：");
        String newName =sc.nextLine();
        if (newName == null || newName.isBlank()) {
            System.out.println("用户名不能为空或全空格,本次操作失败");
            return;
        }else if (newName.startsWith("__deleted_uid__")) {
            System.out.println("用户名不能以\"__deleted_uid__\"开头,本次操作失败");
            return;
        }

        switch(bank.changeUserName(newName)){
            case Bank.SUCCESS -> System.out.println("修改用户名成功");
            case Bank.ERROR_USERNAME_SAME -> System.out.println("修改失败，新用户名不能和旧用户名相同");
            case Bank.ERROR_USERNAME_EXIST -> System.out.println("修改失败，该用户名已被占用");
            case Bank.ERROR_USER_DELETED -> System.out.println("操作失败，用户已删除");
            default -> System.out.println("修改用户名失败,未知错误");
        }
    }

    //删除用户的方法，需要有返回值，因为menu2依靠这个返回值判断是否退出
    public boolean onDeleteUser(){
        System.out.print("输入密码：");
        String oldPassword=sc.nextLine();
        switch(bank.matchPasswordAfterLogin(oldPassword)){
            case Bank.SUCCESS -> {}
            case Bank.ERROR_USER_DELETED ->{
                System.out.println("用户已删除，请勿重复删除");
                return false;
            }
            case Bank.ERROR_PASSWORD_WRONG -> {
                System.out.println("密码错误，操作失败");
                return false;
            }
            default -> {
                System.out.println("未知错误，操作失败");
                return false;
            }

        }

        System.out.println("确认要删除吗？(y/N)");
        String response =sc.nextLine();
        if(!response.equals("y")){
            System.out.println("删除取消");
            return false;
        }
        switch(bank.deleteUser()){
            case Bank.SUCCESS -> {
                System.out.println("用户已删除");
                return true;
            }
            case Bank.ERROR_BALANCE_NOT_ZERO -> System.out.println("用户余额不为 0 . 00 元，删除失败");
            case Bank.ERROR_USER_DELETED -> System.out.println("操作失败，用户已删除，请勿重复删除");
            default -> System.out.println("删除失败，未知错误");
        }
        return false;

    }

    public void onQuery(){
        long currentBalance=bank.query();
        if(currentBalance==Bank.ERROR_USER_NOT_LOGIN){
            System.out.println("用户未登录");
        }
        int decimalPart=(int)(currentBalance%100);//必须要先运算再转化成int类型，否则(int)转化优先级更高可能会导致溢出
        System.out.println("当前用户余额： "+currentBalance/100+" . "+
                (decimalPart<10?"0":"")
                +decimalPart+" 元");
    }

    public boolean verifyLegalityOfMoney(String money){
        // ====================== 第一步：格式合法性校验，同时能保证非负 ======================
        // 正则：只能是 数字 + 最多1个小数点 + 最多2位小数
        // 允许：123  / 123.4 / 123.45 / .45 / 123.
        if (!money.matches("^\\d*\\.?\\d{0,2}$")) {
            // 解释这个regex：
            // 首尾分别加^和$是为了严格匹配整个字符串，否则输入的字符串里有一段符合regex就会返回true
            // 字符串里想表达\需要写成转义字符\\
            // regex里\d表示某个数字，\.表示句点本身，*表示出现0次或任意次，{a}表示出现a次，{a,b}表示出现a到b次

            System.out.println("输入格式非法！只能包含数字和最多1个小数点，且最多2位小数，数字前不能有正号或负号。本次操作失败");
            return false;
        }

        // 额外判断空输入
        if (money.isEmpty()) {
            System.out.println("输入不能为空！本次操作失败");
            return false;
        }

        //格式合法性校验通过
        return true;

    }

    public void onDeposit()
    {
        System.out.print("输入要存入的金额（元）：");
        String money=sc.nextLine().replaceAll("\\s", "");


        //进行格式合法性校验
        if(!verifyLegalityOfMoney(money)){
            return;
        }

        // ====================== 第二步：拆分整数、小数部分 ======================
        String[] parts = money.split("\\.");// 在小数点处切割（必须转义），注：split方法会自动删除末尾的所有空字符串
        String stringIntegerPart = parts[0].isEmpty() ? "0" : parts[0];// 整数部分
        String stringDecimalPart = parts.length > 1  ? parts[1] : "00";// 小数部分（无小数则补00）

        // 小数不足2位要自动补0，否则输入0.1会被转为 1 而不是 10,相当于 0.1 变成了 0.01
        if (stringDecimalPart.length() == 1) {
            stringDecimalPart += "0";
        }

        // ====================== 第三步：防溢出 + 数值校验 ======================
        long integerPart;
        int decimalPart;

        try {
            //校验整数部分是否超出 long 范围
            integerPart = Long.parseLong(stringIntegerPart);
            //校验小数部分是否超出 int 范围（这里最多两位，不可能溢出，但还是加上更安全）
            decimalPart = Integer.parseInt(stringDecimalPart);
        } catch (NumberFormatException e) {
            System.out.println("金额数值过大，超出系统支持范围。本次操作失败");
            return;
        }

        //总金额不能为 0
        if (integerPart == 0 && decimalPart == 0) {
            System.out.println("存款金额不能为0，本次操作失败");
            return;
        }

        //仅仅保证整数部分不溢出还不够，整数部分会乘以100再加上小数部分，最终加到以分为单位的余额上，因此要对余额增量加上防溢出检测
        long balanceIncrement;   //余额增量
        // 校验余额增量是否超出 long 范围
        try {
            // 1. 先安全地计算 integerPart * 100
            long temp = Math.multiplyExact(integerPart, 100L);
            // 2. 再安全地加上 decimalPart
            balanceIncrement = Math.addExact(temp, decimalPart);

            // 如果走到这里，说明没有溢出
        } catch (ArithmeticException e) {
            System.out.println("金额数值过大，超出系统支持范围。本次操作失败");
            return;
        }


        //System.out.println("debug：整数部分："+integerPart+"小数部分"+decimalPart);

        // ====================== 校验通过 ======================
        int state=bank.deposit(balanceIncrement);
        switch(state){
            case Bank.SUCCESS -> System.out.println("存入成功");
            case Bank.ERROR_BALANCE_OVERFLOW -> System.out.println("存入失败，存入金额过大，会使余额超出上限");
            case Bank.ERROR_AMOUNT_INVALID -> System.out.println("存入失败，输入金额非法");
            case Bank.ERROR_USER_DELETED -> System.out.println("操作失败，用户已删除");
            case Bank.ERROR_USER_NOT_LOGIN -> System.out.println("存入失败，用户未登录");
            default -> System.out.println("存入失败，未知错误");
        }

    }

    public void onWithdrawal()
    {
        System.out.print("输入要取出的金额（元）：");
        String money=sc.nextLine().replaceAll("\\s", "");

        //进行格式合法性校验
        if(!verifyLegalityOfMoney(money)){
            return;
        }

        // ====================== 第二步：拆分整数、小数部分 ======================
        String[] parts = money.split("\\.");// 在小数点处切割（必须转义），注：split方法会自动删除末尾的所有空字符串
        String stringIntegerPart = parts[0].isEmpty() ? "0" : parts[0];// 整数部分
        String stringDecimalPart = parts.length > 1  ? parts[1] : "00";// 小数部分（无小数则补00）

        // 小数不足2位要自动补0，否则输入0.1会被转为 1 而不是 10,相当于 0.1 变成了 0.01
        if (stringDecimalPart.length() == 1) {
            stringDecimalPart += "0";
        }

        // ====================== 第三步：防溢出 + 数值校验 ======================
        long integerPart;
        int decimalPart;

        try {
            //校验整数部分是否超出 long 范围
            integerPart = Long.parseLong(stringIntegerPart);
            //校验小数部分是否超出 int 范围（这里最多两位，不可能溢出，但还是加上更安全）
            decimalPart = Integer.parseInt(stringDecimalPart);
        } catch (NumberFormatException e) {
            System.out.println("金额数值过大，超出系统支持范围。本次操作失败");
            return;
        }

        //总金额不能为 0
        if (integerPart == 0 && decimalPart == 0) {
            System.out.println("取款金额不能为0，本次操作失败");
            return;
        }

        //仅仅保证整数部分不溢出还不够，整数部分会乘以100再加上小数部分，最终让以分为单位的余额减去它，因此要对余额减量加上防溢出检测
        long withdrawMoney;   //余额减量
        // 校验余额减量是否超出 long 范围
        try {
            // 1. 先安全地计算 integerPart * 100
            long temp = Math.multiplyExact(integerPart, 100L);
            // 2. 再安全地加上 decimalPart
            withdrawMoney = Math.addExact(temp, decimalPart);

            // 如果走到这里，说明没有溢出
        } catch (ArithmeticException e) {
            System.out.println("金额数值过大，超出系统支持范围。本次操作失败");
            return;
        }


        //System.out.println("debug：整数部分："+integerPart+"小数部分"+decimalPart);

        // ====================== 校验通过 ======================
        int state=bank.withdrawal(withdrawMoney);
        switch(state){
            case Bank.SUCCESS -> System.out.println("取出成功");
            case Bank.ERROR_BALANCE_INSUFFICIENT -> System.out.println("取出失败，余额不足");
            case Bank.ERROR_AMOUNT_INVALID -> System.out.println("取出失败，输入金额非法");
            case Bank.ERROR_USER_DELETED -> System.out.println("操作失败，用户已删除");
            case Bank.ERROR_USER_NOT_LOGIN -> System.out.println("取出失败，用户未登录");
            default -> System.out.println("取出失败，未知错误");
        }
    }

    public static void main(String[] args){
        ATM ccb=new ATM();
        ccb.menu1();
        ccb.bank.shutdown();
        ccb.bank.saveToFile();//ATM退出时保存
        ccb.sc.close();//流资源使用完后需要手动释放
    }
}
