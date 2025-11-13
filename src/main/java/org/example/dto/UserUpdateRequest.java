package org.example.dto;

// API-запрос на обновление пользователя (всё опционально)
public record UserUpdateRequest(
        String email,
        String password,
        String role
) {}
