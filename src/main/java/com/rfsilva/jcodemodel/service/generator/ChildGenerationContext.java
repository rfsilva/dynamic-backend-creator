package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Context for a single child entity generation.
 * Delegates package/cm access to the parent context and
 * stores the child's own generated classes.
 */
@Getter
public class ChildGenerationContext {

    private final GenerationContext parent;
    private final ChildEntityDefinition definition;

    @Setter private JDefinedClass entityClass;
    @Setter private JDefinedClass requestDto;
    @Setter private JDefinedClass responseDto;
    @Setter private JDefinedClass notFoundException;
    @Setter private JDefinedClass repositoryClass;
    @Setter private JDefinedClass mapperClass;
    @Setter private JDefinedClass serviceClass;

    public ChildGenerationContext(GenerationContext parent, ChildEntityDefinition definition) {
        this.parent = parent;
        this.definition = definition;
    }

    // --- Convenience delegates ---

    public JCodeModel getCm()              { return parent.getCm(); }
    public String pkg()                    { return parent.pkg(); }
    public JPackage subPackage(String sfx) { return parent.subPackage(sfx); }
    public String entityName()             { return definition.getEntityName(); }
    public List<FieldDefinition> fields()  { return definition.getFields(); }

    /** Name of the parent entity class (e.g. "SalesOrder"). */
    public String parentEntityName()       { return parent.entityName(); }

    /** Already-generated parent JDefinedClass. */
    public JDefinedClass parentEntityClass()  { return parent.getEntityClass(); }
    public JDefinedClass parentRepoClass()    { return parent.getRepositoryClass(); }
    public JDefinedClass parentNotFoundEx()   { return parent.getNotFoundException(); }
}
