package com.bank.interfaces;

// 多币种转换接口
public interface CurrencyConvertible {
    double CNY_TO_USD = 0.14;

    // 多币种转换方法，将余额转换为美元
    double convertToUSD();
}
