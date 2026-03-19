package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/index.html.
 */
@Component
public class IndexHtmlFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity = ctx.entityName();
        String content = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>%s App</title>
                  <base href="/">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <link rel="icon" type="image/svg+xml" href="favicon.svg">
                  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap" rel="stylesheet">
                  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
                </head>
                <body class="mat-typography">
                  <app-root></app-root>
                </body>
                </html>
                """.formatted(entity);

        return List.of(writeFile(frontendDir, "src/index.html", content));
    }
}
