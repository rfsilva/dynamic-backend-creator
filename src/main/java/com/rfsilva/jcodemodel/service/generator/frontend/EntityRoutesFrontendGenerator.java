package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates the feature routes file for the parent entity (lazy-loaded).
 * Output: src/app/{entity-kebab}/{entity-kebab}.routes.ts
 */
@Component
public class EntityRoutesFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity = ctx.entityName();
        String kebab  = toKebabCase(entity);

        String content = buildContent(ctx, entity, kebab);
        String path    = srcApp(kebab) + "/" + kebab + ".routes.ts";
        return List.of(writeFile(frontendDir, path, content));
    }

    private String buildContent(GenerationContext ctx, String entity, String kebab) {
        StringBuilder sb = new StringBuilder();
        sb.append("import { Routes } from '@angular/router';\n");
        sb.append("import { ").append(entity).append("ListComponent } from './").append(kebab).append("-list/").append(kebab).append("-list.component';\n");
        sb.append("import { ").append(entity).append("FormComponent } from './").append(kebab).append("-form/").append(kebab).append("-form.component';\n");

        for (ChildEntityDefinition child : children(ctx)) {
            String childEntity = child.getEntityName();
            String childKebab  = toKebabCase(childEntity);
            sb.append("import { ").append(childEntity).append("ListComponent } from '../").append(childKebab).append("/").append(childKebab).append("-list/").append(childKebab).append("-list.component';\n");
            sb.append("import { ").append(childEntity).append("FormComponent } from '../").append(childKebab).append("/").append(childKebab).append("-form/").append(childKebab).append("-form.component';\n");
        }

        sb.append("\nexport const ").append(entity).append("ROUTES: Routes = [\n");
        sb.append("  { path: '', component: ").append(entity).append("ListComponent },\n");
        sb.append("  { path: 'new', component: ").append(entity).append("FormComponent },\n");
        sb.append("  { path: ':id/edit', component: ").append(entity).append("FormComponent },\n");

        for (ChildEntityDefinition child : children(ctx)) {
            String childEntity = child.getEntityName();
            String childKebab  = toKebabCase(childEntity);
            sb.append("  {\n");
            sb.append("    path: ':id/").append(childKebab).append("s',\n");
            sb.append("    children: [\n");
            sb.append("      { path: '', component: ").append(childEntity).append("ListComponent },\n");
            sb.append("      { path: 'new', component: ").append(childEntity).append("FormComponent },\n");
            sb.append("      { path: ':childId/edit', component: ").append(childEntity).append("FormComponent },\n");
            sb.append("    ],\n");
            sb.append("  },\n");
        }

        sb.append("];\n");
        return sb.toString();
    }
}
