package com.bank.model;

import com.bank.exception.AccountLockedException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidPasswordException;
import com.bank.util.PasswordUtils;

import java.io.Serializable;

// 银行账户类，抽象类，包含账户编号、账户持有人、账户哈希密码、盐值、账户余额、防刷锁机制
public abstract class BankAccount implements Serializable {
    private static final long serialVersionUID = 2L;

    private BankUser user;
    private String accountNumber;
    private String passwordHash;
    private String salt;
    private String accountPassword; // 兼容旧版本明文序列化字段
    protected double balance;
    private int failedAttempts = 0;
    private boolean isLocked = false;

    private static int accountCounter = 2026001;
    public static final int MAX_FAILED_ATTEMPTS = 3;

    // 构造方法
    public BankAccount(BankUser user, String rawPassword, double balance) {
        if (user == null) {
            throw new BankException("账户所属用户不能为空！");
        }
        PasswordUtils.validatePasswordFormat(rawPassword);

        if (balance < 0) {
            throw new BankException("初始余额不能小于0！");
        }

        this.user = user;
        this.balance = balance;
        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hashPassword(rawPassword, salt);
        this.accountNumber = "HUE_" + accountCounter++;
    }

    public static void updateAccountCounter(int maxLoadedId) {
        if (maxLoadedId >= accountCounter) {
            accountCounter = maxLoadedId + 1;
        }
    }

    public static boolean validatePassword(String newPassword, String confirmPassword) {
        PasswordUtils.validatePasswordConfirmation(newPassword, confirmPassword);
        return true;
    }

    // 密码安全校验与防刷计数
    public void verifyPassword(String inputPassword) {
        if (isLocked) {
            throw new AccountLockedException("该银行卡（" + accountNumber + "）已被锁定！密码输入错误已达3次，请联系客服解锁。");
        }

        // 兼容升级旧数据
        if (salt == null || passwordHash == null) {
            upgradeLegacyPassword();
        }

        if (!PasswordUtils.verifyPassword(inputPassword, passwordHash, salt)) {
            failedAttempts++;
            int remaining = MAX_FAILED_ATTEMPTS - failedAttempts;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                isLocked = true;
                throw new AccountLockedException("密码错误！连续错误已达 " + MAX_FAILED_ATTEMPTS + " 次，银行卡（" + accountNumber + "）已被自动锁定！");
            } else {
                throw new InvalidPasswordException("银行卡密码错误！剩余重试次数：" + remaining + " 次。");
            }
        }

        // 验证成功，重置计数
        failedAttempts = 0;
    }

    private synchronized void upgradeLegacyPassword() {
        if (this.salt == null) {
            this.salt = PasswordUtils.generateSalt();
            String legacy = (this.accountPassword != null) ? this.accountPassword : "000000";
            this.passwordHash = PasswordUtils.hashPassword(legacy, salt);
        }
    }

    // 获取账户持有人唯一标识
    public String getAccountHolder() {
        return user.getId();
    }

    // 存款方法，先验证密码再存入金额
    public void deposit(String password, double amount) {
        verifyPassword(password);

        if (amount <= 0) {
            throw new BankException("存款金额必须大于0！");
        }
        balance += amount;
    }

    // 抽象方法：取款方法，先验证密码再进行规则判定
    public abstract void withdraw(String password, double amount);

    public boolean setNewAccountPassword(String oldPassword, String newPassword, String confirmPassword) {
        verifyPassword(oldPassword);
        PasswordUtils.validatePasswordConfirmation(newPassword, confirmPassword);

        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hashPassword(newPassword, salt);
        this.accountPassword = null;
        this.failedAttempts = 0;
        this.isLocked = false;
        return true;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void unlock() {
        this.isLocked = false;
        this.failedAttempts = 0;
    }

    // 获取账户编号
    public String getAccountNumber() {
        return accountNumber;
    }

    // 获取账户余额
    public double getBalance() {
        return balance;
    }

    // 获取账户所属用户
    public BankUser getUser() {
        return user;
    }
}
