package com.ebookstore.payment.service.impl;

import com.ebookstore.auth.entity.User;
import com.ebookstore.auth.repository.UserRepository;
import com.ebookstore.book.entity.Book;
import com.ebookstore.book.repository.BookRepository;
import com.ebookstore.exception.PaymentProcessException;
import com.ebookstore.exception.UnsupportedPaymentException;
import com.ebookstore.order.entity.Order;
import com.ebookstore.order.entity.OrderStatus;
import com.ebookstore.order.repository.OrderRepository;
import com.ebookstore.payment.dto.PaymentResponseDto;
import com.ebookstore.payment.entity.Payment;
import com.ebookstore.payment.entity.PaymentMethod;
import com.ebookstore.payment.entity.PaymentStatus;
import com.ebookstore.payment.repository.PaymentRepository;
import com.ebookstore.payment.service.PaymentService;
import com.ebookstore.payment.service.strategy.PaymentProcessingResult;
import com.ebookstore.payment.service.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final List<PaymentStrategy> paymentStrategies;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(
            String username,
            Long orderId,
            PaymentMethod paymentMethod
    ) {

        // 1. Find authenticated customer.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new PaymentProcessException("User not found")
                );

        // 2. Lock order to prevent concurrent payment processing.
        Order order = orderRepository
                .findByIdAndUser_IdForUpdate(orderId, user.getId())
                .orElseThrow(() ->
                        new PaymentProcessException(
                                String.format("Order not found: %d", orderId)
                        )
                );


        // 3. Return existing payment if already processed.
        var existingPayment = paymentRepository.findByOrder_Id(orderId);

        if (existingPayment.isPresent()) {
            log.info("Payment already processed for order {}", orderId);

            return toResponseDto(existingPayment.get());
        }

        // 4. Only pending orders can be paid.
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new PaymentProcessException(
                    "Payment cannot be processed for order status: "
                            + order.getStatus()
            );
        }

        // 5. Check reservation expiry.
        if (!LocalDateTime.now().isBefore(
                order.getReservationExpiresAt())) {

            throw new PaymentProcessException(
                    "Payment reservation has expired"
            );
        }

        // 6. Find the selected payment strategy.
        PaymentStrategy strategy = paymentStrategies.stream()
                .filter(s -> s.getPaymentMethod() == paymentMethod)
                .findFirst()
                .orElseThrow(() ->
                        new UnsupportedPaymentException(
                                String.format("Unsupported payment method: %s", paymentMethod)
                        )
                );

        // 7. Delegate payment processing to the strategy.
        PaymentProcessingResult result = strategy.process(order);

        // 8. Update order after successful payment. markPaymentFailed() will also release the reserved quantity of the book.
        if (result.status() == null
                || result.status() != PaymentStatus.SUCCESS) {

            order.markPaymentFailed();
            order.getItems().forEach(item -> {

                Book book = bookRepository.findByIdForUpdate(item.getBookId())
                        .orElseThrow(() ->
                                new PaymentProcessException(
                                        "Book not found: " + item.getBookId()
                                )
                        );

                book.releaseQuantity(item.getQuantity());
            });

            throw new PaymentProcessException(
                    "Payment was not successful"
            );
        }

        order.markPaid();

        log.info(
                "Order {} paid successfully using {}",
                orderId,
                paymentMethod
        );

        // 9. Save payment record.
        Payment payment = Payment.create(
                order,
                result.status(),
                result.paymentMethod(),
                result.transactionReference()
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 10. Return response.
        return toResponseDto(savedPayment);
    }

    private PaymentResponseDto toResponseDto(Payment payment) {

        return new PaymentResponseDto(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getCreatedAt()
        );
    }
}
