package com.ebookstore.payment.dto;

import com.ebookstore.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String paymentMethod,
        String transactionReference,
        LocalDateTime createdAt
) {
}