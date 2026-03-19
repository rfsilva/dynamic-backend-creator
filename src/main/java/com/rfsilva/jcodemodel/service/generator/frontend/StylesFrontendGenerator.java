package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/styles.scss (global styles with Angular Material theme).
 */
@Component
public class StylesFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String content = """
                @use '@angular/material' as mat;

                html {
                  @include mat.theme((
                    color: (
                      primary: mat.$violet-palette,
                      theme-type: light,
                    ),
                    typography: Roboto,
                    density: 0,
                  ));
                }

                html, body {
                  height: 100%;
                  margin: 0;
                  font-family: Roboto, "Helvetica Neue", sans-serif;
                }

                .page-container {
                  padding: 24px;
                  max-width: 1200px;
                  margin: 0 auto;
                }

                .action-bar {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 16px;
                }

                .form-row {
                  display: flex;
                  flex-wrap: wrap;
                  gap: 16px;

                  mat-form-field {
                    flex: 1 1 260px;
                  }
                }

                .form-actions {
                  display: flex;
                  justify-content: flex-end;
                  gap: 8px;
                  margin-top: 16px;
                }
                """;

        return List.of(writeFile(frontendDir, "src/styles.scss", content));
    }
}
