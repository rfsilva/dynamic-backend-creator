package com.rfsilva.jcodemodel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * Defines a child entity (one-to-many) within the main entity definition.
 * Example: SalesOrder → List<OrderItem>, Usuario → List<Endereco>
 */
@Data
public class ChildEntityDefinition {

    @NotBlank(message = "Child entity name is required")
    @Pattern(regexp = "^[A-Z][a-zA-Z0-9]*$",
            message = "Child entity name must start with uppercase and contain only alphanumeric characters")
    private String entityName;

    @NotEmpty(message = "Child entity must have at least one field")
    @Valid
    private List<FieldDefinition> fields;
}
