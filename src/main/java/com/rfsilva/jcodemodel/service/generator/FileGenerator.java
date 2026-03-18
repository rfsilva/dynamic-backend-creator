package com.rfsilva.jcodemodel.service.generator;

import java.io.IOException;

/**
 * Contract for generators that produce non-Java files (pom.xml, application.properties, etc.).
 * Returns the path of the generated file relative to the output directory.
 */
public interface FileGenerator {

    String generate(GenerationContext ctx, String outputDir) throws IOException;
}
