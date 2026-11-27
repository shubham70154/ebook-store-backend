package com.ebookstore.cart.service.impl;

import com.ebookstore.auth.entity.User;
import com.ebookstore.auth.repository.UserRepository;
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
import com.ebookstore.exception.CartProcessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public CartResponseDto addItem(String username, AddCartItemRequestDto request) {

        Cart cart = getOrCreateCart(username);

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() ->
                        new CartProcessException(
                                String.format("Book not found with id: %d", request.getBookId())
                        )
                );

        if (!book.isActive()) {
            throw new CartProcessException(
                    "This book is currently unavailable"
            );
        }

        cart.addBook(book, request.getQuantity());

        cartRepository.saveAndFlush(cart);
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
            throw new CartProcessException(
                    String.format("Cart item not found with id: %d", itemId)
            );
        }

        if (!cartItem.getBook().isActive()) {
            throw new CartProcessException(
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
            throw new CartProcessException(
                    String.format("Cart item not found with id: %d", itemId)
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
                        new UsernameNotFoundException(
                                String.format("User not found: %s", username)
                        )
                );

        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(() ->
                        cartRepository.save(Cart.createFor(user))
                );
    }
}