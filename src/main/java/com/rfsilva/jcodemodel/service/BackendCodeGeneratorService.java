package com.rfsilva.jcodemodel.service;

import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.exception.CodeGenerationException;
import com.rfsilva.jcodemodel.service.generator.backend.*;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrates all backend (Spring Boot) generators.
 * Delegates Java class generation to {@link JavaClassGeneratorService} and
 * writes pom.xml, application.yml, docker-compose, README, .gitignore,
 * and Postman files directly.
 */
@Service
@RequiredArgsConstructor
public class BackendCodeGeneratorService {

    private final JavaClassGeneratorService      javaClassGeneratorService;

    // ── File generators ───────────────────────────────────────────────────────
    private final PomFileGenerator               pomFileGenerator;
    private final ApplicationYmlFileGenerator    applicationYmlGenerator;
    private final CorsConfigFileGenerator        corsConfigGenerator;
    private final DockerComposeFileGenerator     dockerComposeGenerator;
    private final ReadmeFileGenerator            readmeGenerator;
    private final GitignoreFileGenerator         gitignoreGenerator;
    private final PostmanEnvironmentFileGenerator postmanEnvironmentGenerator;
    private final PostmanCollectionFileGenerator  postmanCollectionGenerator;

    // =========================================================================

    public List<String> generate(EntityDefinition definition, String backendDir) {
        List<String> files = new ArrayList<>();
        try {
            JCodeModel cm  = new JCodeModel();
            GenerationContext ctx = new GenerationContext(cm, definition);

            files.addAll(javaClassGeneratorService.generate(ctx, backendDir));
            files.add(pomFileGenerator.generate(ctx, backendDir));
            files.add(applicationYmlGenerator.generate(ctx, backendDir));
            files.add(corsConfigGenerator.generate(ctx, backendDir));
            String dockerCompose = dockerComposeGenerator.generate(ctx, backendDir);
            if (dockerCompose != null) files.add(dockerCompose);
            files.add(readmeGenerator.generate(ctx, backendDir));
            files.add(gitignoreGenerator.generate(ctx, backendDir));
            files.add(postmanEnvironmentGenerator.generate(ctx, backendDir));
            files.add(postmanCollectionGenerator.generate(ctx, backendDir));
        } catch (JClassAlreadyExistsException e) {
            throw new CodeGenerationException("Class already exists: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new CodeGenerationException("IO error during generation: " + e.getMessage(), e);
        }
        return files.stream().filter(Objects::nonNull).toList();
    }

    /** Builds a context (without class generation) for use by the frontend generator. */
    public GenerationContext buildContext(EntityDefinition definition) {
        return new GenerationContext(new JCodeModel(), definition);
    }
}
