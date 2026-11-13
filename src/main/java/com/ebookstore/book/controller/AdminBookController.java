package com.ebookstore.book.controller;

import com.ebookstore.book.dto.request.BookRequestDto;
import com.ebookstore.book.dto.request.CreateBookRequestDto;
import com.ebookstore.book.dto.response.BookResponseDto;
import com.ebookstore.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(
            @Valid @RequestBody CreateBookRequestDto request) {

        BookResponseDto response = bookService.createBook(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDto request) {

        return ResponseEntity.ok(
                bookService.updateBook(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateBook(
            @PathVariable Long id) {

        bookService.deactivateBook(id);

        return ResponseEntity.noContent().build();
    }
}
