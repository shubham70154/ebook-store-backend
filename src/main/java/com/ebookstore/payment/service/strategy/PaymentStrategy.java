package com.ebookstore.payment.service.strategy;

import com.ebookstore.order.entity.Order;
import com.ebookstore.payment.entity.PaymentMethod;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentProcessingResult process(Order order);
}
