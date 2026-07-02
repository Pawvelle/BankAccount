package com.bank.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class BankUser implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private String username;
    private String birthday;
    private String phone;
    private String email;
    private String password;
    private double totalAssets;
    private List<BankAccount> myAccounts;
    private static int userCounter = 100001;

    public BankUser(String username, String birthday, String phone, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空！");
        }
        this.birthday = validateBirthday(birthday);
        this.username = username.trim();
        this.id = "USER_" + userCounter++;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.totalAssets = 0;
        this.myAccounts = new ArrayList<>();
    }

    public static void updateUserCounter(int maxLoadedId) {
        if (maxLoadedId >= userCounter) {
            userCounter = maxLoadedId + 1;
        }
    }

    public boolean setNewAccountPassword(String oldPassword, String newPassword, String confirmPassword) {
        // 验证旧密码是否正确
        if (oldPassword == null || !oldPassword.equals(this.password)) {
            System.out.println("旧密码错误！");
            return false;
        }

        // 验证密码长度是否为6位数字
        if (!newPassword.matches("\\d{6}")) {
            System.out.println("密码必须是6位数字！");
            return false;
        }

        // 验证两次输入的密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("两次输入的密码不一致！");
            return false;
        }

        // 设置新密码
        this.password = newPassword;
        System.out.println("密码设置成功！");
        return true;
    }

    public void addAccount(BankAccount acc) {
        if (acc == null) {
            System.out.println("账户不能为空！");
            return;
        }

        if (acc.getUser() != this) {
            System.out.println("该银行卡不属于当前用户，无法添加！");
            return;
        }

        if (myAccounts.contains(acc)) {
            System.out.println("该账户已存在，无需重复添加！");
            return;
        }

        myAccounts.add(acc);
        totalAssets += acc.getBalance();
        System.out.println("账户添加成功！");
    }

    public void displayMyAssets() {
        if (myAccounts.isEmpty()) {
            System.out.println("当前没有任何账户！");
            return;
        }

        System.out.println("=== " + username + "（" + id + "）的资产信息 ===");
        for (BankAccount acc : myAccounts) {
            System.out.println(acc);
        }
    }

    // 计算当前用户名下所有银行卡总财富（仅按 balance 统计）
    public double calculateTotalWealth() {
        double totalWealth = 0;
        for (BankAccount account : myAccounts) {
            if (account == null) {
                continue;
            }
            totalWealth += account.getBalance();
        }
        this.totalAssets = totalWealth;
        return totalWealth;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    // 修改用户名
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空！");
        }
        String trimmedUsername = username.trim();
        if (this.username.equals(trimmedUsername)) {
            System.out.println("新用户名与当前用户名相同，无需修改。");
            return;
        }

        this.username = trimmedUsername;
        System.out.println("用户名修改成功！");
    }

    // 修改生日
    public void setBirthday(String birthday) {
        this.birthday = validateBirthday(birthday);
        System.out.println("生日修改成功！");
    }

    public static String validateEmail(String email) {
        if (!email.matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式错误！请输入有效的邮箱地址，例如：example@mail.com");
        }
        return email;
    }

    public static String validateBirthday(String birthday) {
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("生日格式错误！请使用yyyy-MM-dd格式，例如：1990-01-01");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(birthday);
        } catch (ParseException e) {
            throw new IllegalArgumentException("生日无效！请输入有效的日期，例如：1990-01-01");
        }

        return birthday;
    }

    // 修改电话号码
    public void setPhone(String phone) {
        this.phone = phone;
        System.out.println("电话号码修改成功！");
    }

    // 修改电子邮箱
    public void setEmail(String email) {
        this.email = email;
        System.out.println("电子邮箱修改成功！");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<BankAccount> getMyAccounts() {
        return myAccounts;
    }

    // 获取用户唯一标识
    public String getId() {
        return id;
    }

    // 获取用户的全部信息
    public String getUserInfo() {
        return "用户ID：" + id + "\n" +
                "用户名：" + username + "\n" +
                "生日：" + birthday + "\n" +
                "电话号码：" + phone + "\n" +
                "电子邮箱：" + email;
    }

}
