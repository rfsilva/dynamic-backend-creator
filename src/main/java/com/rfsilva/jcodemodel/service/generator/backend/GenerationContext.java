package com.rfsilva.jcodemodel.service.generator.backend;

import com.rfsilva.jcodemodel.dto.DatabaseType;
import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
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
    @Setter private JDefinedClass globalExceptionHandler;
    @Setter private JDefinedClass repositoryClass;
    @Setter private JDefinedClass mapperClass;
    @Setter private JDefinedClass serviceClass;

    private final List<ChildGenerationContext> childContexts = new ArrayList<>();

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

    public DatabaseType databaseType() {
        return definition.getDatabase() != null ? definition.getDatabase() : DatabaseType.H2;
    }

    public int backendPort() {
        return definition.getBackendPort();
    }

    public int frontendPort() {
        return definition.getFrontendPort();
    }

    public JPackage subPackage(String suffix) {
        return cm._package(pkg() + "." + suffix);
    }

    public void addChildContext(ChildGenerationContext childCtx) {
        childContexts.add(childCtx);
    }

    public List<ChildGenerationContext> getChildContexts() {
        return Collections.unmodifiableList(childContexts);
    }
}
