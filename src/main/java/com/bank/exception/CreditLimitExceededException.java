package com.bank.exception;

public class CreditLimitExceededException extends BankException {
    public CreditLimitExceededException(String message) {
        super(message);
    }
}
