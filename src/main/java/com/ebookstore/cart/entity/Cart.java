package com.ebookstore.cart.entity;

import com.ebookstore.book.entity.Book;
import com.ebookstore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Cart(User user) {
        this.user = user;
    }

    public static Cart createFor(User user) {
        return new Cart(user);
    }

    public void addBook(Book book, int quantity) {

        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(this, book, quantity);
            items.add(newItem);
        }

        touch();
    }

    public CartItem getItem(Long itemId) {
        return items.stream()
                .filter(item -> item.getId() != null
                        && item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        touch();
    }

    public void clearItems() {
        items.clear();
        touch();
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
}
