package org.example.controller;

import org.example.domain.UserRole;
import org.example.dto.UserCreateRequest;
import org.example.dto.UserResponse;
import org.example.dto.UserUpdateRequest;
import org.example.service.UserService;
import org.example.service.UserService.UserCreateDto;
import org.example.service.UserService.UserReadDto;
import org.example.service.UserService.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService service;

    public UserRestController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserResponse> list(Pageable pageable,
                                   @RequestParam(value = "email", required = false) String emailFilter) {
        Page<UserReadDto> page = service.findPage(pageable, emailFilter);
        return page.map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        UserReadDto dto = service.findById(id);
        return ResponseEntity.ok(toResponse(dto));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody UserCreateRequest body) {
        UserCreateDto dto = toCreateDto(body);
        return toResponse(service.create(dto));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody UserUpdateRequest body) {
        UserUpdateDto dto = toUpdateDto(body);
        return toResponse(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /* ---------- mappers ---------- */

    private UserResponse toResponse(UserReadDto dto) {
        return new UserResponse(dto.id(), dto.email(), dto.role(), dto.createdAt());
    }

    private UserCreateDto toCreateDto(UserCreateRequest req) {
        return new UserCreateDto(
                trim(req.email()),
                req.password(),
                parseRoleOrNull(req.role())
        );
    }

    private UserUpdateDto toUpdateDto(UserUpdateRequest req) {
        return new UserUpdateDto(
                trim(req.email()),
                req.password(),
                parseRoleOrNull(req.role())
        );
    }

    private static UserRole parseRoleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return UserRole.valueOf(s.trim().toUpperCase());
    }
    private static String trim(String s) { return s == null ? null : s.trim(); }
}