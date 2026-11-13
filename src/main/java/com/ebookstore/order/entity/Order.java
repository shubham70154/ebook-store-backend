package com.ebookstore.order.entity;

import com.ebookstore.exception.ConflictException;
import com.ebookstore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "customer_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_user_idempotency_key",
                        columnNames = {"user_id", "idempotency_key"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // Used to identify duplicate checkout requests.
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    // Inventory reservation expiry time.
    @Column(nullable = false)
    private LocalDateTime reservationExpiresAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Order create(
            User user,
            String idempotencyKey,
            LocalDateTime reservationExpiresAt) {

        Order order = new Order();

        order.user = user;
        order.idempotencyKey = idempotencyKey;
        order.reservationExpiresAt = reservationExpiresAt;
        order.status = OrderStatus.PENDING_PAYMENT;
        order.totalAmount = BigDecimal.ZERO;

        return order;
    }

    public void addItem(
            Long bookId,
            String title,
            String author,
            BigDecimal unitPrice,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order item quantity must be greater than zero"
            );
        }

        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Order item price must be valid"
            );
        }

        OrderItem item = new OrderItem(
                this,
                bookId,
                title,
                author,
                unitPrice,
                quantity
        );

        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void markPaid() {
        validatePendingPayment();
        status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        validatePendingPayment();
        status = OrderStatus.PAYMENT_FAILED;
    }

    public void markExpired() {
        validatePendingPayment();
        status = OrderStatus.EXPIRED;
    }

    private void validatePendingPayment() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException(
                    "Order cannot be updated because its status is " + status
            );
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
