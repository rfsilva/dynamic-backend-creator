package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;

/**
 * Base class for all child-entity generators.
 * Extends {@link AbstractClassGenerator} to reuse helper utilities,
 * implements {@link ChildClassGenerator} as the primary contract,
 * and stubs out the unused {@link ClassGenerator#generate(GenerationContext)}.
 */
public abstract class AbstractChildClassGenerator
        extends AbstractClassGenerator
        implements ChildClassGenerator {

    @Override
    public final JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " requires a ChildGenerationContext — call generate(ChildGenerationContext)");
    }
}
