package com.rfsilva.jcodemodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Defines a field in the entity to be generated.
 */
@Data
public class FieldDefinition {

    @NotBlank(message = "Field name is required")
    private String name;

    @NotNull(message = "Field type is required")
    private FieldType type;

    private boolean required;

    private Integer maxLength;

    private String description;

    public enum FieldType {
        STRING,
        INTEGER,
        LONG,
        DOUBLE,
        BOOLEAN,
        LOCAL_DATE,
        LOCAL_DATE_TIME,
        BIG_DECIMAL
    }
}
