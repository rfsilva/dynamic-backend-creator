package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates tsconfig.json, tsconfig.app.json, and tsconfig.spec.json.
 */
@Component
public class TsConfigFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String base = """
                {
                  "compileOnSave": false,
                  "compilerOptions": {
                    "outDir": "./dist/out-tsc",
                    "strict": true,
                    "noImplicitOverride": true,
                    "noPropertyAccessFromIndexSignature": true,
                    "noImplicitReturns": true,
                    "noFallthroughCasesInSwitch": true,
                    "skipLibCheck": true,
                    "esModuleInterop": true,
                    "sourceMap": true,
                    "declaration": false,
                    "experimentalDecorators": true,
                    "moduleResolution": "bundler",
                    "importHelpers": true,
                    "target": "ES2022",
                    "module": "ES2022",
                    "useDefineForClassFields": false,
                    "lib": ["ES2022", "dom"]
                  },
                  "angularCompilerOptions": {
                    "enableI18nLegacyMessageIdFormat": false,
                    "strictInjectionParameters": true,
                    "strictInputAccessModifiers": true,
                    "strictTemplates": true
                  }
                }
                """;

        String app = """
                {
                  "extends": "./tsconfig.json",
                  "compilerOptions": {
                    "outDir": "./dist/out-tsc",
                    "types": []
                  },
                  "files": ["src/main.ts"],
                  "include": ["src/**/*.d.ts"]
                }
                """;

        String spec = """
                {
                  "extends": "./tsconfig.json",
                  "compilerOptions": {
                    "outDir": "./dist/out-tsc",
                    "types": ["jasmine"]
                  },
                  "include": ["src/**/*.spec.ts", "src/**/*.d.ts"]
                }
                """;

        return List.of(
            writeFile(frontendDir, "tsconfig.json", base),
            writeFile(frontendDir, "tsconfig.app.json", app),
            writeFile(frontendDir, "tsconfig.spec.json", spec)
        );
    }
}
