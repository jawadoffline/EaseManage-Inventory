package com.easemanage.user.dto;

import com.easemanage.user.entity.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull Role role
) {}
