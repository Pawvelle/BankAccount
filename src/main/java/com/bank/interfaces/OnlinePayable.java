package com.bank.interfaces;

// 线上支付接口
public interface OnlinePayable {
    // 线上支付方法，先输入密码再输入金额
    void payOnline(String password, double amount);
}
