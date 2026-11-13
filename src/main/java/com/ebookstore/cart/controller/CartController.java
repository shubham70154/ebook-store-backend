package com.ebookstore.cart.controller;

import com.ebookstore.cart.dto.request.AddCartItemRequestDto;
import com.ebookstore.cart.dto.request.UpdateCartItemRequestDto;
import com.ebookstore.cart.dto.response.CartResponseDto;
import com.ebookstore.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequestDto request) {

        return ResponseEntity.ok(
                cartService.addItem(authentication.getName(), request)
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {

        return ResponseEntity.ok(
                cartService.updateItemQuantity(
                        authentication.getName(),
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {

        cartService.removeItem(authentication.getName(), itemId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            Authentication authentication) {

        cartService.clearCart(authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
