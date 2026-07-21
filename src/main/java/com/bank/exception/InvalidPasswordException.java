package com.bank.exception;

public class InvalidPasswordException extends BankException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
