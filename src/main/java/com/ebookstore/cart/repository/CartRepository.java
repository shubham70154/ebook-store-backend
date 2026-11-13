package com.ebookstore.cart.repository;

import com.ebookstore.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.book"})
    Optional<Cart> findByUser_Id(Long userId);
}
