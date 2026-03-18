package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Carries the JCodeModel instance, the entity definition, and all
 * already-generated classes so that generators can reference each other.
 */
@Getter
public class GenerationContext {

    private final JCodeModel cm;
    private final EntityDefinition definition;

    // Populated progressively by CodeGeneratorService
    @Setter private JDefinedClass entityClass;
    @Setter private JDefinedClass requestDto;
    @Setter private JDefinedClass responseDto;
    @Setter private JDefinedClass notFoundException;
    @Setter private JDefinedClass repositoryClass;
    @Setter private JDefinedClass mapperClass;
    @Setter private JDefinedClass serviceClass;

    public GenerationContext(JCodeModel cm, EntityDefinition definition) {
        this.cm = cm;
        this.definition = definition;
    }

    // --- Convenience accessors ---

    public String pkg() {
        return definition.getPackageName();
    }

    public String entityName() {
        return definition.getEntityName();
    }

    public List<FieldDefinition> fields() {
        return definition.getFields();
    }

    public JPackage subPackage(String suffix) {
        return cm._package(pkg() + "." + suffix);
    }
}
