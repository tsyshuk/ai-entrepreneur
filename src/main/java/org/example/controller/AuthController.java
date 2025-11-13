package org.example.controller;

import jakarta.validation.Valid;
import org.example.config.JwtProperties;
import org.example.dto.AuthLoginRequest;
import org.example.dto.AuthRegisterRequest;
import org.example.dto.AuthTokenResponse;
import org.example.dto.UserResponse;
import org.example.security.JwtService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final long accessTtlSec;

    public AuthController(UserService users,
                          AuthenticationManager authManager,
                          JwtService jwtService,
                          JwtProperties props) {
        this.users = users;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.accessTtlSec = props.getExpiresIn();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody AuthRegisterRequest req) {
        var created = users.create(new UserService.UserCreateDto(req.email(), req.password(), null));
        var body = new UserResponse(created.id(), created.email(), created.role(), created.createdAt());
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody AuthLoginRequest req) {
        try {
            // 1) Аутентифицируем email+пароль (даже если фильтр JWT есть — логин идёт через менеджер)
            var auth = new UsernamePasswordAuthenticationToken(req.email(), req.password());
            var result = authManager.authenticate(auth);

            // 2) Успех → генерим access-token
            var principal = (org.springframework.security.core.userdetails.UserDetails) result.getPrincipal();
            String accessToken = jwtService.generateAccessToken(principal);

            // 3) Ответ 200 OK
            return ResponseEntity.ok(new AuthTokenResponse(accessToken, "Bearer", accessTtlSec));
        } catch (BadCredentialsException ex) {
            // Неверный пароль/пользователь — 401
            return ResponseEntity.status(401).build();
        }
    }
}