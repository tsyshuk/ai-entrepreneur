package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.ProjectCreateDto;
import org.example.dto.ProjectReadDto;
import org.example.dto.ProjectUpdateDto;
import org.example.service.ProjectServiceJpa;
import org.example.exception.ApiError;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@Tag(name = "project-rest-controller", description = "CRUD по проектам")
@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    private final ProjectServiceJpa service;

    public ProjectRestController(ProjectServiceJpa service) {
        this.service = service;
    }

    @Operation(summary = "Список проектов (постранично, с фильтром по имени)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры (page/size/sort)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Page<ProjectReadDto> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Фильтр по подстроке имени (регистронезависимый)",
                    example = "ai")
            @RequestParam(name = "name", required = false) String name
    ) {
        return service.findPage(pageable, name);
    }

    @Operation(summary = "Получить проект по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найден"),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ProjectReadDto get(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Создать проект")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создан",
                    headers = @Header(name = "Location", description = "URI созданного ресурса"),
                    content = @Content(schema = @Schema(implementation = ProjectReadDto.class))),
            @ApiResponse(responseCode = "400", description = "Валидация не прошла",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт имени",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProjectReadDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового проекта", required = true,
                    content = @Content(schema = @Schema(implementation = ProjectCreateDto.class)))
            @RequestBody @Valid ProjectCreateDto dto,
            UriComponentsBuilder uriBuilder
    ) {
        ProjectReadDto created = service.create(dto);
        return ResponseEntity
                .created(uriBuilder.path("/api/projects/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @Operation(summary = "Обновить проект")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлён"),
            @ApiResponse(responseCode = "400", description = "Валидация не прошла",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт имени",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ProjectReadDto update(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Изменяемые поля проекта", required = true,
                    content = @Content(schema = @Schema(implementation = ProjectUpdateDto.class)))
            @RequestBody @Valid ProjectUpdateDto dto
    ) {
        return service.update(id, dto);
    }

    @Operation(summary = "Удалить проект")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалён, тело отсутствует"),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}