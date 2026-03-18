package com.rfsilva.jcodemodel.service.generator;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class GitignoreFileGenerator implements FileGenerator {

    private static final String FILE_NAME = ".gitignore";

    private static final String CONTENT = """
            # Compiled output
            target/
            *.class

            # Maven wrapper
            !.mvn/wrapper/maven-wrapper.jar
            !**/src/main/**/target/
            !**/src/test/**/target/

            # IDE — IntelliJ IDEA
            .idea/
            *.iws
            *.iml
            *.ipr

            # IDE — Eclipse
            .settings/
            .classpath
            .project

            # IDE — VS Code
            .vscode/

            # OS artefacts
            .DS_Store
            Thumbs.db

            # Spring Boot
            spring-shell.log

            # Logs
            *.log
            logs/

            # Environment / secrets
            .env
            *.env.local

            # Docker
            docker-compose.override.yml
            """;

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        try (FileWriter fw = new FileWriter(outputDir + "/" + FILE_NAME)) {
            fw.write(CONTENT);
        }
        return FILE_NAME;
    }
}
