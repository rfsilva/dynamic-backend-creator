package com.rfsilva.jcodemodel.service;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.dto.GenerationResult;
import com.rfsilva.jcodemodel.exception.CodeGenerationException;
import com.rfsilva.jcodemodel.service.generator.*;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private static final String DEFAULT_OUTPUT_DIR =
            System.getProperty("java.io.tmpdir") + "/jcodemodel-generated";

    // ── Parent class generators ───────────────────────────────────────────────
    private final EntityClassGenerator                   entityGenerator;
    private final RequestDtoClassGenerator               requestDtoGenerator;
    private final ResponseDtoClassGenerator              responseDtoGenerator;
    private final NotFoundExceptionClassGenerator        notFoundExceptionGenerator;
    private final GlobalExceptionHandlerClassGenerator   globalExceptionHandlerGenerator;
    private final RepositoryClassGenerator               repositoryGenerator;
    private final MapperClassGenerator                   mapperGenerator;
    private final ServiceClassGenerator                  serviceGenerator;
    private final ControllerClassGenerator               controllerGenerator;
    private final MainApplicationClassGenerator          mainApplicationGenerator;

    // ── Child class generators ────────────────────────────────────────────────
    private final ChildEntityClassGenerator              childEntityGenerator;
    private final ChildRequestDtoClassGenerator          childRequestDtoGenerator;
    private final ChildResponseDtoClassGenerator         childResponseDtoGenerator;
    private final ChildNotFoundExceptionClassGenerator   childNotFoundExceptionGenerator;
    private final ChildRepositoryClassGenerator          childRepositoryGenerator;
    private final ChildMapperClassGenerator              childMapperGenerator;
    private final ChildServiceClassGenerator             childServiceGenerator;
    private final ChildControllerClassGenerator          childControllerGenerator;

    // ── File generators ───────────────────────────────────────────────────────
    private final PomFileGenerator                       pomFileGenerator;
    private final ApplicationYmlFileGenerator            applicationYmlGenerator;
    private final DockerComposeFileGenerator             dockerComposeGenerator;
    private final ReadmeFileGenerator                    readmeGenerator;
    private final GitignoreFileGenerator                 gitignoreGenerator;
    private final PostmanEnvironmentFileGenerator        postmanEnvironmentGenerator;
    private final PostmanCollectionFileGenerator         postmanCollectionGenerator;

    // =========================================================================

    public GenerationResult generate(EntityDefinition definition) {
        String outputDir = resolveOutputDir(definition);
        createOutputDir(outputDir);

        List<String> generatedFiles = new ArrayList<>();
        try {
            generatedFiles.addAll(generateJavaClasses(definition, outputDir));
            generatedFiles.add(pomFileGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            generatedFiles.add(applicationYmlGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            String dockerCompose = dockerComposeGenerator.generate(buildContext(new JCodeModel(), definition), outputDir);
            if (dockerCompose != null) generatedFiles.add(dockerCompose);
            generatedFiles.add(readmeGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            generatedFiles.add(gitignoreGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            generatedFiles.add(postmanEnvironmentGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            generatedFiles.add(postmanCollectionGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
        } catch (JClassAlreadyExistsException e) {
            throw new CodeGenerationException("Class already exists: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new CodeGenerationException("IO error during generation: " + e.getMessage(), e);
        }

        return GenerationResult.builder()
                .entityName(definition.getEntityName())
                .outputDirectory(outputDir)
                .generatedFiles(generatedFiles.stream().filter(Objects::nonNull).toList())
                .message("CRUD application generated successfully")
                .build();
    }

    // =========================================================================
    // Orchestration
    // =========================================================================

    private List<String> generateJavaClasses(EntityDefinition definition, String outputDir)
            throws JClassAlreadyExistsException, IOException {

        JCodeModel cm  = new JCodeModel();
        GenerationContext ctx = buildContext(cm, definition);

        // ── Parent entity and its layers ──────────────────────────────────────
        ctx.setEntityClass(entityGenerator.generate(ctx));
        ctx.setRequestDto(requestDtoGenerator.generate(ctx));
        ctx.setResponseDto(responseDtoGenerator.generate(ctx));
        ctx.setNotFoundException(notFoundExceptionGenerator.generate(ctx));
        ctx.setGlobalExceptionHandler(globalExceptionHandlerGenerator.generate(ctx));
        ctx.setRepositoryClass(repositoryGenerator.generate(ctx));
        ctx.setMapperClass(mapperGenerator.generate(ctx));
        ctx.setServiceClass(serviceGenerator.generate(ctx));
        controllerGenerator.generate(ctx);
        mainApplicationGenerator.generate(ctx);

        // ── Child entities ────────────────────────────────────────────────────
        if (definition.getChildren() != null) {
            for (ChildEntityDefinition childDef : definition.getChildren()) {
                generateChild(ctx, childDef);
            }
        }

        // ── Write all .java files ─────────────────────────────────────────────
        File srcDir = new File(outputDir + "/src/main/java");
        srcDir.mkdirs();
        cm.build(srcDir);

        List<String> files = new ArrayList<>();
        collectJavaFiles(srcDir, files, outputDir);
        return files;
    }

    private void generateChild(GenerationContext ctx, ChildEntityDefinition childDef)
            throws JClassAlreadyExistsException {

        ChildGenerationContext childCtx = new ChildGenerationContext(ctx, childDef);

        // Child entity wires @ManyToOne on child + @OneToMany on parent
        childCtx.setEntityClass(childEntityGenerator.generate(childCtx));
        childCtx.setRequestDto(childRequestDtoGenerator.generate(childCtx));
        childCtx.setResponseDto(childResponseDtoGenerator.generate(childCtx));
        childCtx.setNotFoundException(childNotFoundExceptionGenerator.generate(childCtx));

        // Register child's NotFoundException in the shared GlobalExceptionHandler
        globalExceptionHandlerGenerator.addNotFoundHandler(ctx, childCtx.getNotFoundException());

        childCtx.setRepositoryClass(childRepositoryGenerator.generate(childCtx));
        childCtx.setMapperClass(childMapperGenerator.generate(childCtx));
        childCtx.setServiceClass(childServiceGenerator.generate(childCtx));
        childControllerGenerator.generate(childCtx);

        ctx.addChildContext(childCtx);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private GenerationContext buildContext(JCodeModel cm, EntityDefinition definition) {
        return new GenerationContext(cm, definition);
    }

    private void createOutputDir(String outputDir) {
        try {
            Files.createDirectories(Path.of(outputDir));
        } catch (IOException e) {
            throw new CodeGenerationException("Cannot create output directory: " + outputDir, e);
        }
    }

    private String resolveOutputDir(EntityDefinition definition) {
        if (definition.getOutputDirectory() != null && !definition.getOutputDirectory().isBlank()) {
            return definition.getOutputDirectory();
        }
        return DEFAULT_OUTPUT_DIR + "/" + definition.getEntityName().toLowerCase();
    }

    private void collectJavaFiles(File dir, List<String> files, String basePath) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File f : children) {
            if (f.isDirectory()) collectJavaFiles(f, files, basePath);
            else if (f.getName().endsWith(".java"))
                files.add(f.getAbsolutePath().replace(basePath + "/", "").replace("\\", "/"));
        }
    }
}
