package com.ebookstore.book.service.impl;

import com.ebookstore.book.dto.request.BookRequestDto;
import com.ebookstore.book.dto.request.CreateBookRequestDto;
import com.ebookstore.book.dto.response.BookResponseDto;
import com.ebookstore.book.entity.Book;
import com.ebookstore.book.mapper.BookMapper;
import com.ebookstore.book.repository.BookRepository;
import com.ebookstore.book.service.BookService;
import com.ebookstore.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> getAllBooks() {

        return bookRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(bookMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDto getBookById(Long id) {

        Book book = bookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active book not found with id: " + id
                        )
                );

        return bookMapper.toResponseDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> searchBooks(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllBooks();
        }

        return bookRepository.searchActiveBooks(keyword.trim())
                .stream()
                .map(bookMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public BookResponseDto createBook(CreateBookRequestDto request) {

        Book book = Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .category(request.getCategory().trim())
                .coverImageUrl(request.getCoverImageUrl())
                .active(true)
                .build();

        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponseDto(savedBook);
    }

    @Override
    @Transactional
    public BookResponseDto updateBook(Long id, BookRequestDto request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + id
                        )
                );

        book.updateDetails(
                request.getTitle().trim(),
                request.getAuthor().trim(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory().trim(),
                request.getCoverImageUrl()
        );

        return bookMapper.toResponseDto(book);
    }

    @Override
    @Transactional
    public void deactivateBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + id
                        )
                );

        book.deactivate();
    }
}
