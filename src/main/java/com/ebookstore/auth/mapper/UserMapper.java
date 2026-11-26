package com.ebookstore.auth.mapper;

import com.ebookstore.auth.dto.response.UserResponseDto;
import com.ebookstore.auth.entity.Role;
import com.ebookstore.auth.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponseDto toResponseDto(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}