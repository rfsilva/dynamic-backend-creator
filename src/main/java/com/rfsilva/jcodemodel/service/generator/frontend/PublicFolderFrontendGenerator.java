package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates the public/ folder with a favicon.svg so Angular's asset pipeline resolves it.
 */
@Component
public class PublicFolderFrontendGenerator extends AbstractFrontendGenerator {

    private static final String FAVICON_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32">
              <circle cx="12" cy="12" r="12" fill="#1976d2"/>
              <text x="12" y="17" text-anchor="middle" font-size="14" fill="white" font-family="sans-serif">J</text>
            </svg>
            """;

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        return List.of(writeFile(frontendDir, "public/favicon.svg", FAVICON_SVG));
    }
}
