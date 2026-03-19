package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates package.json for the Angular 21 / Node 24 frontend.
 */
@Component
public class PackageJsonFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String kebab = toKebabCase(ctx.entityName());
        String content = """
                {
                  "name": "%s-frontend",
                  "version": "0.0.1",
                  "scripts": {
                    "ng": "ng",
                    "start": "ng serve",
                    "build": "ng build",
                    "watch": "ng build --watch --configuration development",
                    "test": "ng test"
                  },
                  "private": true,
                  "dependencies": {
                    "@angular/animations": "^21.0.0",
                    "@angular/cdk": "^21.0.0",
                    "@angular/common": "^21.0.0",
                    "@angular/compiler": "^21.0.0",
                    "@angular/core": "^21.0.0",
                    "@angular/forms": "^21.0.0",
                    "@angular/material": "^21.0.0",
                    "@angular/platform-browser": "^21.0.0",
                    "@angular/platform-browser-dynamic": "^21.0.0",
                    "@angular/router": "^21.0.0",
                    "rxjs": "~7.8.0",
                    "tslib": "^2.3.0",
                    "zone.js": "~0.15.0"
                  },
                  "devDependencies": {
                    "@angular-devkit/build-angular": "^21.0.0",
                    "@angular/cli": "^21.0.0",
                    "@angular/compiler-cli": "^21.0.0",
                    "@types/jasmine": "~5.1.0",
                    "jasmine-core": "~5.4.0",
                    "karma": "~6.4.0",
                    "karma-chrome-launcher": "~3.2.0",
                    "karma-coverage": "~2.2.0",
                    "karma-jasmine": "~5.1.0",
                    "karma-jasmine-html-reporter": "~2.1.0",
                    "typescript": "~5.9.0"
                  },
                  "overrides": {
                    "undici": ">=7.24.0",
                    "glob": "^10.0.0",
                    "rimraf": "^5.0.0"
                  }
                }
                """.formatted(kebab);

        return List.of(writeFile(frontendDir, "package.json", content));
    }
}
