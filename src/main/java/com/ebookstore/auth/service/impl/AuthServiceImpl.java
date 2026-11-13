package com.ebookstore.auth.service.impl;

import com.ebookstore.auth.dto.request.RegisterRequestDto;
import com.ebookstore.auth.dto.response.UserResponseDto;
import com.ebookstore.auth.service.AuthService;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.exception.ResourceNotFoundException;
import com.ebookstore.user.entity.Role;
import com.ebookstore.user.entity.RoleName;
import com.ebookstore.user.entity.User;
import com.ebookstore.user.mapper.UserMapper;
import com.ebookstore.user.repository.RoleRepository;
import com.ebookstore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {

        validateDuplicateUser(request);

        Role customerRole = roleRepository
                .findByName(RoleName.CUSTOMER)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CUSTOMER role is not configured"
                        )
                );

        User user = buildCustomer(request, customerRole);

        User savedUser = userRepository.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    private void validateDuplicateUser(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(
                    "Username is already registered"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "Email is already registered"
            );
        }
    }

    private User buildCustomer(
            RegisterRequestDto request,
            Role customerRole) {

        return User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return userMapper.toResponseDto(user);
    }

}