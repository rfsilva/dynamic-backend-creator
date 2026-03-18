package com.rfsilva.jcodemodel.service.generator;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class PomFileGenerator implements FileGenerator {

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        String entityName = ctx.entityName();
        String groupId    = ctx.pkg();
        String artifactId = entityName.toLowerCase() + "-service";
        String mainClass  = ctx.pkg() + "." + entityName + "Application";

        String content = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.2.3</version>
                        <relativePath/>
                    </parent>
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <properties>
                        <java.version>21</java.version>
                        <start-class>%s</start-class>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-jpa</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>com.h2database</groupId>
                            <artifactId>h2</artifactId>
                            <scope>runtime</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <optional>true</optional>
                        </dependency>
                    </dependencies>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(groupId, artifactId, mainClass);

        String filePath = outputDir + "/pom.xml";
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write(content);
        }
        return "pom.xml";
    }
}
