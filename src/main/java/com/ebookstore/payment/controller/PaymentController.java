package com.ebookstore.payment.controller;

import com.ebookstore.payment.dto.PaymentResponseDto;
import com.ebookstore.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "false") boolean simulateFailure,
            Authentication authentication) {

        return ResponseEntity.ok(
                paymentService.processPayment(
                        authentication.getName(),
                        orderId,
                        simulateFailure
                )
        );
    }
}