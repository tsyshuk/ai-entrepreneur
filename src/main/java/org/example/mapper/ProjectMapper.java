package org.example.mapper;

import org.example.domain.Project;
import org.example.dto.ProjectCreateDto;
import org.example.dto.ProjectReadDto;
import org.example.dto.ProjectUpdateDto;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static Project toEntity(ProjectCreateDto dto) {
        // Оставляю на случай, если где-то ещё используешь этот метод.
        // DTO с публичными полями (не records).
        Project p = new Project();
        p.setName(dto.name);
        p.setDescription(dto.description);
        return p;
    }

    public static ProjectReadDto toReadDto(Project p) {
        // ProjectReadDto у тебя ожидает createdAt как String → форматируем
        String createdAtStr = p.getCreatedAt() == null ? null : p.getCreatedAt().toString();
        return new ProjectReadDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                createdAtStr
        );
    }

    public static void updateEntity(Project target, ProjectUpdateDto dto) {
        // Если где-то используешь вариант с DTO — он тоже должен работать.
        target.setName(dto.name);
        target.setDescription(dto.description);
    }
}