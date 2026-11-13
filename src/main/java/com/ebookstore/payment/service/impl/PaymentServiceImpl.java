package com.ebookstore.payment.service.impl;

import com.ebookstore.book.entity.Book;
import com.ebookstore.book.repository.BookRepository;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.exception.ResourceNotFoundException;
import com.ebookstore.order.entity.Order;
import com.ebookstore.order.entity.OrderItem;
import com.ebookstore.order.entity.OrderStatus;
import com.ebookstore.order.repository.OrderRepository;
import com.ebookstore.payment.dto.PaymentResponseDto;
import com.ebookstore.payment.entity.Payment;
import com.ebookstore.payment.entity.PaymentStatus;
import com.ebookstore.payment.repository.PaymentRepository;
import com.ebookstore.payment.service.PaymentService;
import com.ebookstore.user.entity.User;
import com.ebookstore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(
            String username,
            Long orderId,
            boolean simulateFailure) {

        // 1. Find authenticated customer.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Lock the order to prevent concurrent payment processing.
        Order order = orderRepository
                .findByIdAndUser_IdForUpdate(orderId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        ));

        // 3. Check whether payment was already processed.
        var existingPayment = paymentRepository.findByOrder_Id(orderId);

        if (existingPayment.isPresent()) {
            log.info("Payment already processed for order {}", orderId);

            return toResponseDto(existingPayment.get());
        }

        // 4. Only pending orders can be paid.
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException(
                    "Payment cannot be processed for order status: "
                            + order.getStatus()
            );
        }

        // 5. Reject payment if the reservation has expired.
        // The scheduled expiry process will release the inventory.
        if (!LocalDateTime.now().isBefore(
                order.getReservationExpiresAt())) {

            throw new ConflictException(
                    "Payment reservation has expired"
            );
        }

        // 6. Simulate payment result.
        PaymentStatus paymentStatus = simulateFailure
                ? PaymentStatus.FAILED
                : PaymentStatus.SUCCESS;

        if (paymentStatus == PaymentStatus.SUCCESS) {

            // Successful payment.
            order.markPaid();

            log.info(
                    "Payment successful for order {}",
                    orderId
            );

        } else {

            // Failed payment.
            order.markPaymentFailed();

            // Restore previously reserved book quantities.
            releaseReservedInventory(order);

            log.warn(
                    "Payment failed for order {}. Inventory released.",
                    orderId
            );
        }

        // 7. Create and save payment record.
        Payment payment = Payment.create(
                order,
                paymentStatus
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 8. Return payment response.
        return toResponseDto(savedPayment);
    }

    private void releaseReservedInventory(Order order) {

        // Lock books in ascending ID order to reduce deadlock risk.
        List<OrderItem> orderItems = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getBookId))
                .toList();

        for (OrderItem item : orderItems) {

            Book book = bookRepository
                    .findByIdForUpdate(item.getBookId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Book not found: " + item.getBookId()
                            ));

            book.releaseQuantity(item.getQuantity());
        }
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
