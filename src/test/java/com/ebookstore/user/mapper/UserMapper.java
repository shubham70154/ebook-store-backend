package com.ebookstore.user.mapper;

import com.ebookstore.auth.dto.response.UserResponseDto;
import com.ebookstore.user.entity.Role;
import com.ebookstore.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponseDto toResponseDto(User user);

    default Set<String> mapRoles(Set<Role> roles) {

        return roles.stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toSet());

    }
}
