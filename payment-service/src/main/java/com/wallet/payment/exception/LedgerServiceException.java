package com.wallet.payment.exception;

public class LedgerServiceException extends RuntimeException {

    public LedgerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
