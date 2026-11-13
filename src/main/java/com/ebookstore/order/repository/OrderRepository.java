package com.ebookstore.order.repository;

import com.ebookstore.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUser_IdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    @EntityGraph(attributePaths = "items")
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUser_Id(Long orderId, Long userId);
}
