package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/app/app.config.ts — bootstraps HttpClient, Router with
 * component input binding, and Angular Material animations.
 */
@Component
public class AppConfigFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String content = """
                import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
                import { provideRouter, withComponentInputBinding } from '@angular/router';
                import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
                import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
                import { routes } from './app.routes';

                export const appConfig: ApplicationConfig = {
                  providers: [
                    provideZoneChangeDetection({ eventCoalescing: true }),
                    provideRouter(routes, withComponentInputBinding()),
                    provideHttpClient(withInterceptorsFromDi()),
                    provideAnimationsAsync(),
                  ],
                };
                """;

        return List.of(writeFile(frontendDir, "src/app/app.config.ts", content));
    }
}
