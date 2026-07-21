package com.bank.ui;

import com.bank.exception.*;
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
            System.out.println("  " + ConsoleUtils.bold("Card Package Wallet"));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("1.", ConsoleUtils.GRAY) + " View bank cards" + ConsoleUtils.color("     (查看银行卡)", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("2.", ConsoleUtils.GRAY) + " Apply for new card" + ConsoleUtils.color("  (申请银行卡)", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("3.", ConsoleUtils.GRAY) + " Select and use card" + ConsoleUtils.color(" (使用银行卡)", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("4.", ConsoleUtils.GRAY) + " Return to profile" + ConsoleUtils.color("   (返回上一级)", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println();

            int choice = ConsoleUtils.readInt("请选择操作");
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
                    userManager.saveData();
                    break;
                default:
                    ConsoleUtils.showError("无效选项，请重新选择。");
                    break;
            }
        }
    }

    public void printUserCards(BankUser user, boolean waitAfterPrint) {
        List<BankAccount> accounts = user.getMyAccounts();
        if (accounts.isEmpty()) {
            ConsoleUtils.showInfo("当前名下没有关联的银行卡。");
            if (waitAfterPrint) {
                ConsoleUtils.waitForEnter("按回车键返回上一级...");
            }
            return;
        }

        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Card Package:"));
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount account = accounts.get(i);
            boolean isSavings = account instanceof SavingsAccount;
            String cardType = isSavings ? "储蓄卡 (Savings)" : "信用卡 (Credit)";
            String status = account.isLocked() ? ConsoleUtils.color("Locked", ConsoleUtils.RED) : "Active";

            System.out.println("  " + (i + 1) + ". " + ConsoleUtils.bold(account.getAccountNumber()) +
                    ConsoleUtils.color(" (" + cardType + ")", ConsoleUtils.GRAY));
            System.out.println("     Balance: " + ConsoleUtils.formatMoney(account.getBalance()));
            if (!isSavings) {
                CreditAccount credit = (CreditAccount) account;
                System.out.println("     Limit:   " + ConsoleUtils.formatMoney(credit.getCreditLimit()) +
                        ConsoleUtils.color(" (Used: " + ConsoleUtils.formatMoney(credit.getUsedCredit()) + ")", ConsoleUtils.GRAY));
            }
            System.out.println("     Status:  " + status);
        }
        if (waitAfterPrint) {
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
        }
    }

    private void addBankCard(BankUser user) {
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Apply For Bank Card"));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("1.", ConsoleUtils.GRAY) + " Savings card" + ConsoleUtils.color(" (储蓄卡)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("2.", ConsoleUtils.GRAY) + " Credit card" + ConsoleUtils.color("  (信用卡)", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println();
        int type = ConsoleUtils.readInt("请选择卡片类型编号");

        if (type != 1 && type != 2) {
            ConsoleUtils.showError("卡片类型无效。");
            return;
        }

        try {
            String cardPassword = ConsoleUtils.readPasswordWithConfirmation("请设定6位数字银行卡密码", "请再次输入银行卡密码");
            double balance = ConsoleUtils.readDouble("请输入初始存入余额");

            BankAccount account;
            if (type == 1) {
                double interestRate = ConsoleUtils.readDouble("请输入利率 (例如 0.02 代表 2% 年利率)");
                account = new SavingsAccount(user, cardPassword, balance, interestRate);
            } else {
                double creditLimit = ConsoleUtils.readDouble("请输入信用额度");
                account = new CreditAccount(user, cardPassword, balance, creditLimit);
            }

            ConsoleUtils.showSpinner("Requesting active card from host gateway...", 800);
            user.addAccount(account);
            userManager.saveData();

            System.out.println();
            System.out.println("  " + ConsoleUtils.ICON_SUCCESS + " " + ConsoleUtils.bold("Card successfully activated"));
            System.out.println("    Card Number:  " + ConsoleUtils.bold(account.getAccountNumber()));
            System.out.println("    Type:         " + getAccountTypeName(account));
            System.out.println("    Init Balance: " + ConsoleUtils.formatMoney(account.getBalance()));
            System.out.println();
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
        } catch (BankException e) {
            ConsoleUtils.showError("开卡失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
        }
    }

    private void useBankCard(BankUser user) {
        if (user.getMyAccounts().isEmpty()) {
            ConsoleUtils.showWarning("当前名下没有关联的银行卡，请先添加银行卡。");
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
            return;
        }

        printUserCards(user, false);
        System.out.println();
        String accountNumber = ConsoleUtils.readRequiredText("请输入要操作的银行卡号");
        BankAccount account = findAccountByNumber(user, accountNumber);
        if (account == null) {
            ConsoleUtils.showError("未找到该卡号，请确认输入是否正确。");
            ConsoleUtils.waitForEnter("按回车键返回卡包管理...");
            return;
        }

        boolean using = true;
        while (using) {
            printCardMenu(account);
            int choice = ConsoleUtils.readInt("请选择操作");
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
                        ConsoleUtils.showInfo("当前银行卡没有扩展功能。");
                        ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
                    }
                    break;
                case 6:
                    using = false;
                    ConsoleUtils.showSpinner("Saving transactional state...", 500);
                    userManager.saveData();
                    break;
                default:
                    ConsoleUtils.showError("无效选项，请重新选择。");
                    break;
            }
        }
    }

    private void printCardMenu(BankAccount account) {
        boolean isSavings = account instanceof SavingsAccount;
        String cardType = isSavings ? "Savings Card" : "Credit Card";
        String lockedTag = account.isLocked() ? ConsoleUtils.color(" [Locked]", ConsoleUtils.RED) : "";

        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Card Management") + ConsoleUtils.color(" (" + account.getAccountNumber() + ")", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println("    Type:         " + cardType + lockedTag);
        System.out.println("    Balance:      " + ConsoleUtils.formatMoney(account.getBalance()));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println("    1. Deposit funds" + ConsoleUtils.color("          (存款)", ConsoleUtils.GRAY));
        System.out.println("    2. Withdraw cash" + ConsoleUtils.color("          (取款)", ConsoleUtils.GRAY));
        System.out.println("    3. Reset card password" + ConsoleUtils.color("    (修改卡密码)", ConsoleUtils.GRAY));
        System.out.println("    4. View active parameters" + ConsoleUtils.color(" (查看详细信息)", ConsoleUtils.GRAY));
        if (account instanceof SavingsAccount) {
            System.out.println("    5. Compute accrual interest" + ConsoleUtils.color(" (计算利息)", ConsoleUtils.GRAY));
        } else if (account instanceof CreditAccount) {
            System.out.println("    5. Extra billing features" + ConsoleUtils.color("   (信用卡管理)", ConsoleUtils.GRAY));
        } else {
            System.out.println("    5. No options available");
        }
        System.out.println("    6. Return to wallet" + ConsoleUtils.color("         (返回上一级)", ConsoleUtils.GRAY));
        System.out.println();
    }

    private void doDeposit(BankAccount account) {
        try {
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码");
            double amount = ConsoleUtils.readDouble("请输入存款金额");
            ConsoleUtils.showSpinner("Processing deposit transaction...", 700);
            account.deposit(password, amount);
            userManager.saveData();
            ConsoleUtils.showOperationResult("存款成功。", "存款金额：" + ConsoleUtils.formatMoney(amount) + "\n当前余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("操作中断：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void doWithdraw(BankAccount account) {
        try {
            String password = ConsoleUtils.readRequiredText("请输入银行卡密码");
            double amount = ConsoleUtils.readDouble("请输入取款金额");
            ConsoleUtils.showSpinner("Authenticating transaction security...", 800);
            account.withdraw(password, amount);
            userManager.saveData();
            ConsoleUtils.showOperationResult("取款成功。", "取款金额：" + ConsoleUtils.formatMoney(amount) + "\n当前余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回银行卡操作菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("操作中断：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void changeCardPassword(BankAccount account) {
        try {
            String oldPassword = ConsoleUtils.readRequiredText("请输入原密码 (6位数字)");
            String newPassword = ConsoleUtils.readRequiredText("请输入新密码 (6位数字)");
            String confirmPassword = ConsoleUtils.readRequiredText("请再次输入新密码");
            ConsoleUtils.showSpinner("Resetting card password keys...", 600);
            boolean success = account.setNewAccountPassword(oldPassword, newPassword, confirmPassword);
            if (success) {
                userManager.saveData();
                ConsoleUtils.showOperationResult("银行卡密码修改成功。", "卡号：" + account.getAccountNumber() + "\n安全状态：密码已更新",
                        "按回车键返回银行卡操作菜单...");
            }
        } catch (BankException e) {
            ConsoleUtils.showError("修改失败：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回银行卡操作菜单...");
        }
    }

    private void showCardInfo(BankAccount account) {
        boolean isSavings = account instanceof SavingsAccount;
        System.out.println();
        System.out.println("  " + ConsoleUtils.bold("Card Details") + ConsoleUtils.color(" (" + account.getAccountNumber() + ")", ConsoleUtils.GRAY));
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println("    Card Number:  " + account.getAccountNumber());
        System.out.println("    Holder ID:    " + account.getAccountHolder());
        System.out.println("    Card Type:    " + getAccountTypeName(account));
        System.out.println("    Card Status:  " + (account.isLocked() ? ConsoleUtils.color("Locked", ConsoleUtils.RED) : "Active"));
        System.out.println("    Balance:      " + ConsoleUtils.formatMoney(account.getBalance()));

        if (account instanceof SavingsAccount) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            System.out.println("    Interest:     " + String.format("%.2f%% (Annual)", savingsAccount.getInterestRate() * 100));
        } else if (account instanceof CreditAccount) {
            CreditAccount creditAccount = (CreditAccount) account;
            System.out.println("    Credit Limit: " + ConsoleUtils.formatMoney(creditAccount.getCreditLimit()));
            System.out.println("    Used Credit:  " + ConsoleUtils.formatMoney(creditAccount.getUsedCredit()));
            double available = creditAccount.getBalance() + creditAccount.getCreditLimit() - creditAccount.getUsedCredit();
            System.out.println("    Available:    " + ConsoleUtils.formatMoney(available));
        }
        System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
        System.out.println();
        ConsoleUtils.waitForEnter("按回车键返回操作菜单...");
    }

    private void applySavingsInterest(SavingsAccount account) {
        ConsoleUtils.showSpinner("Calculating accrued interest...", 800);
        account.applyInterest();
        userManager.saveData();
        ConsoleUtils.showOperationResult("结息处理完成。", "当前余额：" + ConsoleUtils.formatMoney(account.getBalance()) + "\n包含已结付的储蓄卡活期利息",
                "按回车键返回银行卡操作菜单...");
    }

    private void creditCardExtraMenu(CreditAccount account) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("  " + ConsoleUtils.bold("Credit Features") + ConsoleUtils.color(" (" + account.getAccountNumber() + ")", ConsoleUtils.GRAY));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("    Used Credit:  " + ConsoleUtils.formatMoney(account.getUsedCredit()));
            System.out.println("  " + ConsoleUtils.color("────────────────────────────────────────────────────────", ConsoleUtils.GRAY));
            System.out.println("    1. Online simulated pay" + ConsoleUtils.color("      (线上支付)", ConsoleUtils.GRAY));
            System.out.println("    2. Repay billing balance" + ConsoleUtils.color("     (还款)", ConsoleUtils.GRAY));
            System.out.println("    3. Convert balance to USD" + ConsoleUtils.color("    (美元折算)", ConsoleUtils.GRAY));
            System.out.println("    4. Return to card menu" + ConsoleUtils.color("       (返回上一级)", ConsoleUtils.GRAY));
            System.out.println();

            int choice = ConsoleUtils.readInt("请选择功能");
            switch (choice) {
                case 1:
                    doOnlinePay(account);
                    break;
                case 2:
                    doRepay(account);
                    break;
                case 3:
                    ConsoleUtils.showSpinner("Requesting FX rate gateway...", 600);
                    ConsoleUtils.showOperationResult("Exchange rate updated.",
                            "USD Balance: " + String.format("%,.2f USD", account.convertToUSD()) + " (Exchange Rate: 7.24)",
                            "按回车键返回信用卡菜单...");
                    break;
                case 4:
                    running = false;
                    userManager.saveData();
                    break;
                default:
                    ConsoleUtils.showError("无效选项，请重新选择。");
                    break;
            }
        }
    }

    private void doOnlinePay(CreditAccount account) {
        try {
            String password = ConsoleUtils.readRequiredText("请输入信用卡密码");
            double amount = ConsoleUtils.readDouble("请输入在线支付交易金额");
            ConsoleUtils.showSpinner("Evaluating gateway validation...", 800);
            account.payOnline(password, amount);
            userManager.saveData();
            ConsoleUtils.showOperationResult("线上支付扣款成功。", "消费金额：" + ConsoleUtils.formatMoney(amount) + "\n消费后卡内余额：" + ConsoleUtils.formatMoney(account.getBalance()),
                    "按回车键返回信用卡菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("支付中断：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回信用卡菜单...");
        }
    }

    private void doRepay(CreditAccount account) {
        try {
            double amount = ConsoleUtils.readDouble("请输入还款金额");
            String password = ConsoleUtils.readRequiredText("请输入信用卡密码");
            ConsoleUtils.showSpinner("Updating credit balance limits...", 700);
            account.repay(amount, password);
            userManager.saveData();
            ConsoleUtils.showOperationResult("还款成功。",
                    "还款入账: " + ConsoleUtils.formatMoney(amount) + "\n当前余额: " +
                            ConsoleUtils.formatMoney(account.getBalance()) + "\n当前已用额度: " +
                            ConsoleUtils.formatMoney(account.getUsedCredit()),
                    "按回车键返回信用卡菜单...");
        } catch (BankException e) {
            ConsoleUtils.showError("还款中断：" + e.getMessage());
            ConsoleUtils.waitForEnter("按回车键返回信用卡菜单...");
        }
    }

    private BankAccount findAccountByNumber(BankUser user, String accountNumber) {
        for (BankAccount account : user.getMyAccounts()) {
            if (account.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
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
