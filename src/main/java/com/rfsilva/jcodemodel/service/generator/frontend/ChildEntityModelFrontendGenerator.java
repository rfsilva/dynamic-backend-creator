package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates TypeScript model interfaces for each child entity.
 * Output: src/app/{child-kebab}/{child-kebab}.model.ts
 */
@Component
public class ChildEntityModelFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        List<String> generated = new java.util.ArrayList<>();
        for (ChildEntityDefinition child : children(ctx)) {
            generated.addAll(generateChild(child, frontendDir));
        }
        return generated;
    }

    private List<String> generateChild(ChildEntityDefinition child, String frontendDir) throws IOException {
        String entity = child.getEntityName();
        String kebab  = toKebabCase(entity);

        StringBuilder sb = new StringBuilder();
        sb.append("export interface ").append(entity).append(" {\n");
        sb.append("  id: number;\n");
        for (FieldDefinition f : child.getFields()) {
            sb.append("  ").append(f.getName()).append(": ").append(toTsType(f.getType())).append(";\n");
        }
        sb.append("}\n\n");

        sb.append("export interface ").append(entity).append("Request {\n");
        for (FieldDefinition f : child.getFields()) {
            sb.append("  ").append(f.getName()).append(": ").append(toTsType(f.getType())).append(";\n");
        }
        sb.append("}\n");

        String path = srcApp(kebab) + "/" + kebab + ".model.ts";
        return List.of(writeFile(frontendDir, path, sb.toString()));
    }
}
