package com.ebookstore.cart.service.impl;

import com.ebookstore.book.entity.Book;
import com.ebookstore.book.repository.BookRepository;
import com.ebookstore.cart.dto.request.AddCartItemRequestDto;
import com.ebookstore.cart.dto.request.UpdateCartItemRequestDto;
import com.ebookstore.cart.dto.response.CartResponseDto;
import com.ebookstore.cart.entity.Cart;
import com.ebookstore.cart.entity.CartItem;
import com.ebookstore.cart.mapper.CartMapper;
import com.ebookstore.cart.repository.CartRepository;
import com.ebookstore.cart.service.CartService;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.exception.ResourceNotFoundException;
import com.ebookstore.user.entity.User;
import com.ebookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponseDto getCart(String username) {

        Cart cart = getOrCreateCart(username);

        return cartMapper.toResponseDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addItem(
            String username,
            AddCartItemRequestDto request) {

        Cart cart = getOrCreateCart(username);

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + request.getBookId()
                        )
                );

        if (!book.isActive()) {
            throw new ConflictException(
                    "This book is currently unavailable"
            );
        }

        cart.addBook(book, request.getQuantity());

        return cartMapper.toResponseDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto updateItemQuantity(
            String username,
            Long itemId,
            UpdateCartItemRequestDto request) {

        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cart.getItem(itemId);

        if (cartItem == null) {
            throw new ResourceNotFoundException(
                    "Cart item not found with id: " + itemId
            );
        }

        if (!cartItem.getBook().isActive()) {
            throw new ConflictException(
                    "This book is currently unavailable"
            );
        }

        cartItem.updateQuantity(request.getQuantity());
        cart.touch();

        return cartMapper.toResponseDto(cart);
    }

    @Override
    @Transactional
    public void removeItem(String username, Long itemId) {

        Cart cart = getOrCreateCart(username);

        CartItem cartItem = cart.getItem(itemId);

        if (cartItem == null) {
            throw new ResourceNotFoundException(
                    "Cart item not found with id: " + itemId
            );
        }

        cart.removeItem(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(String username) {

        Cart cart = getOrCreateCart(username);

        cart.clearItems();
    }

    private Cart getOrCreateCart(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(() ->
                        cartRepository.save(Cart.createFor(user))
                );
    }
}
