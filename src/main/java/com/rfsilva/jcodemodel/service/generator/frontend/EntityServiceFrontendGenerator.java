package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates the Angular service for the parent entity using HttpClient and signals.
 * Output: src/app/{entity-kebab}/{entity-kebab}.service.ts
 */
@Component
public class EntityServiceFrontendGenerator extends AbstractFrontendGenerator {

    @SuppressWarnings("java:S1075")
    private static String servicePath(String kebab) {
        return srcApp(kebab) + "/" + kebab + ".service.ts";
    }

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity  = ctx.entityName();
        String kebab   = toKebabCase(entity);
        String apiPath = apiBasePath(entity);

        String content = buildContent(entity, kebab, apiPath);

        String path = servicePath(kebab);
        return List.of(writeFile(frontendDir, path, content));
    }

    private String buildContent(String entity, String kebab, String apiPath) {
        return "import { inject, Injectable, signal } from '@angular/core';\n"
             + "import { HttpClient } from '@angular/common/http';\n"
             + "import { Observable, tap } from 'rxjs';\n"
             + "import { environment } from '../../environments/environment';\n"
             + "import { " + entity + ", " + entity + "Request } from './" + kebab + ".model';\n\n"
             + "@Injectable({ providedIn: 'root' })\n"
             + "export class " + entity + "Service {\n"
             + "  private readonly http = inject(HttpClient);\n"
             + "  private readonly baseUrl = `${environment.apiUrl}" + apiPath + "`;\n\n"
             + "  readonly items = signal<" + entity + "[]>([]);\n\n"
             + "  findAll(): Observable<" + entity + "[]> {\n"
             + "    return this.http.get<" + entity + "[]>(this.baseUrl).pipe(\n"
             + "      tap(data => this.items.set(data))\n"
             + "    );\n"
             + "  }\n\n"
             + "  findById(id: number): Observable<" + entity + "> {\n"
             + "    return this.http.get<" + entity + ">(`${this.baseUrl}/${id}`);\n"
             + "  }\n\n"
             + "  create(request: " + entity + "Request): Observable<" + entity + "> {\n"
             + "    return this.http.post<" + entity + ">(this.baseUrl, request).pipe(\n"
             + "      tap(() => this.findAll().subscribe())\n"
             + "    );\n"
             + "  }\n\n"
             + "  update(id: number, request: " + entity + "Request): Observable<" + entity + "> {\n"
             + "    return this.http.put<" + entity + ">(`${this.baseUrl}/${id}`, request).pipe(\n"
             + "      tap(() => this.findAll().subscribe())\n"
             + "    );\n"
             + "  }\n\n"
             + "  delete(id: number): Observable<void> {\n"
             + "    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(\n"
             + "      tap(() => this.findAll().subscribe())\n"
             + "    );\n"
             + "  }\n"
             + "}\n";
    }
}
