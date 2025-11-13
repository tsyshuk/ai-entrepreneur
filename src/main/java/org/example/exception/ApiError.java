package org.example.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Стандартное тело ошибки")
public record ApiError(
        @Schema(example = "2025-10-29T18:36:43Z") String timestamp,
        @Schema(example = "/api/projects/123") String path,
        @Schema(example = "Not Found") String error,
        @Schema(example = "Project 123 not found") String message
) {}