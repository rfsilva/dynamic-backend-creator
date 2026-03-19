package com.rfsilva.jcodemodel.service;

import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.dto.GenerationResult;
import com.rfsilva.jcodemodel.exception.CodeGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private final BackendCodeGeneratorService  backendGenerator;
    private final FrontendCodeGeneratorService frontendGenerator;

    // =========================================================================

    public GenerationResult generate(EntityDefinition definition) {
        String outputDir   = resolveOutputDir(definition);
        String backendDir  = outputDir + "/backend";
        String frontendDir = outputDir + "/frontend";
        createOutputDir(backendDir);
        createOutputDir(frontendDir);

        List<String> files = new ArrayList<>();
        try {
            files.addAll(backendGenerator.generate(definition, backendDir));
            files.addAll(frontendGenerator.generate(
                    backendGenerator.buildContext(definition), frontendDir));
        } catch (IOException e) {
            throw new CodeGenerationException("IO error during generation: " + e.getMessage(), e);
        }

        return GenerationResult.builder()
                .entityName(definition.getEntityName())
                .outputDirectory(outputDir)
                .generatedFiles(files)
                .message("CRUD application generated successfully")
                .build();
    }

    // =========================================================================

    private String resolveOutputDir(EntityDefinition definition) {
        if (definition.getOutputDirectory() != null && !definition.getOutputDirectory().isBlank()) {
            return definition.getOutputDirectory();
        }
        return DEFAULT_OUTPUT_DIR + "/" + definition.getEntityName().toLowerCase();
    }

    private void createOutputDir(String dir) {
        try {
            Files.createDirectories(Path.of(dir));
        } catch (IOException e) {
            throw new CodeGenerationException("Cannot create output directory: " + dir, e);
        }
    }
}
