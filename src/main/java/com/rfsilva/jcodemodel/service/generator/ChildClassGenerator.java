package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;

/**
 * Contract for all child-entity class generators.
 * Receives a {@link ChildGenerationContext} instead of the parent {@link GenerationContext}.
 */
public interface ChildClassGenerator {

    JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException;
}
