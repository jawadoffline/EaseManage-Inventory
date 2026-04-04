package com.easemanage.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    UserInfo user
) {
    public record UserInfo(Long id, String username, String email, String firstName, String lastName, String role) {}
}
