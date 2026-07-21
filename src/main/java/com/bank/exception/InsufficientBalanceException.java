package com.bank.exception;

public class InsufficientBalanceException extends BankException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
