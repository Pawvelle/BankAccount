import java.util.Objects;

// 银行账户类，抽象类，包含账户编号、账户持有人、账户密码、账户余额、账户计数器
public abstract class BankAccount {
    private BankUser user;
    private String accountNumber;
    private String accountPassword;
    protected double balance;
    private static int accountCounter = 2026001;

    // 构造方法
    public BankAccount(BankUser user, String accountPassword, double balance) {
        this.user = user;
        this.accountPassword = accountPassword;

        if (balance < 0) {
            throw new IllegalArgumentException("余额不能小于0!请输入正确的余额！");
        }
        this.balance = balance;
        this.accountNumber = "HUE_" + accountCounter;
        accountCounter++;
    }

    public static boolean validatePassword(String newPassword, String confirmPassword) {
        // 验证新密码是否为6位数字
        if (!newPassword.matches("\\d{6}")) {
            System.out.println("密码必须是6位数字！");
            return false;
        }

        // 验证两次输入的新密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("两次输入的密码不一致！");
            return false;
        }

        System.out.println("密码验证成功！");
        return true;
    }

    // 获取账户持有人唯一标识
    public String getAccountHolder() {
        return user.getId();
    }

    // 存款方法，先输入密码再输入金额
    public void deposit(String password, double amount) {
        if (!Objects.equals(password, accountPassword)) {
            throw new IllegalArgumentException("密码错误!");
        }

        if (amount > 0) {
            balance += amount;
        } else {
            throw new IllegalArgumentException("存款金额必须大于0!");
        }
    }

    // 抽象方法
    // 取款方法，先输入密码再输入金额
    public abstract void withdraw(String password, double amount);

    public boolean setNewAccountPassword(String oldPassword, String newPassword, String confirmPassword) {
        // 验证旧密码是否正确
        if (!oldPassword.equals(accountPassword)) {
            System.out.println("旧密码错误！");
            return false;
        }

        // 验证新密码是否为6位数字
        if (!newPassword.matches("\\d{6}")) {
            System.out.println("密码必须是6位数字！");
            return false;
        }

        // 验证两次输入的新密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("两次输入的密码不一致！");
            return false;
        }

        // 设置新密码
        this.accountPassword = newPassword;
        System.out.println("密码设置成功！");
        return true;
    }

    // 获取账户编号
    public String getAccountNumber() {
        return accountNumber;
    }

    // 获取账户余额
    public double getBalance() {
        return balance;
    }

    // 获取账户密码
    public String getAccountPassword() {
        return accountPassword;
    }

    // 获取账户所属用户
    public BankUser getUser() {
        return user;
    }

}
