package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;

/**
 * Contract for all class generators. Each implementation is responsible for
 * generating exactly one Java class/interface into the given {@link GenerationContext}.
 */
public interface ClassGenerator {

    JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException;
}
