package org.example.dto;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}