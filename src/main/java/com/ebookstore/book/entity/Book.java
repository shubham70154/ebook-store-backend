package com.ebookstore.book.entity;

import com.ebookstore.exception.ConflictException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 500)
    private String coverImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    // Checks whether the requested quantity is available.
    public boolean hasAvailableQuantity(int quantity) {
        return quantity > 0
                && availableQuantity != null
                && availableQuantity >= quantity;
    }

    // Reserves book quantity during checkout.
    public void reserveQuantity(int quantity) {

        if (!hasAvailableQuantity(quantity)) {
            throw new ConflictException(
                    "Insufficient quantity available for book: " + title
            );
        }

        availableQuantity -= quantity;
    }

    // Releases reserved quantity if payment fails or an order expires.
    public void releaseQuantity(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity to release must be greater than zero"
            );
        }

        if (totalQuantity == null
                || availableQuantity == null
                || availableQuantity + quantity > totalQuantity) {

            throw new ConflictException(
                    "Released quantity exceeds total book quantity"
            );
        }

        availableQuantity += quantity;
    }

    // Updates total inventory while preserving existing reservations.
    public void updateTotalQuantity(int newTotalQuantity) {

        if (newTotalQuantity < 0) {
            throw new IllegalArgumentException(
                    "Total quantity cannot be negative"
            );
        }

        if (totalQuantity == null || availableQuantity == null) {
            throw new IllegalStateException(
                    "Book inventory has not been initialized"
            );
        }

        int reservedQuantity = totalQuantity - availableQuantity;

        if (newTotalQuantity < reservedQuantity) {
            throw new ConflictException(
                    "Total quantity cannot be less than currently reserved quantity"
            );
        }

        this.totalQuantity = newTotalQuantity;
        this.availableQuantity = newTotalQuantity - reservedQuantity;
    }

    // Updates book details without changing inventory.
    public void updateDetails(
            String title,
            String author,
            String description,
            BigDecimal price,
            String category,
            String coverImageUrl) {

        this.title = title;
        this.author = author;
        this.description = description;
        this.price = price;
        this.category = category;
        this.coverImageUrl = coverImageUrl;
    }

    // Soft delete: book remains in the database but is no longer active.
    public void deactivate() {
        this.active = false;
    }
}
