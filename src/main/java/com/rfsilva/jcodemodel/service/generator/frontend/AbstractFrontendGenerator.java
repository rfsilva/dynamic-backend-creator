package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition.FieldType;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Base class for Angular frontend generators providing shared utilities.
 */
public abstract class AbstractFrontendGenerator implements FrontendFileGenerator {

    // ── Naming conventions ────────────────────────────────────────────────────

    /** "SalesOrder" → "sales-order" */
    protected static String toKebabCase(String name) {
        return name.replaceAll("([A-Z])", "-$1").toLowerCase().replaceFirst("^-", "");
    }

    /** "SalesOrder" → "salesOrder" */
    protected static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /** "SalesOrder" → "SalesOrder" (identity, but makes intent explicit) */
    protected static String toPascalCase(String name) {
        return name;
    }

    /** "SalesOrder" → "SALES_ORDER" */
    protected static String toConstantCase(String name) {
        return name.replaceAll("([A-Z])", "_$1").toUpperCase().replaceFirst("^_", "");
    }

    // ── TypeScript type mapping ────────────────────────────────────────────────

    protected static String toTsType(FieldType type) {
        return switch (type) {
            case STRING, LOCAL_DATE, LOCAL_DATE_TIME -> "string";
            case INTEGER, LONG, DOUBLE, BIG_DECIMAL  -> "number";
            case BOOLEAN                             -> "boolean";
        };
    }

    protected static String toTsDefaultValue(FieldType type) {
        return switch (type) {
            case STRING, LOCAL_DATE, LOCAL_DATE_TIME -> "''";
            case INTEGER, LONG, DOUBLE, BIG_DECIMAL  -> "0";
            case BOOLEAN                             -> "false";
        };
    }

    protected static String toInputType(FieldType type) {
        return switch (type) {
            case INTEGER, LONG, DOUBLE, BIG_DECIMAL -> "number";
            case BOOLEAN                            -> "checkbox";
            case LOCAL_DATE                         -> "date";
            case LOCAL_DATE_TIME                    -> "datetime-local";
            default                                 -> "text";
        };
    }

    // ── File writing ──────────────────────────────────────────────────────────

    protected String writeFile(String dir, String relativePath, String content) throws IOException {
        Path fullPath = Path.of(dir, relativePath);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content);
        return relativePath;
    }

    // ── Field helpers ─────────────────────────────────────────────────────────

    protected static String buildInterfaceFields(List<FieldDefinition> fields) {
        StringBuilder sb = new StringBuilder();
        for (FieldDefinition f : fields) {
            sb.append("  ").append(f.getName()).append(": ").append(toTsType(f.getType())).append(";\n");
        }
        return sb.toString();
    }

    protected static String buildFormGroupFields(List<FieldDefinition> fields) {
        StringBuilder sb = new StringBuilder();
        for (FieldDefinition f : fields) {
            String defaultVal = toTsDefaultValue(f.getType());
            String validators = f.isRequired() ? ", [Validators.required]" : "";
            sb.append("      ").append(f.getName()).append(": [").append(defaultVal).append(validators).append("],\n");
        }
        return sb.toString();
    }

    protected static boolean hasRequiredFields(List<FieldDefinition> fields) {
        return fields.stream().anyMatch(FieldDefinition::isRequired);
    }

    // ── Path helpers ──────────────────────────────────────────────────────────

    protected static String apiBasePath(String entityName) {
        return "/api/" + toKebabCase(entityName) + "s";
    }

    protected static String apiChildBasePath(String parentName, String childName) {
        return "/api/" + toKebabCase(parentName) + "s/${parentId}/" + toKebabCase(childName) + "s";
    }

    protected static List<ChildEntityDefinition> children(GenerationContext ctx) {
        List<ChildEntityDefinition> ch = ctx.getDefinition().getChildren();
        return ch != null ? ch : List.of();
    }

    protected static String srcApp(String entityKebab) {
        return "src/app/" + entityKebab;
    }
}
