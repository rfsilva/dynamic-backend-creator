package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates angular.json for the Angular 21 frontend project.
 */
@Component
public class AngularJsonFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String kebab = toKebabCase(ctx.entityName());
        String content = """
                {
                  "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
                  "version": 1,
                  "newProjectRoot": "projects",
                  "projects": {
                    "%s-frontend": {
                      "projectType": "application",
                      "schematics": {
                        "@schematics/angular:component": {
                          "style": "scss",
                          "standalone": true
                        },
                        "@schematics/angular:directive": { "standalone": true },
                        "@schematics/angular:pipe": { "standalone": true }
                      },
                      "root": "",
                      "sourceRoot": "src",
                      "prefix": "app",
                      "architect": {
                        "build": {
                          "builder": "@angular-devkit/build-angular:application",
                          "options": {
                            "outputPath": "dist/%s-frontend",
                            "index": "src/index.html",
                            "browser": "src/main.ts",
                            "polyfills": ["zone.js"],
                            "tsConfig": "tsconfig.app.json",
                            "assets": [
                              { "glob": "**/*", "input": "public" }
                            ],
                            "styles": ["src/styles.scss"],
                            "scripts": []
                          },
                          "configurations": {
                            "production": {
                              "budgets": [
                                { "type": "initial", "maximumWarning": "500kB", "maximumError": "1MB" },
                                { "type": "anyComponentStyle", "maximumWarning": "4kB", "maximumError": "8kB" }
                              ],
                              "outputHashing": "all"
                            },
                            "development": {
                              "optimization": false,
                              "extractLicenses": false,
                              "sourceMap": true
                            }
                          },
                          "defaultConfiguration": "production"
                        },
                        "serve": {
                          "builder": "@angular-devkit/build-angular:dev-server",
                          "configurations": {
                            "production": { "buildTarget": "%s-frontend:build:production" },
                            "development": { "buildTarget": "%s-frontend:build:development" }
                          },
                          "defaultConfiguration": "development"
                        },
                        "test": {
                          "builder": "@angular-devkit/build-angular:karma",
                          "options": {
                            "polyfills": ["zone.js", "zone.js/testing"],
                            "tsConfig": "tsconfig.spec.json",
                            "assets": [{ "glob": "**/*", "input": "public" }],
                            "styles": ["src/styles.scss"],
                            "scripts": []
                          }
                        }
                      }
                    }
                  }
                }
                """.formatted(kebab, kebab, kebab, kebab);

        return List.of(writeFile(frontendDir, "angular.json", content));
    }
}
