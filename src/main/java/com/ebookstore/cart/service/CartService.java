package com.ebookstore.cart.service;

import com.ebookstore.cart.dto.request.AddCartItemRequestDto;
import com.ebookstore.cart.dto.request.UpdateCartItemRequestDto;
import com.ebookstore.cart.dto.response.CartResponseDto;

public interface CartService {

    CartResponseDto getCart(String username);

    CartResponseDto addItem(
            String username,
            AddCartItemRequestDto request
    );

    CartResponseDto updateItemQuantity(
            String username,
            Long itemId,
            UpdateCartItemRequestDto request
    );

    void removeItem(String username, Long itemId);

    void clearCart(String username);
}
