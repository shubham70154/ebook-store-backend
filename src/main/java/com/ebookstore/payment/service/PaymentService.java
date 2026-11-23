package com.ebookstore.payment.service;

import com.ebookstore.payment.dto.PaymentResponseDto;
import com.ebookstore.payment.entity.PaymentMethod;

public interface PaymentService {

    PaymentResponseDto processPayment(
            String username,
            Long orderId,
            PaymentMethod paymentMethod
    );
}
