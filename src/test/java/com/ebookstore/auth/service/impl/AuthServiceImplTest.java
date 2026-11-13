package com.ebookstore.auth.service.impl;

import com.ebookstore.auth.dto.request.RegisterRequestDto;
import com.ebookstore.auth.dto.response.UserResponseDto;
import com.ebookstore.exception.ConflictException;
import com.ebookstore.user.entity.Role;
import com.ebookstore.user.entity.RoleName;
import com.ebookstore.user.entity.User;
import com.ebookstore.user.mapper.UserMapper;
import com.ebookstore.user.repository.RoleRepository;
import com.ebookstore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterCustomerSuccessfully() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("shubham");
        request.setEmail("shubham@example.com");
        request.setPassword("password123");

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.CUSTOMER)
                .build();

        UserResponseDto expectedResponse =
                UserResponseDto.builder()
                        .id(1L)
                        .username("shubham")
                        .email("shubham@example.com")
                        .roles(Set.of("CUSTOMER"))
                        .build();

        when(userRepository.existsByUsername("shubham"))
                .thenReturn(false);

        when(userRepository.existsByEmail("shubham@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encrypted-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(expectedResponse);

        UserResponseDto actualResponse =
                authService.register(request);

        assertNotNull(actualResponse);
        assertEquals("shubham", actualResponse.username());
        assertEquals("shubham@example.com", actualResponse.email());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("encrypted-password", savedUser.getPassword());
        assertTrue(savedUser.isEnabled());
        assertEquals(1, savedUser.getRoles().size());

        verify(passwordEncoder).encode("password123");
        verify(userMapper).toResponseDto(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("shubham");
        request.setEmail("shubham@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("shubham"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("newuser");
        request.setEmail("shubham@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("shubham@example.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }
}