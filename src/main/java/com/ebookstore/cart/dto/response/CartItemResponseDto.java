package com.ebookstore.cart.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemResponseDto(
        Long id,
        Long bookId,
        String title,
        String author,
        String category,
        String coverImageUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        boolean available
) {
}
