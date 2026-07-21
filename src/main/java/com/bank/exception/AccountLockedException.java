package com.bank.exception;

public class AccountLockedException extends BankException {
    public AccountLockedException(String message) {
        super(message);
    }
}
