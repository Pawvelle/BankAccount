import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final BankUserManager userManager = new BankUserManager();

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        boolean running = true;
        printWelcome();
        while (running) {
            printMainMenu();
            int choice = readInt("请选择功能：");
            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    showAssetRanking();
                    break;
                case 4:
                    running = false;
                    System.out.println("系统已退出，欢迎下次使用。");
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void printWelcome() {
        System.out.println("==================================");
        System.out.println("     银行账户 CLI 操作系统");
        System.out.println("==================================");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("========== 主菜单 ==========");
        System.out.println("1. 注册账户");
        System.out.println("2. 登录账户");
        System.out.println("3. 资产排行");
        System.out.println("4. 退出系统");
        System.out.println("当前系统用户数：" + userManager.getAllUsers().size());
        System.out.println("============================");
    }

    private void registerUser() {
        System.out.println();
        System.out.println("========== 注册账户 ==========");
        try {
            String username = readRequiredText("请输入用户名：");
            String birthday = readRequiredText("请输入生日（yyyy-MM-dd）：");
            String phone = readRequiredText("请输入手机号：");
            String email = readRequiredText("请输入邮箱：");
            String password = readPasswordWithConfirmation("请输入登录密码（6位数字）：", "请再次输入登录密码：");

            BankUser user = new BankUser(username, birthday, phone, email, password);
            userManager.addUser(user);
            System.out.println();
            System.out.println("========== 注册成功 ==========");
            System.out.println("你的用户ID是：" + user.getId());
            System.out.println("用户名：" + user.getUsername());
            waitForEnter("按回车键返回主菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("注册失败：" + e.getMessage());
            waitForEnter("按回车键返回主菜单...");
        }
    }

    private void loginUser() {
        System.out.println();
        System.out.println("========== 登录账户 ==========");
        String id = readRequiredText("请输入用户ID：");
        String password = readRequiredText("请输入登录密码：");

        BankUser user = authenticate(id, password);
        if (user == null) {
            System.out.println("登录失败：用户ID或密码错误。");
            waitForEnter("按回车键返回主菜单...");
            return;
        }

        System.out.println("登录成功，欢迎你，" + user.getUsername() + "。");
        userCenter(user);
    }

    private void userCenter(BankUser user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("====== 用户中心：" + user.getUsername() + " ======");
            System.out.println("用户ID：" + user.getId());
            System.out.println("银行卡数量：" + user.getMyAccounts().size() + " 张");
            System.out.println("当前总资产：" + formatMoney(user.calculateTotalWealth()));
            System.out.println("1. 查看账户信息");
            System.out.println("2. 卡包管理");
            System.out.println("3. 修改基础信息");
            System.out.println("4. 退出登录");
            System.out.println("=================================");

            int choice = readInt("请选择功能：");
            switch (choice) {
                case 1:
                    showUserInfo(user);
                    break;
                case 2:
                    walletMenu(user);
                    break;
                case 3:
                    editBasicInfo(user);
                    break;
                case 4:
                    loggedIn = false;
                    System.out.println("你已退出当前账户。");
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void showUserInfo(BankUser user) {
        System.out.println();
        System.out.println("========== 账户信息 ==========");
        System.out.println(user.getUserInfo());
        System.out.println("登录密码：******");
        System.out.println("账户总资产：" + formatMoney(user.calculateTotalWealth()));
        System.out.println("------------------------------");
        printUserCards(user, false);
        waitForEnter("按回车键返回用户中心...");
    }

    private void walletMenu(BankUser user) {
        boolean inWallet = true;
        while (inWallet) {
            System.out.println();
            System.out.println("========== 卡包管理 ==========");
            System.out.println("1. 查看银行卡");
            System.out.println("2. 添加银行卡");
            System.out.println("3. 使用银行卡");
            System.out.println("4. 返回上一级");
            System.out.println("==============================");

            int choice = readInt("请选择功能：");
            switch (choice) {
                case 1:
                    printUserCards(user, true);
                    break;
                case 2:
                    addBankCard(user);
                    break;
                case 3:
                    useBankCard(user);
                    break;
                case 4:
                    inWallet = false;
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void addBankCard(BankUser user) {
        System.out.println();
        System.out.println("请选择银行卡类型：");
        System.out.println("1. 储蓄卡");
        System.out.println("2. 信用卡");
        int type = readInt("请输入类型编号：");

        try {
            String cardPassword = readPasswordWithConfirmation("请输入银行卡密码（6位数字）：", "请再次输入银行卡密码：");
            double balance = readDouble("请输入初始余额：");

            BankAccount account;
            if (type == 1) {
                double interestRate = readDouble("请输入利率（例如 0.02）：");
                account = new SavingsAccount(user, cardPassword, balance, interestRate);
            } else if (type == 2) {
                double creditLimit = readDouble("请输入信用额度：");
                account = new CreditAccount(user, cardPassword, balance, creditLimit);
            } else {
                System.out.println("银行卡类型无效。");
                return;
            }

            user.addAccount(account);
            System.out.println();
            System.out.println("========== 添加成功 ==========");
            System.out.println("银行卡添加完成。");
            System.out.println("卡号：" + account.getAccountNumber());
            System.out.println("卡类型：" + getAccountTypeName(account));
            System.out.println("当前余额：" + formatMoney(account.getBalance()));
            waitForEnter("按回车键返回卡包管理...");
        } catch (IllegalArgumentException e) {
            System.out.println("添加失败：" + e.getMessage());
            waitForEnter("按回车键返回卡包管理...");
        }
    }

    private void useBankCard(BankUser user) {
        if (user.getMyAccounts().isEmpty()) {
            System.out.println("当前没有银行卡，请先添加。");
            waitForEnter("按回车键返回卡包管理...");
            return;
        }

        printUserCards(user, false);
        String accountNumber = readRequiredText("请输入要使用的卡号：");
        BankAccount account = findAccountByNumber(user, accountNumber);
        if (account == null) {
            System.out.println("未找到该银行卡。");
            waitForEnter("按回车键返回卡包管理...");
            return;
        }

        boolean using = true;
        while (using) {
            printCardMenu(account);
            int choice = readInt("请选择操作：");
            switch (choice) {
                case 1:
                    doDeposit(account);
                    break;
                case 2:
                    doWithdraw(account);
                    break;
                case 3:
                    changeCardPassword(account);
                    break;
                case 4:
                    showCardInfo(account);
                    break;
                case 5:
                    if (account instanceof SavingsAccount) {
                        applySavingsInterest((SavingsAccount) account);
                    } else if (account instanceof CreditAccount) {
                        creditCardExtraMenu((CreditAccount) account);
                    } else {
                        System.out.println("当前银行卡没有扩展功能。");
                        waitForEnter("按回车键返回银行卡操作菜单...");
                    }
                    break;
                case 6:
                    using = false;
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void printCardMenu(BankAccount account) {
        System.out.println();
        System.out.println("====== 银行卡操作：" + account.getAccountNumber() + " ======");
        System.out.println("1. 存款");
        System.out.println("2. 取款");
        System.out.println("3. 修改银行卡密码");
        System.out.println("4. 查看银行卡信息");
        if (account instanceof SavingsAccount) {
            System.out.println("5. 应用利息");
        } else if (account instanceof CreditAccount) {
            System.out.println("5. 信用卡扩展功能");
        } else {
            System.out.println("5. 暂无扩展功能");
        }
        System.out.println("6. 返回上一级");
        System.out.println("=========================================");
    }

    private void doDeposit(BankAccount account) {
        try {
            String password = readRequiredText("请输入银行卡密码：");
            double amount = readDouble("请输入存款金额：");
            account.deposit(password, amount);
            showOperationResult("存款成功。", "当前余额：" + formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("存款失败：" + e.getMessage());
            waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void doWithdraw(BankAccount account) {
        try {
            String password = readRequiredText("请输入银行卡密码：");
            double amount = readDouble("请输入取款金额：");
            account.withdraw(password, amount);
            showOperationResult("取款成功。", "当前余额：" + formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("取款失败：" + e.getMessage());
            waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void changeCardPassword(BankAccount account) {
        String oldPassword = readRequiredText("请输入旧银行卡密码：");
        String newPassword = readRequiredText("请输入新银行卡密码：");
        String confirmPassword = readRequiredText("请再次输入新银行卡密码：");
        boolean success = account.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
        if (success) {
            showOperationResult("银行卡密码修改成功。", "卡号：" + account.getAccountNumber(),
                    "按回车键返回银行卡操作菜单...");
        } else {
            waitForEnter("银行卡密码未修改，按回车键返回银行卡操作菜单...");
        }
    }

    private void showCardInfo(BankAccount account) {
        System.out.println();
        System.out.println("========== 银行卡信息 ==========");
        System.out.println("卡号：" + account.getAccountNumber());
        System.out.println("持有人ID：" + account.getAccountHolder());
        System.out.println("卡类型：" + getAccountTypeName(account));
        System.out.println("余额：" + formatMoney(account.getBalance()));

        if (account instanceof SavingsAccount) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            System.out.println("利率：" + savingsAccount.getInterestRate());
        } else if (account instanceof CreditAccount) {
            CreditAccount creditAccount = (CreditAccount) account;
            System.out.println("信用额度：" + formatMoney(creditAccount.getCreditLimit()));
            System.out.println("已用额度：" + formatMoney(creditAccount.getUsedCredit()));
            System.out.println("可用额度：" + formatMoney(
                    creditAccount.getBalance() + creditAccount.getCreditLimit() - creditAccount.getUsedCredit()));
        }
        waitForEnter("按回车键返回银行卡操作菜单...");
    }

    private void applySavingsInterest(SavingsAccount account) {
        account.applyInterest();
        showOperationResult("利息处理完成。", "当前余额：" + formatMoney(account.getBalance()),
                "按回车键返回银行卡操作菜单...");
    }

    private void creditCardExtraMenu(CreditAccount account) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("====== 信用卡扩展功能 ======");
            System.out.println("1. 线上支付");
            System.out.println("2. 还款");
            System.out.println("3. 查看美元余额");
            System.out.println("4. 返回上一级");
            System.out.println("============================");

            int choice = readInt("请选择功能：");
            switch (choice) {
                case 1:
                    doOnlinePay(account);
                    break;
                case 2:
                    doRepay(account);
                    break;
                case 3:
                    showOperationResult("美元余额查看完成。",
                            "折算美元余额：" + String.format("%.2f USD", account.convertToUSD()),
                            "按回车键返回信用卡菜单...");
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void doOnlinePay(CreditAccount account) {
        try {
            String password = readRequiredText("请输入银行卡密码：");
            double amount = readDouble("请输入支付金额：");
            account.payOnline(password, amount);
            showOperationResult("线上支付成功。", "支付后余额：" + formatMoney(account.getBalance()),
                    "按回车键返回信用卡菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("支付失败：" + e.getMessage());
            waitForEnter("按回车键返回信用卡菜单...");
        }
    }

    private void doRepay(CreditAccount account) {
        try {
            double amount = readDouble("请输入还款金额：");
            String password = readRequiredText("请输入银行卡密码：");
            account.repay(amount, password);
            showOperationResult("还款处理完成。",
                    "当前余额：" + formatMoney(account.getBalance()) + "\n当前已用额度：" +
                            formatMoney(account.getUsedCredit()),
                    "按回车键返回信用卡菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("还款失败：" + e.getMessage());
            waitForEnter("按回车键返回信用卡菜单...");
        }
    }

    private void editBasicInfo(BankUser user) {
        boolean editing = true;
        while (editing) {
            System.out.println();
            System.out.println("========== 修改基础信息 ==========");
            System.out.println("1. 修改用户名");
            System.out.println("2. 修改生日");
            System.out.println("3. 修改手机号");
            System.out.println("4. 修改邮箱");
            System.out.println("5. 修改登录密码");
            System.out.println("6. 返回上一级");
            System.out.println("==================================");

            int choice = readInt("请选择功能：");
            try {
                switch (choice) {
                    case 1:
                        user.setUsername(readRequiredText("请输入新用户名："));
                        showProfileUpdateResult("用户名已更新。", user);
                        break;
                    case 2:
                        user.setBirthday(readRequiredText("请输入新生日（yyyy-MM-dd）："));
                        showProfileUpdateResult("生日已更新。", user);
                        break;
                    case 3:
                        user.setPhone(readRequiredText("请输入新手机号："));
                        showProfileUpdateResult("手机号已更新。", user);
                        break;
                    case 4:
                        user.setEmail(readRequiredText("请输入新邮箱："));
                        showProfileUpdateResult("邮箱已更新。", user);
                        break;
                    case 5:
                        String oldPassword = readRequiredText("请输入旧登录密码：");
                        String newPassword = readRequiredText("请输入新登录密码：");
                        String confirmPassword = readRequiredText("请再次输入新登录密码：");
                        boolean success = user.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
                        if (success) {
                            showOperationResult("登录密码修改成功。", "用户ID：" + user.getId(),
                                    "按回车键返回修改菜单...");
                        } else {
                            waitForEnter("登录密码未修改，按回车键继续...");
                        }
                        break;
                    case 6:
                        editing = false;
                        break;
                    default:
                        System.out.println("无效选项，请重新输入。");
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("修改失败：" + e.getMessage());
            }
        }
    }

    private void showAssetRanking() {
        System.out.println();
        System.out.println("========== 资产排行 ==========");
        List<BankUser> ranking = userManager.getRankingByAssets();
        if (ranking.isEmpty()) {
            System.out.println("当前还没有用户数据。");
            waitForEnter("按回车键返回主菜单...");
            return;
        }

        for (int i = 0; i < ranking.size(); i++) {
            BankUser user = ranking.get(i);
            System.out.println((i + 1) + ". " + user.getUsername() + "（" + user.getId() + "）");
            System.out.println("   总资产：" + formatMoney(user.calculateTotalWealth()));
        }
        waitForEnter("按回车键返回主菜单...");
    }

    private void printUserCards(BankUser user, boolean waitAfterPrint) {
        List<BankAccount> accounts = user.getMyAccounts();
        if (accounts.isEmpty()) {
            System.out.println("当前没有银行卡。");
            if (waitAfterPrint) {
                waitForEnter("按回车键返回上一级...");
            }
            return;
        }

        System.out.println("========== 我的银行卡 ==========");
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount account = accounts.get(i);
            System.out.println((i + 1) + ". 卡号：" + account.getAccountNumber());
            System.out.println("   类型：" + getAccountTypeName(account));
            System.out.println("   余额：" + formatMoney(account.getBalance()));
        }
        if (waitAfterPrint) {
            waitForEnter("按回车键返回上一级...");
        }
    }

    private BankUser authenticate(String id, String password) {
        for (BankUser user : userManager.getAllUsers()) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private BankAccount findAccountByNumber(BankUser user, String accountNumber) {
        for (BankAccount account : user.getMyAccounts()) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    private String getAccountTypeName(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return "储蓄卡";
        }
        if (account instanceof CreditAccount) {
            return "信用卡";
        }
        return "未知类型";
    }

    private String readPasswordWithConfirmation(String prompt, String confirmPrompt) {
        String password = readRequiredText(prompt);
        String confirmPassword = readRequiredText(confirmPrompt);
        if (!BankAccount.validatePassword(password, confirmPassword)) {
            throw new IllegalArgumentException("密码验证失败。");
        }
        return password;
    }

    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            ensureInputAvailable();
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }
    }

    private String formatMoney(double amount) {
        return String.format("%.2f 元", amount);
    }

    private void showOperationResult(String title, String detail, String prompt) {
        System.out.println();
        System.out.println("========== 操作结果 ==========");
        System.out.println(title);
        System.out.println(detail);
        waitForEnter(prompt);
    }

    private void showProfileUpdateResult(String message, BankUser user) {
        System.out.println();
        System.out.println("========== 修改结果 ==========");
        System.out.println(message);
        System.out.println("当前信息：");
        System.out.println(user.getUserInfo());
        waitForEnter("按回车键返回修改菜单...");
    }

    private void waitForEnter(String prompt) {
        System.out.print(prompt);
        ensureInputAvailable();
        scanner.nextLine();
    }

    private void ensureInputAvailable() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("输入已结束，系统自动退出。");
            System.exit(0);
        }
    }
}
