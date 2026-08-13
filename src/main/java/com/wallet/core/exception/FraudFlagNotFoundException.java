package com.wallet.core.exception;

public class FraudFlagNotFoundException extends RuntimeException {
    public FraudFlagNotFoundException(String message) {
        super(message);
    }
}
