package com.aditi.backendcapstoneproject.exception;

public class StripePaymentException extends Exception {
    public StripePaymentException(String message) {
        super(message);
    }

    public StripePaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
