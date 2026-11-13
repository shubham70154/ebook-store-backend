package com.ebookstore.book.controller;

import com.ebookstore.book.dto.response.BookResponseDto;
import com.ebookstore.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {

        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(
            @PathVariable Long id) {

        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDto>> searchBooks(
            @RequestParam(defaultValue = "") String keyword) {

        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }
}
