package com.bank.ui;

import com.bank.model.*;
import com.bank.service.BankUserManager;

import java.util.List;

public class CardMenuUI {
    private final BankUserManager userManager;

    public CardMenuUI(BankUserManager userManager) {
        this.userManager = userManager;
    }

    public void walletMenu(BankUser user) {
        boolean inWallet = true;
        while (inWallet) {
            System.out.println();
            System.out.println("========== 卡包管理 ==========");
            System.out.println("1. 查看银行卡");
            System.out.println("2. 添加银行卡");
            System.out.println("3. 使用银行卡");
            System.out.println("4. 返回上一级");
            System.out.println("==============================");

            int choice = ConsoleUtils.readInt("请选择功能：");
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
                    userManager.saveData(); // 离开卡包管理时保存数据
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    public void printUserCards(BankUser user, boolean waitAfterPrint) {
        List<BankAccount> accounts = user.getMyAccounts();
        if (accounts.isEmpty()) {
            System.out.println("当前没有银行卡。");
            if (waitAfterPrint) {
                ConsoleUtils.waitForEnter("按回车键返回上一级...");
            }
            return;
        }

        System.out.println("========== 我的银行卡 ==========");
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount account = accounts.get(i);
            System.out.println((i + 1) + ". 卡号：" + account.getAccountNumber());
            System.out.println("   类型：" + getAccountTypeName(account));
            System.out.println("   余额：" + ConsoleUtils.formatMoney(account.getBalance()));
        }
        if (waitAfterPrint) {
            ConsoleUtils.waitForEnter("按回车键返回上一级...");
        }
    }

    private void addBankCard(BankUser user) {
        System.out.println();
        System.out.println("请选择银行卡类型：");
        System.out.println("1. 储蓄卡");
        System.out.println("2. 信用卡");
        int type = ConsoleUtils.readInt("请输入类型编号：");

        try {
            String cardPassword = ConsoleUtils.readPasswordWithConfirmation("请输入银行卡密码（6位数字）：", "请再次输入银行卡密码：");
            double balance = ConsoleUtils.readDouble("请输入初始余额：");

            BankAccount account;
            if (type == 1) {
                double interestRate = ConsoleUtils.readDouble("请输入利率（例如 0.02）：");
                account = new SavingsAccount(user, cardPassword, balance, interestRate);
            } else if (type == 2) {
                double creditLimit = ConsoleUtils.readDouble("请输入信用额度：");
                account = new CreditAccount(user, cardPassword, balance, creditLimit);
            } else {
                System.out.println("银行卡类型无效。");
                return;
            }

            user.addAccount(account);
            userManager.saveData(); // 添加银行卡后保存数据
            System.out.println();
            System.out.println("========== 添加成功 ==========");
            System.out.println("银行卡添加完成。");
            System.out.println("卡号：" + account.getAccountNumber());
            System.out.println("卡类型：" + getAccountTypeName(account));
            System.out.println("当前余额：" + ConsoleUtils.formatMoney(account.getBalance()));
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
        } catch (IllegalArgumentException e) {
            System.out.println("添加失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
        }
    }

    private void useBankCard(BankUser user) {
        if (user.getMyAccounts().isEmpty()) {
            System.out.println("当前没有银行卡，请先添加。");
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
            return;
        }

        printUserCards(user, false);
        String accountNumber = ConsoleUtils.readRequiredText("请输入要使用的卡号：");
        BankAccount account = findAccountByNumber(user, accountNumber);
        if (account == null) {
            System.out.println("未找到该银行卡。");
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
            return;
        }

        boolean using = true;
        while (using) {
            printCardMenu(account);
            int choice = ConsoleUtils.readInt("请选择操作：");
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
                        ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
                    }
                    break;
                case 6:
                    using = false;
                    userManager.saveData(); // 结束使用银行卡后保存数据
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
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码：");
            double amount = ConsoleUtils.readDouble("请输入存款金额：");
            account.deposit(password, amount);
            userManager.saveData(); // 存款后保存数据
            ConsoleUtils.showOperationResult("存款成功。", "当前余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("存款失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void doWithdraw(BankAccount account) {
        try {
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码：");
            double amount = ConsoleUtils.readDouble("请输入取款金额：");
            account.withdraw(password, amount);
            userManager.saveData(); // 取款后保存数据
            ConsoleUtils.showOperationResult("取款成功。", "当前余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("取款失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void changeCardPassword(BankAccount account) {
        String oldPassword = ConsoleUtils.readRequiredText("请输入旧银行卡密码：");
        String newPassword = ConsoleUtils.readRequiredText("请输入新银行卡密码：");
        String confirmPassword = ConsoleUtils.readRequiredText("请再次输入新银行卡密码：");
        boolean success = account.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
        if (success) {
            userManager.saveData(); // 修改密码后保存数据
            ConsoleUtils.showOperationResult("银行卡密码修改成功。", "卡号：" + account.getAccountNumber(),
                    "按回车键返回银行卡操作菜单...");
        } else {
            ConsoleUtils.waitForEnter("银行卡密码未修改，按回车键返回银行卡操作菜单...");
        }
    }

    private void showCardInfo(BankAccount account) {
        System.out.println();
        System.out.println("========== 银行卡信息 ==========");
        System.out.println("卡号：" + account.getAccountNumber());
        System.out.println("持有人ID：" + account.getAccountHolder());
        System.out.println("卡类型：" + getAccountTypeName(account));
        System.out.println("余额：" + ConsoleUtils.formatMoney(account.getBalance()));

        if (account instanceof SavingsAccount) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            System.out.println("利率：" + savingsAccount.getInterestRate());
        } else if (account instanceof CreditAccount) {
            CreditAccount creditAccount = (CreditAccount) account;
            System.out.println("信用额度：" + ConsoleUtils.formatMoney(creditAccount.getCreditLimit()));
            System.out.println("已用额度：" + ConsoleUtils.formatMoney(creditAccount.getUsedCredit()));
            System.out.println("可用额度：" + ConsoleUtils.formatMoney(
                    creditAccount.getBalance() + creditAccount.getCreditLimit() - creditAccount.getUsedCredit()));
        }
        ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
    }

    private void applySavingsInterest(SavingsAccount account) {
        account.applyInterest();
        userManager.saveData(); // 应用利息后保存数据
        ConsoleUtils.showOperationResult("利息处理完成。", "当前余额：" + ConsoleUtils.formatMoney(account.getBalance()),
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

            int choice = ConsoleUtils.readInt("请选择功能：");
            switch (choice) {
                case 1:
                    doOnlinePay(account);
                    break;
                case 2:
                    doRepay(account);
                    break;
                case 3:
                    ConsoleUtils.showOperationResult("美元余额查看完成。",
                            "折算美元余额：" + String.format("%.2f USD", account.convertToUSD()),
                            "按回车键返回信用卡菜单...");
                    break;
                case 4:
                    running = false;
                    userManager.saveData(); // 退出信用卡菜单前保存数据
                    break;
                default:
                    System.out.println("无效选项，请重新输入。");
                    break;
            }
        }
    }

    private void doOnlinePay(CreditAccount account) {
        try {
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码：");
            double amount = ConsoleUtils.readDouble("请输入支付金额：");
            account.payOnline(password, amount);
            userManager.saveData(); // 线上支付后保存数据
            ConsoleUtils.showOperationResult("线上支付成功。", "支付后余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回信用卡菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("支付失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回信用卡菜单...");
        }
    }

    private void doRepay(CreditAccount account) {
        try {
            double amount = ConsoleUtils.readDouble("请输入还款金额：");
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码：");
            account.repay(amount, password);
            userManager.saveData(); // 还款后保存数据
            ConsoleUtils.showOperationResult("还款处理完成。",
                    "当前余额：" + ConsoleUtils.formatMoney(account.getBalance()) + "\n当前已用额度：" +
                            ConsoleUtils.formatMoney(account.getUsedCredit()),
                    "按回车键返回信用卡菜单...");
        } catch (IllegalArgumentException e) {
            System.out.println("还款失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回信用卡菜单...");
        }
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
}
