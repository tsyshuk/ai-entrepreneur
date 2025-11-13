package org.example.service;

import org.example.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    record UserCreateDto(String email, String password, UserRole role) {}
    record UserUpdateDto(String email, String password, UserRole role) {}
    record UserReadDto(Long id, String email, String role, String createdAt) {}

    UserReadDto create(UserCreateDto dto);
    UserReadDto findById(Long id);
    UserReadDto update(Long id, UserUpdateDto dto);
    void delete(Long id);
    Page<UserReadDto> findPage(Pageable pageable, String emailFilter);
}