package com.ebookstore.payment.service;

import com.ebookstore.payment.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto processPayment(
            String username,
            Long orderId,
            boolean simulateFailure
    );
}
