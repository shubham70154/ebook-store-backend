package com.ebookstore.payment.service.strategy.impl;

import com.ebookstore.order.entity.Order;
import com.ebookstore.payment.entity.PaymentMethod;
import com.ebookstore.payment.service.strategy.PaymentProcessingResult;
import com.ebookstore.payment.service.strategy.PaymentStrategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DebitCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public PaymentProcessingResult process(Order order) {

        log.info(
                "Processing debit card payment for order {}. Amount: {}",
                order.getId(),
                order.getTotalAmount()
        );

        log.info("Debit card payment successful for order {}", order.getId());

        return PaymentProcessingResult.successful(
                PaymentMethod.DEBIT_CARD
        );
    }
}
