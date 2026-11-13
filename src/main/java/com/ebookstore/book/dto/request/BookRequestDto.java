package com.ebookstore.book.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookRequestDto {

    @NotBlank(message = "Book title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Author name is required")
    @Size(max = 150)
    private String author;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Book price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2,
            message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotBlank(message = "Book category is required")
    @Size(max = 100)
    private String category;

    @Size(max = 500)
    private String coverImageUrl;
}
