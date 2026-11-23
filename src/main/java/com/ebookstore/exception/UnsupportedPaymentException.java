package com.ebookstore.exception;

public class UnsupportedPaymentException extends ConflictException {

    public UnsupportedPaymentException(String msg) {
        super(msg);
    }
}
