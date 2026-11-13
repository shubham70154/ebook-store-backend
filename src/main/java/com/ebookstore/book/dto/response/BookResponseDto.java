package com.ebookstore.book.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BookResponseDto(
        Long id,
        String title,
        String author,
        String description,
        BigDecimal price,
        Integer totalQuantity,
        Integer availableQuantity,
        String category,
        String coverImageUrl,
        boolean active,

        LocalDateTime createdAt
) {
}