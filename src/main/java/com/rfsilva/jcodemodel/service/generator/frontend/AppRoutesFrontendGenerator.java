package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/app/app.routes.ts — root routes with lazy-loaded entity feature routes.
 */
@Component
public class AppRoutesFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity = ctx.entityName();
        String kebab  = toKebabCase(entity);

        StringBuilder imports = new StringBuilder();
        StringBuilder routes  = new StringBuilder();

        imports.append("import { Routes } from '@angular/router';\n\n");

        routes.append("export const routes: Routes = [\n");
        routes.append("  { path: '', redirectTo: '").append(kebab).append("s', pathMatch: 'full' },\n");
        routes.append("  {\n");
        routes.append("    path: '").append(kebab).append("s',\n");
        routes.append("    loadChildren: () =>\n");
        routes.append("      import('./").append(kebab).append("/").append(kebab).append(".routes')\n");
        routes.append("        .then(m => m.").append(entity).append("ROUTES),\n");
        routes.append("  },\n");

        for (ChildEntityDefinition child : children(ctx)) {
            // child routes are nested under the parent feature module
        }

        routes.append("  { path: '**', redirectTo: '").append(kebab).append("s' },\n");
        routes.append("];\n");

        String content = imports.toString() + routes.toString();
        return List.of(writeFile(frontendDir, "src/app/app.routes.ts", content));
    }
}
