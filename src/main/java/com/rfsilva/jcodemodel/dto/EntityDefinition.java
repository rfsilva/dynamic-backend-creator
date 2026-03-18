package com.rfsilva.jcodemodel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * Defines the entity (object) to generate a CRUD application for.
 */
@Data
public class EntityDefinition {

    @NotBlank(message = "Entity name is required")
    @Pattern(regexp = "^[A-Z][a-zA-Z0-9]*$", message = "Entity name must start with uppercase and contain only alphanumeric characters")
    private String entityName;

    @NotBlank(message = "Package name is required")
    @Pattern(regexp = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$", message = "Invalid package name")
    private String packageName;

    @NotEmpty(message = "At least one field is required")
    @Valid
    private List<FieldDefinition> fields;

    private String outputDirectory;
}
