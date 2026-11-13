package com.ebookstore.order.mapper;

import com.ebookstore.order.dto.response.OrderItemResponseDto;
import com.ebookstore.order.dto.response.OrderResponseDto;
import com.ebookstore.order.entity.Order;
import com.ebookstore.order.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponseDto toResponseDto(Order order);

    OrderItemResponseDto toItemResponseDto(OrderItem orderItem);

    List<OrderResponseDto> toResponseDtoList(List<Order> orders);
}
