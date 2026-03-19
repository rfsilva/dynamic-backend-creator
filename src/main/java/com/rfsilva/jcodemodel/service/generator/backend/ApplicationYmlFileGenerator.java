package com.rfsilva.jcodemodel.service.generator.backend;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class ApplicationYmlFileGenerator implements FileGenerator {

    private static final String RELATIVE_PATH = "src/main/resources/application.yml";

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        new File(outputDir + "/src/main/resources").mkdirs();
        String content = buildYml(ctx);
        try (FileWriter fw = new FileWriter(outputDir + "/" + RELATIVE_PATH)) {
            fw.write(content);
        }
        return RELATIVE_PATH;
    }

    private String buildYml(GenerationContext ctx) {
        String lower = ctx.entityName().toLowerCase();
        int    port  = ctx.backendPort();
        return switch (ctx.databaseType()) {
            case H2         -> h2Yml(lower, port);
            case POSTGRESQL -> postgresYml(lower, port);
            case MYSQL      -> mysqlYml(lower, port);
            case MARIADB    -> mariadbYml(lower, port);
            case MSSQL      -> mssqlYml(lower, port);
        };
    }

    private String h2Yml(String lower, int port) {
        return """
                spring:
                  application:
                    name: %s-service

                  datasource:
                    url: jdbc:h2:mem:%sdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
                    driver-class-name: org.h2.Driver
                    username: sa
                    password:

                  h2:
                    console:
                      enabled: true
                      path: /h2-console

                  jpa:
                    database-platform: org.hibernate.dialect.H2Dialect
                    hibernate:
                      ddl-auto: create-drop
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true

                server:
                  port: %d
                """.formatted(lower, lower, port);
    }

    private String postgresYml(String lower, int port) {
        return """
                spring:
                  application:
                    name: %s-service

                  datasource:
                    url: jdbc:postgresql://localhost:5432/%sdb
                    driver-class-name: org.postgresql.Driver
                    username: sa
                    password: password

                  jpa:
                    database-platform: org.hibernate.dialect.PostgreSQLDialect
                    hibernate:
                      ddl-auto: update
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true

                server:
                  port: %d
                """.formatted(lower, lower, port);
    }

    private String mysqlYml(String lower, int port) {
        return """
                spring:
                  application:
                    name: %s-service

                  datasource:
                    url: jdbc:mysql://localhost:3306/%sdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
                    driver-class-name: com.mysql.cj.jdbc.Driver
                    username: sa
                    password: password

                  jpa:
                    database-platform: org.hibernate.dialect.MySQLDialect
                    hibernate:
                      ddl-auto: update
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true

                server:
                  port: %d
                """.formatted(lower, lower, port);
    }

    private String mariadbYml(String lower, int port) {
        return """
                spring:
                  application:
                    name: %s-service

                  datasource:
                    url: jdbc:mariadb://localhost:3306/%sdb
                    driver-class-name: org.mariadb.jdbc.Driver
                    username: sa
                    password: password

                  jpa:
                    database-platform: org.hibernate.dialect.MariaDBDialect
                    hibernate:
                      ddl-auto: update
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true

                server:
                  port: %d
                """.formatted(lower, lower, port);
    }

    private String mssqlYml(String lower, int port) {
        return """
                spring:
                  application:
                    name: %s-service

                  datasource:
                    url: jdbc:sqlserver://localhost:1433;databaseName=%sdb;encrypt=true;trustServerCertificate=true
                    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
                    username: sa
                    password: Password123!

                  jpa:
                    database-platform: org.hibernate.dialect.SQLServerDialect
                    hibernate:
                      ddl-auto: update
                    show-sql: true
                    properties:
                      hibernate:
                        format_sql: true

                server:
                  port: %d
                """.formatted(lower, lower, port);
    }
}
