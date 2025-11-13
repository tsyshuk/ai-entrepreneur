package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectUpdateDto {

    @NotBlank(message = "name must not be blank")
    @Size(max = 100, message = "name must be <= 100 chars")
    @Pattern(
            regexp = "^[\\p{L}\\p{N} _\\-\\.,()]+$",
            message = "name contains invalid characters"
    )
    public String name;

    @Size(max = 1000, message = "description must be <= 1000 chars")
    public String description;
}