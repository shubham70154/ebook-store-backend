package com.ebookstore.order.service;

import com.ebookstore.order.dto.response.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto checkout(String username, String idempotencyKey);

    List<OrderResponseDto> getMyOrders(String username);

    OrderResponseDto getMyOrderById(String username, Long orderId);
}
