package org.example.dto;

// API-запрос на создание пользователя
public record UserCreateRequest(
        String email,
        String password,
        String role // "ADMIN" / "USER" (необязательно)
) {}
