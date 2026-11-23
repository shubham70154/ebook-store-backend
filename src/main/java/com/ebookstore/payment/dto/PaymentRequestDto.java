package com.ebookstore.payment.dto;

import com.ebookstore.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
