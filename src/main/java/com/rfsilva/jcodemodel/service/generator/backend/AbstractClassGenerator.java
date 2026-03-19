package com.rfsilva.jcodemodel.service.generator.backend;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base class with shared utilities for all generators.
 */
public abstract class AbstractClassGenerator implements ClassGenerator {

    protected JType resolveJType(JCodeModel cm, FieldDefinition.FieldType type) {
        return switch (type) {
            case STRING          -> cm.ref(String.class);
            case INTEGER         -> cm.ref(Integer.class);
            case LONG            -> cm.ref(Long.class);
            case DOUBLE          -> cm.ref(Double.class);
            case BOOLEAN         -> cm.ref(Boolean.class);
            case LOCAL_DATE      -> cm.ref(LocalDate.class);
            case LOCAL_DATE_TIME -> cm.ref(LocalDateTime.class);
            case BIG_DECIMAL     -> cm.ref(BigDecimal.class);
        };
    }

    protected String getter(String camelName) {
        return "get" + capitalize(camelName);
    }

    protected String setter(String camelName) {
        return "set" + capitalize(camelName);
    }

    protected String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    protected String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    protected String toSnakeCase(String name) {
        return name.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }

    protected String toKebabCase(String name) {
        return name.replaceAll("([A-Z])", "-$1").toLowerCase().replaceFirst("^-", "");
    }
}
