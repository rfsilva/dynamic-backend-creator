package com.rfsilva.jcodemodel.service.generator.backend;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates a WebConfig.java with CORS configuration that allows the Angular
 * dev server (http://localhost:4200) to call the backend.
 * Output: src/main/java/{package}/config/WebConfig.java
 */
@Component
public class CorsConfigFileGenerator implements FileGenerator {

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        String pkg         = ctx.pkg();
        String configPkg   = pkg + ".config";
        String relativePath = "src/main/java/" + configPkg.replace('.', '/') + "/WebConfig.java";

        new File(outputDir + "/src/main/java/" + configPkg.replace('.', '/')).mkdirs();

        String content = buildContent(configPkg, ctx.frontendPort());

        try (FileWriter fw = new FileWriter(outputDir + "/" + relativePath)) {
            fw.write(content);
        }
        return relativePath;
    }

    private String buildContent(String configPkg, int frontendPort) {
        return "package " + configPkg + ";\n\n"
             + "import org.springframework.context.annotation.Configuration;\n"
             + "import org.springframework.web.servlet.config.annotation.CorsRegistry;\n"
             + "import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;\n\n"
             + "@Configuration\n"
             + "public class WebConfig implements WebMvcConfigurer {\n\n"
             + "    @Override\n"
             + "    public void addCorsMappings(CorsRegistry registry) {\n"
             + "        registry.addMapping(\"/api/**\")\n"
             + "                .allowedOrigins(\"http://localhost:" + frontendPort + "\")\n"
             + "                .allowedMethods(\"GET\", \"POST\", \"PUT\", \"DELETE\", \"OPTIONS\")\n"
             + "                .allowedHeaders(\"*\")\n"
             + "                .allowCredentials(true)\n"
             + "                .maxAge(3600);\n"
             + "    }\n"
             + "}\n";
    }
}
