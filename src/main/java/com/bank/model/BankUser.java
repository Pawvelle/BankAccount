package com.bank.model;

import com.bank.exception.AccountLockedException;
import com.bank.exception.BankException;
import com.bank.exception.InvalidPasswordException;
import com.bank.util.PasswordUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BankUser implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String id;
    private String username;
    private String birthday;
    private String phone;
    private String email;
    private String passwordHash;
    private String salt;
    private String password; // 兼容旧版明文字段
    private double totalAssets;
    private List<BankAccount> myAccounts;
    private int failedLoginAttempts = 0;
    private boolean isLocked = false;
    // 旧版数据升级后，用于提示用户尽快修改密码（非序列化）
    private transient boolean legacyPasswordUpgraded = false;

    private static int userCounter = 100001;
    public static final int MAX_FAILED_ATTEMPTS = 3;

    public BankUser(String username, String birthday, String phone, String email, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new BankException("用户名不能为空！");
        }
        this.birthday = validateBirthday(birthday);
        this.email = validateEmail(email);
        validatePhone(phone);
        PasswordUtils.validatePasswordFormat(rawPassword);

        this.username = username.trim();
        this.id = "USER_" + userCounter++;
        this.phone = phone.trim();
        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hashPassword(rawPassword, salt);
        this.totalAssets = 0;
        this.myAccounts = new ArrayList<>();
    }

    public static void updateUserCounter(int maxLoadedId) {
        if (maxLoadedId >= userCounter) {
            userCounter = maxLoadedId + 1;
        }
    }

    public void verifyPassword(String inputPassword) {
        if (isLocked) {
            throw new AccountLockedException("账号（" + id + "）已被锁定！登录密码连续输入错误已达3次，请联系管理员。");
        }

        if (salt == null || passwordHash == null) {
            upgradeLegacyPassword();
        }

        if (!PasswordUtils.verifyPassword(inputPassword, passwordHash, salt)) {
            failedLoginAttempts++;
            int remaining = MAX_FAILED_ATTEMPTS - failedLoginAttempts;
            if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
                isLocked = true;
                throw new AccountLockedException("登录密码错误！连续错误已达 " + MAX_FAILED_ATTEMPTS + " 次，账号（" + id + "）已被自动锁定！");
            } else {
                throw new InvalidPasswordException("登录密码错误！剩余重试次数：" + remaining + " 次。");
            }
        }

        // 验证成功，重置失败次数
        failedLoginAttempts = 0;
    }

    private synchronized void upgradeLegacyPassword() {
        if (this.salt == null) {
            this.salt = PasswordUtils.generateSalt();
            String legacy = (this.password != null) ? this.password : "000000";
            this.passwordHash = PasswordUtils.hashPassword(legacy, salt);
            // 升级后立即清空明文密码，避免在内存中残留
            this.password = null;
            // 标记为升级态，登录后强制提示用户改密
            this.legacyPasswordUpgraded = true;
        }
    }

    /**
     * 是否在本次会话中从旧版数据升级而来。返回 true 时建议强制用户修改密码。
     */
    public boolean isLegacyPasswordUpgraded() {
        return legacyPasswordUpgraded;
    }

    public void clearLegacyPasswordUpgradedFlag() {
        this.legacyPasswordUpgraded = false;
    }

    public boolean setNewAccountPassword(String oldPassword, String newPassword, String confirmPassword) {
        verifyPassword(oldPassword);
        PasswordUtils.validatePasswordConfirmation(newPassword, confirmPassword);
        applyNewPassword(newPassword);
        return true;
    }

    /**
     * 强制重置密码（不校验旧密码），用于账号被锁后的找回流程。
     */
    public void forceResetPassword(String newPassword, String confirmPassword) {
        PasswordUtils.validatePasswordConfirmation(newPassword, confirmPassword);
        applyNewPassword(newPassword);
    }

    private void applyNewPassword(String newPassword) {
        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hashPassword(newPassword, salt);
        this.password = null;
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.legacyPasswordUpgraded = false;
    }

    public void addAccount(BankAccount acc) {
        if (acc == null) {
            throw new BankException("账户不能为空！");
        }

        if (acc.getUser() != this) {
            throw new BankException("该银行卡不属于当前用户，无法添加！");
        }

        if (myAccounts.contains(acc)) {
            throw new BankException("该账户已存在，无需重复添加！");
        }

        myAccounts.add(acc);
        // 注意：totalAssets 字段保留仅用于兼容旧版序列化数据。
        // 实际取数请使用 getTotalAssets() —— 它会实时计算。
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
        // 始终返回实时计算值，避免字段 stale（历史版本曾因 addAccount 累加导致不一致）
        return calculateTotalWealth();
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BankException("用户名不能为空！");
        }
        String trimmedUsername = username.trim();
        if (this.username.equals(trimmedUsername)) {
            System.out.println("新用户名与当前用户名相同，无需修改。");
            return;
        }

        this.username = trimmedUsername;
    }

    public void setBirthday(String birthday) {
        this.birthday = validateBirthday(birthday);
    }

    public static String validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new BankException("邮箱格式错误！请输入有效的邮箱地址，例如：example@mail.com");
        }
        return email;
    }

    public static String validateBirthday(String birthday) {
        if (birthday == null || !birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new BankException("生日格式错误！请使用yyyy-MM-dd格式，例如：1990-01-01");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(birthday);
        } catch (ParseException e) {
            throw new BankException("生日无效！请输入有效的日期，例如：1990-01-01");
        }

        return birthday;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BankException("手机号不能为空！");
        }
        String trimmed = phone.trim();
        if (!trimmed.matches("^1[3-9]\\d{9}$")) {
            throw new BankException("手机号格式错误！请输入有效的11位中国大陆手机号，例如：13800138000");
        }
        return trimmed;
    }

    public void setPhone(String phone) {
        this.phone = validatePhone(phone);
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void unlock() {
        this.isLocked = false;
        this.failedLoginAttempts = 0;
    }

    public String getUsername() {
        return username;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public List<BankAccount> getMyAccounts() {
        return myAccounts;
    }

    public String getId() {
        return id;
    }

    public String getUserInfo() {
        return "用户ID：" + id + "\n" +
                "用户名：" + username + "\n" +
                "生日：" + birthday + "\n" +
                "电话号码：" + phone + "\n" +
                "电子邮箱：" + email + "\n" +
                "状态：" + (isLocked ? "已锁定 (密码错3次)" : "正常");
    }

    @Override
    public String toString() {
        return "BankUser{id=" + id + ", username=" + username
                + ", 总资产=" + String.format("%.2f", getTotalAssets())
                + ", 卡片数=" + myAccounts.size()
                + ", 状态=" + (isLocked ? "已锁定" : "正常") + "}";
    }
}
