package com.ebookstore.order.service.impl;

import com.ebookstore.book.entity.Book;
import com.ebookstore.book.repository.BookRepository;
import com.ebookstore.cart.entity.Cart;
import com.ebookstore.cart.entity.CartItem;
import com.ebookstore.cart.repository.CartRepository;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.exception.ResourceNotFoundException;
import com.ebookstore.order.dto.response.OrderResponseDto;
import com.ebookstore.order.entity.Order;
import com.ebookstore.order.mapper.OrderMapper;
import com.ebookstore.order.repository.OrderRepository;
import com.ebookstore.order.service.OrderService;
import com.ebookstore.user.entity.User;
import com.ebookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int RESERVATION_MINUTES = 15;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDto checkout(
            String username,
            String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        // 1. Lock the customer row to serialize their checkout requests.
        User user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Check whether this checkout request was already processed.
        var existingOrder = orderRepository
                .findByUser_IdAndIdempotencyKey(
                        user.getId(),
                        idempotencyKey
                );

        if (existingOrder.isPresent()) {
            return orderMapper.toResponseDto(existingOrder.get());
        }

        // 3. Load the customer's cart.
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() ->
                        new ConflictException("Your cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new ConflictException("Your cart is empty");
        }

        // 4. Sort cart items by book ID to reduce deadlock risk
        // when multiple books need to be locked.
        List<CartItem> cartItems = cart.getItems().stream()
                .sorted((first, second) ->
                        Long.compare(
                                first.getBook().getId(),
                                second.getBook().getId()
                        ))
                .toList();

        // 5. Create the order. It will be saved in the same transaction.
        Order order = Order.create(
                user,
                idempotencyKey,
                LocalDateTime.now().plusMinutes(RESERVATION_MINUTES)
        );

        // 6. Lock each book, validate inventory and reserve quantity.
        for (CartItem cartItem : cartItems) {

            Long bookId = cartItem.getBook().getId();
            int quantity = cartItem.getQuantity();

            Book book = bookRepository.findByIdForUpdate(bookId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Book not found: " + bookId
                            ));

            if (!book.isActive()) {
                throw new ConflictException(
                        "Book is no longer available: " + book.getTitle()
                );
            }

            book.reserveQuantity(quantity);

            // Save a snapshot of book details and price in the order.
            order.addItem(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPrice(),
                    quantity
            );
        }

        // 7. Save the order and its items.
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 8. Clear the cart. This is committed only if the whole
        // checkout transaction succeeds.
        cart.clearItems();

        // 9. Return the response.
        return orderMapper.toResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return orderMapper.toResponseDtoList(
                orderRepository.findByUser_IdOrderByCreatedAtDesc(
                        user.getId()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getMyOrderById(
            String username,
            Long orderId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndUser_Id(
                        orderId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        ));

        return orderMapper.toResponseDto(order);
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {

            throw new IllegalArgumentException(
                    "A valid Idempotency-Key header is required"
            );
        }
    }


}
