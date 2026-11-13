package com.ebookstore.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Long bookId,
        String title,
        String author,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
