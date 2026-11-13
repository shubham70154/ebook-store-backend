package com.ebookstore.book.mapper;

import com.ebookstore.book.dto.response.BookResponseDto;
import com.ebookstore.book.entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponseDto toResponseDto(Book book);
}
