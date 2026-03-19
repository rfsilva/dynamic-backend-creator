package com.rfsilva.jcodemodel.service.generator.backend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class PostmanEnvironmentFileGenerator implements FileGenerator {

    private static final String VAR_SEP = ",\n    ";

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        String fileName = ctx.entityName().toLowerCase() + ".environment.json";
        new java.io.File(outputDir + "/postman").mkdirs();
        String content  = buildEnvironment(ctx);
        try (FileWriter fw = new FileWriter(outputDir + "/postman/" + fileName)) {
            fw.write(content);
        }
        return "postman/" + fileName;
    }

    private String buildEnvironment(GenerationContext ctx) {
        String entity = ctx.entityName();
        String lower  = entity.toLowerCase();
        List<ChildEntityDefinition> children = ctx.getDefinition().getChildren();

        StringBuilder vars = new StringBuilder();
        vars.append(variable("baseUrl", "http://localhost:" + ctx.backendPort()));
        vars.append(VAR_SEP).append(variable("frontendUrl", "http://localhost:" + ctx.frontendPort()));
        vars.append(VAR_SEP).append(variable(lower + "Id", "1"));

        if (children != null) {
            for (ChildEntityDefinition child : children) {
                vars.append(VAR_SEP).append(variable(child.getEntityName().toLowerCase() + "Id", "1"));
            }
        }

        return """
                {
                  "id": "%s",
                  "name": "%s Service",
                  "values": [
                    %s
                  ],
                  "_postman_variable_scope": "environment"
                }
                """.formatted(UUID.randomUUID(), entity, vars);
    }

    private String variable(String key, String value) {
        return "{ \"key\": \"%s\", \"value\": \"%s\", \"type\": \"string\", \"enabled\": true }".formatted(key, value);
    }
}
