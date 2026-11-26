package com.ebookstore.auth.service.impl;

import com.ebookstore.auth.dto.request.RegisterRequestDto;
import com.ebookstore.auth.dto.response.UserResponseDto;
import com.ebookstore.auth.entity.Role;
import com.ebookstore.auth.entity.RoleName;
import com.ebookstore.auth.entity.User;
import com.ebookstore.auth.mapper.UserMapper;
import com.ebookstore.auth.repository.RoleRepository;
import com.ebookstore.auth.repository.UserRepository;
import com.ebookstore.auth.service.AuthService;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Encode password in the service.
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // Create the customer through the entity factory method.
        User user = User.createCustomer(
                request.getUsername(),
                request.getEmail(),
                encodedPassword,
                customerRole
        );

        User savedUser = userRepository.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    private void validateDuplicateUser(RegisterRequestDto request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(
                    "Username is already registered"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "Email is already registered"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format("User not found with username: %s", username)
                        )
                );

        return userMapper.toResponseDto(user);
    }
}
