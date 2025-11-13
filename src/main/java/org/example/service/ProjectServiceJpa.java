package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.domain.Project;
import org.example.dto.ProjectCreateDto;
import org.example.dto.ProjectReadDto;
import org.example.dto.ProjectUpdateDto;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.mapper.ProjectMapper;
import org.example.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectServiceJpa implements ProjectService {

    private final ProjectRepository repo;

    @PersistenceContext
    private EntityManager em;

    public ProjectServiceJpa(ProjectRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public ProjectReadDto create(ProjectCreateDto dto) {
        // Нормализация имени (DTO — с публичными полями)
        String name = trim(dto.name);
        String description = dto.description;

        // Бизнес-проверка уникальности (без учёта регистра)
        if (repo.existsByNameIgnoreCase(name)) {
            throw ConflictException.of("Project", "name '" + name + "' already exists");
        }

        // Создаём сущность напрямую (без конструирования нового DTO)
        Project entity = new Project();
        entity.setName(name);
        entity.setDescription(description);

        repo.saveAndFlush(entity);
        // Подтягиваем дефолты из БД (created_at)
        em.refresh(entity);

        return ProjectMapper.toReadDto(entity);
    }

    @Override
    public ProjectReadDto findById(Long id) {
        return repo.findById(id)
                .map(ProjectMapper::toReadDto)
                .orElseThrow(() -> NotFoundException.of("Project", id));
    }

    public List<ProjectReadDto> findAll() {
        return repo.findAll().stream()
                .map(ProjectMapper::toReadDto)
                .toList();
    }

    @Override
    @Transactional
    public ProjectReadDto update(Long id, ProjectUpdateDto dto) {
        Project entity = repo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Project", id));

        // Нормализация новых значений
        String newName = trim(dto.name);
        String newDesc = dto.description;

        // Проверка коллизии имени с другими проектами
        if (repo.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            throw ConflictException.of("Project", "name '" + newName + "' already exists");
        }

        // Обновляем поля у сущности напрямую
        entity.setName(newName);
        entity.setDescription(newDesc);

        return ProjectMapper.toReadDto(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw NotFoundException.of("Project", id);
        }
        repo.deleteById(id);
    }

    public Page<ProjectReadDto> findPage(Pageable pageable) {
        return repo.findAll(pageable).map(ProjectMapper::toReadDto);
    }

    /* ===== helpers ===== */
    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    @Override
    public Page<ProjectReadDto> findPage(Pageable pageable, String nameFilter) {
        if (nameFilter == null || nameFilter.isBlank()) {
            return repo.findAll(pageable).map(ProjectMapper::toReadDto);
        }
        String q = nameFilter.trim();
        return repo.findByNameContainingIgnoreCase(q, pageable)
                .map(ProjectMapper::toReadDto);
    }
}