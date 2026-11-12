package com.ebookstore.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50,
            message = "Username must be between 3 and 50 characters")
    private String userName;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100)
    private String email;

}
