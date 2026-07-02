package com.bank.model;

import java.io.Serializable;
import java.util.Objects;

// 储蓄账户类，继承自银行账户类
public class SavingsAccount extends BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double interestRate;

    // 构造方法
    public SavingsAccount(BankUser user, String accountPassword, double balance, double interestRate) {
        super(user, accountPassword, balance);

        if (interestRate <= 0) {
            throw new IllegalArgumentException("利息率不能小于等于0!请输入正确的利息率！");
        }
        this.interestRate = interestRate;
    }

    // 取款方法，先输入密码再输入金额
    public void withdraw(String password, double amount) {
        if (!Objects.equals(password, getAccountPassword())) {
            throw new IllegalArgumentException("密码错误!");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("取款金额不能小于0!");
        } else if (getBalance() - amount < 10) {
            throw new IllegalArgumentException("余额不足10元，无法取款!");
        } else {
            balance -= amount;
        }
    }

    // 应用利息方法
    public void applyInterest() {
        double interest = getBalance() * interestRate;
        if (interest > 0) {
            deposit(getAccountPassword(), interest);
            System.out.println("利息已应用，获得利息：" + interest);
        }
    }

    // 获取利息率方法
    public double getInterestRate() {
        return interestRate;
    }
}
