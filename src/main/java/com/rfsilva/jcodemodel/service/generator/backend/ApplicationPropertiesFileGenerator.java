package com.rfsilva.jcodemodel.service.generator.backend;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class ApplicationPropertiesFileGenerator implements FileGenerator {

    private static final String RELATIVE_PATH = "src/main/resources/application.properties";

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        String lower = ctx.entityName().toLowerCase();

        new File(outputDir + "/src/main/resources").mkdirs();

        String content = String.join(System.lineSeparator(),
                "spring.application.name=" + lower + "-service",
                "server.port=8080",
                "",
                "# H2 Database",
                "spring.datasource.url=jdbc:h2:mem:" + lower + "db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.h2.console.enabled=true",
                "spring.h2.console.path=/h2-console",
                "",
                "# JPA / Hibernate",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true",
                "spring.jpa.properties.hibernate.format_sql=true"
        );

        try (FileWriter fw = new FileWriter(outputDir + "/" + RELATIVE_PATH)) {
            fw.write(content);
        }
        return RELATIVE_PATH;
    }
}
