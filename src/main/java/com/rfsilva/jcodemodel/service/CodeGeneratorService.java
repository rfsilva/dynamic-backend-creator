package com.rfsilva.jcodemodel.service;

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

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private static final String DEFAULT_OUTPUT_DIR =
            System.getProperty("java.io.tmpdir") + "/jcodemodel-generated";

    // Java class generators
    private final EntityClassGenerator              entityGenerator;
    private final RequestDtoClassGenerator          requestDtoGenerator;
    private final ResponseDtoClassGenerator         responseDtoGenerator;
    private final NotFoundExceptionClassGenerator   notFoundExceptionGenerator;
    private final GlobalExceptionHandlerClassGenerator globalExceptionHandlerGenerator;
    private final RepositoryClassGenerator          repositoryGenerator;
    private final MapperClassGenerator              mapperGenerator;
    private final ServiceClassGenerator             serviceGenerator;
    private final ControllerClassGenerator          controllerGenerator;
    private final MainApplicationClassGenerator     mainApplicationGenerator;

    // Non-Java file generators
    private final PomFileGenerator                  pomFileGenerator;
    private final ApplicationPropertiesFileGenerator applicationPropertiesGenerator;

    public GenerationResult generate(EntityDefinition definition) {
        String outputDir = resolveOutputDir(definition);
        createOutputDir(outputDir);

        List<String> generatedFiles = new ArrayList<>();
        try {
            generatedFiles.addAll(generateJavaClasses(definition, outputDir));
            generatedFiles.add(pomFileGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
            generatedFiles.add(applicationPropertiesGenerator.generate(buildContext(new JCodeModel(), definition), outputDir));
        } catch (JClassAlreadyExistsException e) {
            throw new CodeGenerationException("Class already exists: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new CodeGenerationException("IO error during generation: " + e.getMessage(), e);
        }

        return GenerationResult.builder()
                .entityName(definition.getEntityName())
                .outputDirectory(outputDir)
                .generatedFiles(generatedFiles)
                .message("CRUD application generated successfully")
                .build();
    }

    // -------------------------------------------------------------------------
    // Orchestration
    // -------------------------------------------------------------------------

    private List<String> generateJavaClasses(EntityDefinition definition, String outputDir)
            throws JClassAlreadyExistsException, IOException {

        JCodeModel cm = new JCodeModel();
        GenerationContext ctx = buildContext(cm, definition);

        // Each generator populates ctx so the next one can reference prior results
        ctx.setEntityClass(entityGenerator.generate(ctx));
        ctx.setRequestDto(requestDtoGenerator.generate(ctx));
        ctx.setResponseDto(responseDtoGenerator.generate(ctx));
        ctx.setNotFoundException(notFoundExceptionGenerator.generate(ctx));
        globalExceptionHandlerGenerator.generate(ctx);          // result not referenced by others
        ctx.setRepositoryClass(repositoryGenerator.generate(ctx));
        ctx.setMapperClass(mapperGenerator.generate(ctx));
        ctx.setServiceClass(serviceGenerator.generate(ctx));
        controllerGenerator.generate(ctx);                      // result not referenced by others
        mainApplicationGenerator.generate(ctx);                 // result not referenced by others

        File srcDir = new File(outputDir + "/src/main/java");
        srcDir.mkdirs();
        cm.build(srcDir);

        List<String> files = new ArrayList<>();
        collectJavaFiles(srcDir, files, outputDir);
        return files;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
