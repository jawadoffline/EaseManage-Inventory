package com.easemanage.user.dto;

import com.easemanage.user.entity.Role;
import com.easemanage.user.entity.Status;

public record UpdateUserRequest(
    String email,
    String firstName,
    String lastName,
    Role role,
    Status status
) {}
