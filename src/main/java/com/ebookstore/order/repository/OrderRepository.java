package com.ebookstore.order.repository;

import com.ebookstore.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "items")
    @Query("""
            SELECT o FROM Order o
            WHERE o.id = :orderId
            AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUser_IdForUpdate(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );
}
