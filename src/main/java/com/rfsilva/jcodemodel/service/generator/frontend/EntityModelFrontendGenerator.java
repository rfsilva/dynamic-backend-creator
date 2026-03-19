package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates the TypeScript model interface for the parent entity.
 * Output: src/app/{entity-kebab}/{entity-kebab}.model.ts
 */
@Component
public class EntityModelFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity = ctx.entityName();
        String kebab  = toKebabCase(entity);

        StringBuilder sb = new StringBuilder();
        sb.append("export interface ").append(entity).append(" {\n");
        sb.append("  id: number;\n");
        for (FieldDefinition f : ctx.fields()) {
            sb.append("  ").append(f.getName()).append(": ").append(toTsType(f.getType())).append(";\n");
        }
        sb.append("}\n\n");

        sb.append("export interface ").append(entity).append("Request {\n");
        for (FieldDefinition f : ctx.fields()) {
            sb.append("  ").append(f.getName()).append(": ").append(toTsType(f.getType())).append(";\n");
        }
        sb.append("}\n");

        String path = srcApp(kebab) + "/" + kebab + ".model.ts";
        return List.of(writeFile(frontendDir, path, sb.toString()));
    }
}
