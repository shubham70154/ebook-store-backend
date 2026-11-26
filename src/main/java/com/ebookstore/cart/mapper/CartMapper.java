package com.ebookstore.cart.mapper;

import com.ebookstore.cart.dto.response.CartItemResponseDto;
import com.ebookstore.cart.dto.response.CartResponseDto;
import com.ebookstore.cart.entity.Cart;
import com.ebookstore.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // CartItem mapping
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "category", source = "book.category")
    @Mapping(target = "coverImageUrl", source = "book.coverImageUrl")
    @Mapping(target = "unitPrice", source = "book.price")
    @Mapping(
            target = "lineTotal",
            expression = "java(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))"
    )
    @Mapping(
            target = "available",
            expression = "java(item.getBook().isActive() && item.getBook().getAvailableQuantity() >= item.getQuantity())"
    )
    CartItemResponseDto toItemResponseDto(CartItem item);

    // Cart mapping
    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "items")
    @Mapping(
            target = "totalQuantity",
            expression = "java(calculateTotalQuantity(cart))"
    )
    @Mapping(
            target = "subtotal",
            expression = "java(calculateSubtotal(cart))"
    )
    CartResponseDto toResponseDto(Cart cart);

    default int calculateTotalQuantity(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
