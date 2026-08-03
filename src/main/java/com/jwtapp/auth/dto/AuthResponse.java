package com.jwtapp.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInMs
) {
}
