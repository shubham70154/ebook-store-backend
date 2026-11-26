package com.ebookstore.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // object creation responsibility: create a customer user
    public static User createCustomer(
            String username,
            String email,
            String encodedPassword,
            Role customerRole
    ) {
        if (customerRole == null) {
            throw new IllegalArgumentException("Customer role cannot be null");
        }

        return User.builder()
                .username(username.trim())
                .email(email.trim().toLowerCase())
                .password(encodedPassword)
                .enabled(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
