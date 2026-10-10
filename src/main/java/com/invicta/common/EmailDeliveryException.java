package com.invicta.common;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(Throwable cause) {
        super("Verification email could not be sent", cause);
    }
}
