package com.bank.model;

import com.bank.exception.BankException;
import com.bank.exception.InsufficientBalanceException;

// 储蓄账户类，继承自银行账户类
public class SavingsAccount extends BankAccount {
    private static final long serialVersionUID = 2L;

    private double interestRate;

    // 构造方法
    public SavingsAccount(BankUser user, String accountPassword, double balance, double interestRate) {
        super(user, accountPassword, balance);

        if (interestRate <= 0) {
            throw new BankException("利息率必须大于0！");
        }
        this.interestRate = interestRate;
    }

    // 取款方法，先验证密码再核算余额
    @Override
    public void withdraw(String password, double amount) {
        verifyPassword(password);

        if (amount <= 0) {
            throw new BankException("取款金额必须大于0！");
        } else if (getBalance() - amount < 10) {
            throw new InsufficientBalanceException("余额不足！储蓄卡需至少保留10元底金。当前余额：" + String.format("%.2f", getBalance()) + " 元");
        } else {
            balance -= amount;
        }
    }

    // 应用利息方法
    public void applyInterest() {
        double interest = getBalance() * interestRate;
        if (interest > 0) {
            balance += interest;
            System.out.println("利息已应用，获得利息：" + String.format("%.2f 元", interest));
        }
    }

    // 获取利息率方法
    public double getInterestRate() {
        return interestRate;
    }

    @Override
    public String toString() {
        return "储蓄卡{卡号=" + getAccountNumber()
                + ", 余额=" + String.format("%.2f", getBalance())
                + ", 年利率=" + String.format("%.2f%%", interestRate * 100)
                + ", 状态=" + (isLocked() ? "已锁定" : "正常")
                + "}";
    }
}
