package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/environments/environment.ts and environment.development.ts.
 */
@Component
public class EnvironmentFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        int port = ctx.backendPort();

        String prod = """
                export const environment = {
                  production: true,
                  apiUrl: 'http://localhost:%d',
                };
                """.formatted(port);

        String dev = """
                export const environment = {
                  production: false,
                  apiUrl: 'http://localhost:%d',
                };
                """.formatted(port);

        return List.of(
            writeFile(frontendDir, "src/environments/environment.ts", prod),
            writeFile(frontendDir, "src/environments/environment.development.ts", dev)
        );
    }
}
