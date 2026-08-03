package com.bank.interfaces;

// 多币种转换接口
public interface CurrencyConvertible {
    // 1 CNY = 0.14 USD（即 1 USD ≈ 7.14 CNY），与 UI 文案保持一致
    double CNY_TO_USD = 0.14;
    // 反向展示汇率（1 USD 兑多少 CNY），与常量 CNY_TO_USD = 1/7.14 ≈ 0.14 对应
    double USD_TO_CNY = 7.14;

    // 多币种转换方法，将余额转换为美元
    double convertToUSD();
}
