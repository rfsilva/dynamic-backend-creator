package com.rfsilva.jcodemodel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of the code generation process.
 */
@Data
@Builder
public class GenerationResult {

    private String entityName;
    private String outputDirectory;
    private List<String> generatedFiles;
    private String message;
}
