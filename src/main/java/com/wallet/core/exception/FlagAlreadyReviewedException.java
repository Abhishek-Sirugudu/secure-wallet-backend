package com.wallet.core.exception;

public class FlagAlreadyReviewedException extends RuntimeException {
    public FlagAlreadyReviewedException(String message) {
        super(message);
    }
}
