package com.ebookstore.order.controller;

import com.ebookstore.order.dto.response.OrderResponseDto;
import com.ebookstore.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDto> checkout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        OrderResponseDto response = orderService.checkout(
                authentication.getName(),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(authentication.getName())
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getMyOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrderById(
                        authentication.getName(),
                        orderId
                )
        );
    }
}
