package com.rfsilva.jcodemodel.service;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import com.rfsilva.jcodemodel.service.generator.frontend.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates all Angular frontend file generators.
 * Delegates to each generator in declaration order and collects relative file paths.
 */
@Service
@RequiredArgsConstructor
public class FrontendCodeGeneratorService {

    // ── Infrastructure ────────────────────────────────────────────────────────
    private final PackageJsonFrontendGenerator    packageJsonGenerator;
    private final AngularJsonFrontendGenerator    angularJsonGenerator;
    private final TsConfigFrontendGenerator       tsConfigGenerator;
    private final MainTsFrontendGenerator         mainTsGenerator;
    private final IndexHtmlFrontendGenerator      indexHtmlGenerator;
    private final StylesFrontendGenerator         stylesGenerator;
    private final EnvironmentFrontendGenerator    environmentGenerator;
    private final AppConfigFrontendGenerator      appConfigGenerator;
    private final AppRoutesFrontendGenerator      appRoutesGenerator;
    private final AppComponentFrontendGenerator   appComponentGenerator;

    // ── Entity ────────────────────────────────────────────────────────────────
    private final EntityModelFrontendGenerator        entityModelGenerator;
    private final EntityServiceFrontendGenerator      entityServiceGenerator;
    private final EntityListComponentFrontendGenerator entityListGenerator;
    private final EntityFormComponentFrontendGenerator entityFormGenerator;
    private final EntityRoutesFrontendGenerator        entityRoutesGenerator;

    // ── Child entities ────────────────────────────────────────────────────────
    private final ChildEntityModelFrontendGenerator        childModelGenerator;
    private final ChildEntityServiceFrontendGenerator      childServiceGenerator;
    private final ChildEntityListComponentFrontendGenerator childListGenerator;
    private final ChildEntityFormComponentFrontendGenerator childFormGenerator;

    // =========================================================================

    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        List<String> files = new ArrayList<>();

        // Infrastructure
        files.addAll(packageJsonGenerator.generate(ctx, frontendDir));
        files.addAll(angularJsonGenerator.generate(ctx, frontendDir));
        files.addAll(tsConfigGenerator.generate(ctx, frontendDir));
        files.addAll(mainTsGenerator.generate(ctx, frontendDir));
        files.addAll(indexHtmlGenerator.generate(ctx, frontendDir));
        files.addAll(stylesGenerator.generate(ctx, frontendDir));
        files.addAll(environmentGenerator.generate(ctx, frontendDir));
        files.addAll(appConfigGenerator.generate(ctx, frontendDir));
        files.addAll(appRoutesGenerator.generate(ctx, frontendDir));
        files.addAll(appComponentGenerator.generate(ctx, frontendDir));

        // Parent entity
        files.addAll(entityModelGenerator.generate(ctx, frontendDir));
        files.addAll(entityServiceGenerator.generate(ctx, frontendDir));
        files.addAll(entityListGenerator.generate(ctx, frontendDir));
        files.addAll(entityFormGenerator.generate(ctx, frontendDir));
        files.addAll(entityRoutesGenerator.generate(ctx, frontendDir));

        // Child entities (each generator iterates children internally)
        files.addAll(childModelGenerator.generate(ctx, frontendDir));
        files.addAll(childServiceGenerator.generate(ctx, frontendDir));
        files.addAll(childListGenerator.generate(ctx, frontendDir));
        files.addAll(childFormGenerator.generate(ctx, frontendDir));

        return files;
    }
}
