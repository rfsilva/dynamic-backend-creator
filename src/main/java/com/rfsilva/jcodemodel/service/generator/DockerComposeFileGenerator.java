package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.DatabaseType;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates a docker-compose.yml for the selected database.
 * H2 is in-memory — no docker-compose is generated for it.
 */
@Component
public class DockerComposeFileGenerator implements FileGenerator {

    private static final String FILE_NAME = "docker-compose.yml";

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        DatabaseType db = ctx.databaseType();
        if (db == DatabaseType.H2) {
            return null; // H2 is embedded — no container needed
        }

        String lower   = ctx.entityName().toLowerCase();
        String content = buildCompose(db, lower);

        try (FileWriter fw = new FileWriter(outputDir + "/" + FILE_NAME)) {
            fw.write(content);
        }
        return FILE_NAME;
    }

    private String buildCompose(DatabaseType db, String lower) {
        return switch (db) {
            case POSTGRESQL -> postgresCompose(lower);
            case MYSQL      -> mysqlCompose(lower);
            case MARIADB    -> mariadbCompose(lower);
            case MSSQL      -> mssqlCompose(lower);
            default         -> throw new IllegalStateException("Unexpected db: " + db);
        };
    }

    private String postgresCompose(String lower) {
        return """
                services:
                  db:
                    image: postgres:16
                    container_name: %s-postgres
                    environment:
                      POSTGRES_DB: %sdb
                      POSTGRES_USER: sa
                      POSTGRES_PASSWORD: password
                    ports:
                      - "5432:5432"
                    volumes:
                      - %s_postgres_data:/var/lib/postgresql/data
                    restart: unless-stopped

                volumes:
                  %s_postgres_data:
                """.formatted(lower, lower, lower, lower);
    }

    private String mysqlCompose(String lower) {
        return """
                services:
                  db:
                    image: mysql:8
                    container_name: %s-mysql
                    environment:
                      MYSQL_DATABASE: %sdb
                      MYSQL_USER: sa
                      MYSQL_PASSWORD: password
                      MYSQL_ROOT_PASSWORD: rootpassword
                    ports:
                      - "3306:3306"
                    volumes:
                      - %s_mysql_data:/var/lib/mysql
                    restart: unless-stopped

                volumes:
                  %s_mysql_data:
                """.formatted(lower, lower, lower, lower);
    }

    private String mariadbCompose(String lower) {
        return """
                services:
                  db:
                    image: mariadb:11
                    container_name: %s-mariadb
                    environment:
                      MARIADB_DATABASE: %sdb
                      MARIADB_USER: sa
                      MARIADB_PASSWORD: password
                      MARIADB_ROOT_PASSWORD: rootpassword
                    ports:
                      - "3306:3306"
                    volumes:
                      - %s_mariadb_data:/var/lib/mysql
                    restart: unless-stopped

                volumes:
                  %s_mariadb_data:
                """.formatted(lower, lower, lower, lower);
    }

    private String mssqlCompose(String lower) {
        return """
                services:
                  db:
                    image: mcr.microsoft.com/mssql/server:2022-latest
                    container_name: %s-mssql
                    environment:
                      ACCEPT_EULA: "Y"
                      MSSQL_SA_PASSWORD: "Password123!"
                    ports:
                      - "1433:1433"
                    volumes:
                      - %s_mssql_data:/var/opt/mssql
                    restart: unless-stopped

                volumes:
                  %s_mssql_data:
                """.formatted(lower, lower, lower);
    }
}
