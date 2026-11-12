package com.ebookstore.auth.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record UserResponseDto(
        Long id,
        String username,
        String email,
        Set<String> roles
) {
}
