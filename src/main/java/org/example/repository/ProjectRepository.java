package org.example.repository;

import org.example.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Поиск по части имени (без учёта регистра)
    List<Project> findByNameContainingIgnoreCase(String part);

    // Есть ли проект с таким именем (без учёта регистра)
    boolean existsByNameIgnoreCase(String name);

    // Есть ли проект с таким именем, но с ДРУГИМ id (для проверки при обновлении)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Project> findByNameContainingIgnoreCase(String part, Pageable pageable);
}