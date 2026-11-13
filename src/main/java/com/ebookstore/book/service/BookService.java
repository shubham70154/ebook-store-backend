package com.ebookstore.book.service;

import com.ebookstore.book.dto.request.BookRequestDto;
import com.ebookstore.book.dto.request.CreateBookRequestDto;
import com.ebookstore.book.dto.response.BookResponseDto;

import java.util.List;

public interface BookService {

    List<BookResponseDto> getAllBooks();

    BookResponseDto getBookById(Long id);

    List<BookResponseDto> searchBooks(String keyword);

    BookResponseDto createBook(CreateBookRequestDto request);

    BookResponseDto updateBook(Long id, BookRequestDto request);
    
    void deactivateBook(Long id);
}
