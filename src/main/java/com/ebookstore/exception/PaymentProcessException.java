package com.ebookstore.exception;

public class PaymentProcessException extends ConflictException {

    public PaymentProcessException(String msg) {
        super(msg);
    }
}
