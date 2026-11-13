package com.ebookstore.book.repository;

import com.ebookstore.book.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByActiveTrueOrderByCreatedAtDesc();

    Optional<Book> findByIdAndActiveTrue(Long id);

    @Query("""
            SELECT b FROM Book b
            WHERE b.active = true
            AND (
                LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            ORDER BY b.createdAt DESC
            """)
    List<Book> searchActiveBooks(@Param("keyword") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
