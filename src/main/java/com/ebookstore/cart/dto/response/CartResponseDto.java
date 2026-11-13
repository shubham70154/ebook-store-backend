package com.ebookstore.cart.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponseDto(
        Long cartId,
        List<CartItemResponseDto> items,
        int totalQuantity,
        BigDecimal subtotal
) {
}
