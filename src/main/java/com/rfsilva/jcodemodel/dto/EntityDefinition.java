package com.rfsilva.jcodemodel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    /** Child entities with one-to-many relationship (optional). */
    @Valid
    private List<ChildEntityDefinition> children;

    /** Target database. Defaults to H2 if not specified. */
    @NotNull(message = "Database type is required")
    private DatabaseType database = DatabaseType.H2;

    private String outputDirectory;

    /** HTTP port for the generated application. Defaults to 8080. */
    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private int port = 8080;
}
