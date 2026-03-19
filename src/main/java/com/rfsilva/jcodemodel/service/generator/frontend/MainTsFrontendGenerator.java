package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/main.ts.
 */
@Component
public class MainTsFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String content = """
                import { bootstrapApplication } from '@angular/platform-browser';
                import { appConfig } from './app/app.config';
                import { AppComponent } from './app/app.component';

                bootstrapApplication(AppComponent, appConfig)
                  .catch((err) => console.error(err));
                """;

        return List.of(writeFile(frontendDir, "src/main.ts", content));
    }
}
