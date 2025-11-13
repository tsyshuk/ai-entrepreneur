package org.example.service;

import org.example.domain.Project;
import org.example.dto.ProjectCreateDto;
import org.example.dto.ProjectReadDto;
import org.example.dto.ProjectUpdateDto;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ProjectServiceJpaTest {

    @Mock
    ProjectRepository repo;

    @Mock
    EntityManager em;

    @InjectMocks
    ProjectServiceJpa service;

    @BeforeEach
    void injectEntityManagerIfNeeded() {
        // На случай, если @InjectMocks не воткнул em в поле @PersistenceContext
        setField(service, "em", em);
    }

    // Хелпер: парсим ISO-8601 (с секундами или без) и приводим к Instant
    private static Instant toInstant(String iso) {
        return OffsetDateTime.parse(iso).toInstant();
    }

    /* ===================== create ===================== */

    @Test
    void create_ok_savesAndRefreshes_andReturnsDto() {
        // given
        var dto = new ProjectCreateDto();
        setField(dto, "name", "AI Core");
        setField(dto, "description", "x");

        when(repo.existsByNameIgnoreCase("AI Core")).thenReturn(false);

        when(repo.saveAndFlush(any(Project.class))).thenAnswer(inv -> {
            Project arg = inv.getArgument(0);
            setField(arg, "id", 42L);
            arg.setCreatedAt(OffsetDateTime.parse("2025-10-24T12:00:00Z"));
            return arg;
        });

        // when
        ProjectReadDto res = service.create(dto);

        // then
        verify(repo).existsByNameIgnoreCase("AI Core");
        verify(repo).saveAndFlush(any(Project.class));
        verify(em).refresh(any(Project.class));
        assertThat(res.id()).isEqualTo(42L);
        assertThat(res.name()).isEqualTo("AI Core");
        assertThat(res.description()).isEqualTo("x");
        // Сравниваем как время (а не как строку с обязательными секундами)
        assertThat(toInstant(res.createdAt()))
                .isEqualTo(toInstant("2025-10-24T12:00:00Z"));
    }

    @Test
    void create_conflict_throws409() {
        var dto = new ProjectCreateDto();
        setField(dto, "name", "AI Core");
        setField(dto, "description", "x");

        when(repo.existsByNameIgnoreCase("AI Core")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(repo, never()).saveAndFlush(any());
        verify(em, never()).refresh(any());
    }

    /* ===================== findById ===================== */

    @Test
    void findById_found_returnsDto() {
        var p = new Project();
        setField(p, "id", 7L);
        p.setName("Gamma");
        p.setDescription("d");
        p.setCreatedAt(OffsetDateTime.parse("2025-10-24T13:00:00Z"));

        when(repo.findById(7L)).thenReturn(Optional.of(p));

        ProjectReadDto dto = service.findById(7L);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("Gamma");
        assertThat(dto.description()).isEqualTo("d");
        assertThat(toInstant(dto.createdAt()))
                .isEqualTo(toInstant("2025-10-24T13:00:00Z"));
    }

    @Test
    void findById_notFound_throws404() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 999 not found");
    }

    /* ===================== update ===================== */

    @Test
    void update_ok_changesFields_andReturnsDto() {
        var existing = new Project();
        setField(existing, "id", 10L);
        existing.setName("Old");
        existing.setDescription("d1");
        existing.setCreatedAt(OffsetDateTime.parse("2025-10-24T14:00:00Z"));

        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.existsByNameIgnoreCaseAndIdNot("New", 10L)).thenReturn(false);

        var dto = new ProjectUpdateDto();
        setField(dto, "name", "New");
        setField(dto, "description", "d2");

        ProjectReadDto res = service.update(10L, dto);

        verify(repo).findById(10L);
        verify(repo).existsByNameIgnoreCaseAndIdNot("New", 10L);

        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.name()).isEqualTo("New");
        assertThat(res.description()).isEqualTo("d2");
        assertThat(toInstant(res.createdAt()))
                .isEqualTo(toInstant("2025-10-24T14:00:00Z"));
    }

    @Test
    void update_notFound_throws404() {
        when(repo.findById(10L)).thenReturn(Optional.empty());

        var dto = new ProjectUpdateDto();
        setField(dto, "name", "New");
        setField(dto, "description", "d2");

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_conflictName_throws409() {
        var existing = new Project();
        setField(existing, "id", 10L);
        existing.setName("Old");
        existing.setDescription("d1");
        when(repo.findById(10L)).thenReturn(Optional.of(existing));

        when(repo.existsByNameIgnoreCaseAndIdNot("Alpha", 10L)).thenReturn(true);

        var dto = new ProjectUpdateDto();
        setField(dto, "name", "Alpha");
        setField(dto, "description", "d2");

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    /* ===================== delete ===================== */

    @Test
    void delete_ok_callsRepoDelete() {
        when(repo.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repo).existsById(5L);
        verify(repo).deleteById(5L);
    }

    @Test
    void delete_notFound_throws404() {
        when(repo.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(NotFoundException.class);
        verify(repo, never()).deleteById(any());
    }

    /* ===================== findPage (с фильтром / без) ===================== */

    @Test
    void findPage_noFilter_usesFindAll_andMaps() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        var p1 = new Project();
        setField(p1, "id", 1L);
        p1.setName("A");
        p1.setDescription("d");
        p1.setCreatedAt(OffsetDateTime.parse("2025-10-24T10:00:00Z"));

        var p2 = new Project();
        setField(p2, "id", 2L);
        p2.setName("B");
        p2.setDescription("e");
        p2.setCreatedAt(OffsetDateTime.parse("2025-10-24T09:00:00Z"));

        when(repo.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));

        var page = service.findPage(pageable, null);

        verify(repo).findAll(pageable);
        assertThat(page.getContent()).extracting(ProjectReadDto::name)
                .containsExactly("A", "B");
    }

    @Test
    void findPage_withFilter_usesFindByNameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 5);

        var p = new Project();
        setField(p, "id", 3L);
        p.setName("AI Core");
        p.setDescription("x");
        p.setCreatedAt(OffsetDateTime.parse("2025-10-24T08:00:00Z"));

        when(repo.findByNameContainingIgnoreCase("ai", pageable))
                .thenReturn(new PageImpl<>(List.of(p), pageable, 1));

        var page = service.findPage(pageable, "  ai  ");

        verify(repo).findByNameContainingIgnoreCase("ai", pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("AI Core");
    }
}