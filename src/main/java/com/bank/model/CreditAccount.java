package com.bank.model;

import com.bank.interfaces.CurrencyConvertible;
import com.bank.interfaces.OnlinePayable;

import java.io.Serializable;
import java.util.Objects;

// 信用卡账户类，继承自银行账户类，实现在线支付接口和多币种转换接口
public class CreditAccount extends BankAccount implements OnlinePayable, CurrencyConvertible, Serializable {
    private static final long serialVersionUID = 1L;
    
    private double creditLimit;
    private double usedCredit;

    // 构造方法
    public CreditAccount(BankUser user, String accountPassword, double balance, double creditLimit) {
        super(user, accountPassword, balance);

        if (creditLimit < 0) {
            throw new IllegalArgumentException("信用额度不能小于0!请输入正确的信用额度！");
        }
        this.creditLimit = creditLimit;
    }

    // 取款方法，先输入密码再输入金额
    public void withdraw(String password, double amount) {
        if (!Objects.equals(password, getAccountPassword())) {
            throw new IllegalArgumentException("密码错误!");
        }
        double availableFunds = getBalance() + (creditLimit - usedCredit);

        if (amount <= 0) {
            throw new IllegalArgumentException("取款金额不能小于0!");
        } else if (amount > availableFunds) {
            throw new IllegalArgumentException("超出了可用额度，无法取款!");
        } else {
            if (amount > getBalance()) {
                usedCredit += amount - getBalance();
                balance = 0;
            } else {
                balance -= amount;
            }
        }
    }

    // 线上支付方法，先输入密码再输入金额
    public void payOnline(String password, double amount) {
        System.out.println("------正在进行线上支付安全校验------");
        System.out.println("支付商户：网购平台");
        withdraw(password, amount);
        System.out.println("------线上支付操作完成------");
    }

    // 还款方法，先输入密码再输入金额
    public void repay(double amount, String password) {
        if (!Objects.equals(password, getAccountPassword())) {
            throw new IllegalArgumentException("密码错误!");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("还款金额必须大于0!");
        } else if (amount > usedCredit) {
            deposit(password, amount - usedCredit);
            usedCredit = 0;
            System.out.println("还款成功，已用信用额度已清零，余额已更新!");
        } else {
            usedCredit -= amount;
            System.out.println("还款成功，已用信用额度已更新!");
        }
    }

    // 多币种转换方法，将余额转换为美元
    public double convertToUSD() {
        return getBalance() * CurrencyConvertible.CNY_TO_USD;
    }

    // 获取信用额度
    public double getCreditLimit() {
        return creditLimit;
    }

    // 获取已用信用额度
    public double getUsedCredit() {
        return usedCredit;
    }
}
