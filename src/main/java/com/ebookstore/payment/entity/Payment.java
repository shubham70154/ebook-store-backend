package com.ebookstore.payment.entity;

import com.ebookstore.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_order",
                        columnNames = "order_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, unique = true, length = 100)
    private String transactionReference;

    private LocalDateTime createdAt;

    public static Payment create(
            Order order,
            PaymentStatus status) {

        Payment payment = new Payment();

        payment.order = order;
        payment.amount = order.getTotalAmount();
        payment.status = status;
        payment.paymentMethod = "DUMMY";
        payment.transactionReference =
                "DUMMY-" + UUID.randomUUID();

        return payment;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}