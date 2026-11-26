package com.ebookstore.payment.service.strategy;

import com.ebookstore.payment.entity.PaymentMethod;
import com.ebookstore.payment.entity.PaymentStatus;

import java.util.UUID;

public record PaymentProcessingResult(
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionReference
) {

    public static PaymentProcessingResult successful(
            PaymentMethod paymentMethod
    ) {
        return new PaymentProcessingResult(
                PaymentStatus.SUCCESS,
                paymentMethod,
                String.format("DEMO-%s", UUID.randomUUID())
        );
    }
}
