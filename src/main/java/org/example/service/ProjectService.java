package org.example.service;

import org.example.dto.ProjectCreateDto;
import org.example.dto.ProjectReadDto;
import org.example.dto.ProjectUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ProjectService {

    // Получить страницу проектов (пример, если у тебя есть пагинация)
    @PreAuthorize("hasRole('USER')")
    Page<ProjectReadDto> findPage(Pageable pageable, String nameFilter);

    // Получить один проект по id
    @PreAuthorize("hasRole('USER')")
    ProjectReadDto findById(Long id);

    // Создать проект
    @PreAuthorize("hasRole('USER')")
    ProjectReadDto create(ProjectCreateDto dto);

    // Обновить проект
    @PreAuthorize("hasRole('USER')")
    ProjectReadDto update(Long id, ProjectUpdateDto dto);

    // Удалить проект
    @PreAuthorize("hasRole('USER')")
    void delete(Long id);
}
