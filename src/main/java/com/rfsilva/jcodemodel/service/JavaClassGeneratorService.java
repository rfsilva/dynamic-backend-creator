package com.rfsilva.jcodemodel.service;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.*;
import com.sun.codemodel.JClassAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates all JCodeModel class generators (parent + child entities).
 * Writes .java source files to {@code outputDir/src/main/java}.
 */
@Service
@RequiredArgsConstructor
public class JavaClassGeneratorService {

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

    // =========================================================================

    /**
     * Generates all Java classes for the given context and writes them under
     * {@code outputDir/src/main/java}. Returns the list of relative file paths.
     */
    public List<String> generate(GenerationContext ctx, String outputDir)
            throws JClassAlreadyExistsException, IOException {

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

        if (ctx.getDefinition().getChildren() != null) {
            for (ChildEntityDefinition childDef : ctx.getDefinition().getChildren()) {
                generateChild(ctx, childDef);
            }
        }

        File srcDir = new File(outputDir + "/src/main/java");
        srcDir.mkdirs();
        ctx.getCm().build(srcDir);

        List<String> files = new ArrayList<>();
        collectJavaFiles(srcDir, files, outputDir);
        return files;
    }

    // =========================================================================

    private void generateChild(GenerationContext ctx, ChildEntityDefinition childDef)
            throws JClassAlreadyExistsException {

        ChildGenerationContext childCtx = new ChildGenerationContext(ctx, childDef);

        childCtx.setEntityClass(childEntityGenerator.generate(childCtx));
        childCtx.setRequestDto(childRequestDtoGenerator.generate(childCtx));
        childCtx.setResponseDto(childResponseDtoGenerator.generate(childCtx));
        childCtx.setNotFoundException(childNotFoundExceptionGenerator.generate(childCtx));

        globalExceptionHandlerGenerator.addNotFoundHandler(ctx, childCtx.getNotFoundException());

        childCtx.setRepositoryClass(childRepositoryGenerator.generate(childCtx));
        childCtx.setMapperClass(childMapperGenerator.generate(childCtx));
        childCtx.setServiceClass(childServiceGenerator.generate(childCtx));
        childControllerGenerator.generate(childCtx);

        ctx.addChildContext(childCtx);
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
