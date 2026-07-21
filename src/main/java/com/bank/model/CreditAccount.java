package com.bank.model;

import com.bank.exception.BankException;
import com.bank.exception.CreditLimitExceededException;
import com.bank.interfaces.CurrencyConvertible;
import com.bank.interfaces.OnlinePayable;

// 信用卡账户类，继承自银行账户类，实现在线支付接口和多币种转换接口
public class CreditAccount extends BankAccount implements OnlinePayable, CurrencyConvertible {
    private static final long serialVersionUID = 2L;

    private double creditLimit;
    private double usedCredit;

    // 构造方法
    public CreditAccount(BankUser user, String accountPassword, double balance, double creditLimit) {
        super(user, accountPassword, balance);

        if (creditLimit < 0) {
            throw new BankException("信用额度不能小于0！");
        }
        this.creditLimit = creditLimit;
        this.usedCredit = 0;
    }

    // 取款方法，先验证密码再检查可用额度
    @Override
    public void withdraw(String password, double amount) {
        verifyPassword(password);
        double availableFunds = getBalance() + (creditLimit - usedCredit);

        if (amount <= 0) {
            throw new BankException("取款金额必须大于0！");
        } else if (amount > availableFunds) {
            throw new CreditLimitExceededException("超出了可用额度（最高透支可用：" + String.format("%.2f", availableFunds) + "元），无法取款！");
        } else {
            if (amount > getBalance()) {
                usedCredit += amount - getBalance();
                balance = 0;
            } else {
                balance -= amount;
            }
        }
    }

    // 线上支付方法
    @Override
    public void payOnline(String password, double amount) {
        System.out.println("------正在进行线上支付安全校验------");
        System.out.println("支付商户：网购平台");
        withdraw(password, amount);
        System.out.println("------线上支付操作完成------");
    }

    // 还款方法
    public void repay(double amount, String password) {
        verifyPassword(password);

        if (amount <= 0) {
            throw new BankException("还款金额必须大于0！");
        }

        if (amount > usedCredit) {
            double remaining = amount - usedCredit;
            usedCredit = 0;
            balance += remaining;
            System.out.println("还款成功，已用信用额度已清零，剩余还款金转入账户余额！");
        } else {
            usedCredit -= amount;
            System.out.println("还款成功，已用信用额度已更新！");
        }
    }

    // 多币种转换方法，将余额转换为美元
    @Override
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
