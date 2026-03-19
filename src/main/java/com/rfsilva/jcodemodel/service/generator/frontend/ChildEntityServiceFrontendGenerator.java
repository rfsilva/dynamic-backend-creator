package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the Angular service for each child entity.
 * Child services use nested URLs: /api/{parent}s/{parentId}/{child}s
 * Output: src/app/{child-kebab}/{child-kebab}.service.ts
 */
@Component
public class ChildEntityServiceFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        List<String> generated = new ArrayList<>();
        for (ChildEntityDefinition child : children(ctx)) {
            generated.addAll(generateChild(child, ctx.entityName(), frontendDir));
        }
        return generated;
    }

    private List<String> generateChild(ChildEntityDefinition child, String parentName,
                                        String frontendDir) throws IOException {
        String entity      = child.getEntityName();
        String kebab       = toKebabCase(entity);
        String parentKebab = toKebabCase(parentName);

        String content = buildContent(entity, kebab, parentName, parentKebab);
        String path    = srcApp(kebab) + "/" + kebab + ".service.ts";
        return List.of(writeFile(frontendDir, path, content));
    }

    private String buildContent(String entity, String kebab, String parentName, String parentKebab) {
        String urlTemplate = "/api/" + parentKebab + "s/${parentId}/" + kebab + "s";

        return "import { inject, Injectable, signal } from '@angular/core';\n"
             + "import { HttpClient } from '@angular/common/http';\n"
             + "import { Observable, tap } from 'rxjs';\n"
             + "import { environment } from '../../environments/environment';\n"
             + "import { " + entity + ", " + entity + "Request } from './" + kebab + ".model';\n\n"
             + "@Injectable({ providedIn: 'root' })\n"
             + "export class " + entity + "Service {\n"
             + "  private readonly http = inject(HttpClient);\n\n"
             + "  private baseUrl(parentId: number): string {\n"
             + "    return `${environment.apiUrl}" + urlTemplate + "`;\n"
             + "  }\n\n"
             + "  readonly items = signal<" + entity + "[]>([]);\n\n"
             + "  findAll(parentId: number): Observable<" + entity + "[]> {\n"
             + "    return this.http.get<" + entity + "[]>(this.baseUrl(parentId)).pipe(\n"
             + "      tap(data => this.items.set(data))\n"
             + "    );\n"
             + "  }\n\n"
             + "  findById(parentId: number, id: number): Observable<" + entity + "> {\n"
             + "    return this.http.get<" + entity + ">(`${this.baseUrl(parentId)}/${id}`);\n"
             + "  }\n\n"
             + "  create(parentId: number, request: " + entity + "Request): Observable<" + entity + "> {\n"
             + "    return this.http.post<" + entity + ">(this.baseUrl(parentId), request).pipe(\n"
             + "      tap(() => this.findAll(parentId).subscribe())\n"
             + "    );\n"
             + "  }\n\n"
             + "  update(parentId: number, id: number, request: " + entity + "Request): Observable<" + entity + "> {\n"
             + "    return this.http.put<" + entity + ">(`${this.baseUrl(parentId)}/${id}`, request).pipe(\n"
             + "      tap(() => this.findAll(parentId).subscribe())\n"
             + "    );\n"
             + "  }\n\n"
             + "  delete(parentId: number, id: number): Observable<void> {\n"
             + "    return this.http.delete<void>(`${this.baseUrl(parentId)}/${id}`).pipe(\n"
             + "      tap(() => this.findAll(parentId).subscribe())\n"
             + "    );\n"
             + "  }\n"
             + "}\n";
    }
}
