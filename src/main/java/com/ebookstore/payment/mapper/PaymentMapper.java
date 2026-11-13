package com.ebookstore.payment.mapper;

import com.ebookstore.payment.dto.PaymentResponseDto;
import com.ebookstore.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponseDto toResponseDto(Payment payment);
}
