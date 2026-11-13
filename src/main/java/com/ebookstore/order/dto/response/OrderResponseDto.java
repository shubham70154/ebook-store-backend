package com.ebookstore.order.dto.response;

import com.ebookstore.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponseDto> items,
        LocalDateTime reservationExpiresAt,
        LocalDateTime createdAt
) {
}
