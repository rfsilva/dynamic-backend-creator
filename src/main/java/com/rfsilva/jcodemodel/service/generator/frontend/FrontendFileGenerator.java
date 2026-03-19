package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;

import java.io.IOException;
import java.util.List;

/**
 * Contract for generators that produce Angular frontend files.
 * Returns the list of relative paths of the generated files.
 */
public interface FrontendFileGenerator {

    List<String> generate(GenerationContext ctx, String frontendDir) throws IOException;
}
