package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class UserServiceJpa implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @PersistenceContext
    private EntityManager em;

    // Обычный конструктор вместо Lombok
    public UserServiceJpa(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserReadDto create(UserCreateDto dto) {
        String email = safe(dto.email());
        if (repo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("User with email '" + email + "' already exists");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(nullToEmpty(dto.password())));
        u.setRole(dto.role() == null ? UserRole.USER : dto.role());

        User saved = repo.saveAndFlush(u);
        em.refresh(saved);
        return toReadDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadDto findById(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
        return toReadDto(u);
    }

    @Override
    public UserReadDto update(Long id, UserUpdateDto dto) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));

        if (dto.email() != null) {
            String newEmail = safe(dto.email());
            if (!newEmail.equalsIgnoreCase(u.getEmail()) &&
                    repo.existsByEmailIgnoreCase(newEmail)) {
                throw new ConflictException("User with email '" + newEmail + "' already exists");
            }
            u.setEmail(newEmail);
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            u.setPasswordHash(encoder.encode(dto.password()));
        }
        if (dto.role() != null) {
            u.setRole(dto.role());
        }
        return toReadDto(repo.saveAndFlush(u));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("User %d not found".formatted(id));
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserReadDto> findPage(Pageable pageable, String emailFilter) {
        if (emailFilter == null || emailFilter.isBlank()) {
            return repo.findAll(pageable).map(UserServiceJpa::toReadDto);
        } else {
            return repo.findByEmailContainingIgnoreCase(emailFilter.trim(), pageable)
                    .map(UserServiceJpa::toReadDto);
        }
    }

    /* helpers */
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String safe(String s) { return s == null ? null : s.trim(); }

    private static UserReadDto toReadDto(User u) {
        OffsetDateTime cat = u.getCreatedAt();
        String created = (cat == null) ? null : cat.toString();
        return new UserReadDto(
                u.getId(),
                u.getEmail(),
                u.getRole() == null ? null : u.getRole().name(),
                created
        );
    }
}
