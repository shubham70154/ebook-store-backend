package com.ebookstore.auth.service;

import com.ebookstore.auth.dto.request.RegisterRequestDto;
import com.ebookstore.auth.dto.response.UserResponseDto;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto getCurrentUser(String username);

}
