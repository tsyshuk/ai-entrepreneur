package org.example.controller;

import org.example.dto.UserResponse;
import org.example.exception.NotFoundException;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(@AuthenticationPrincipal UserDetails principal) {

        if (principal == null) {
            throw new NotFoundException("User is not authenticated");
        }

        String email = principal.getUsername(); // в нашем случае username = email

        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        String createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().toString()
                : null;

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                createdAt
        );
    }
}