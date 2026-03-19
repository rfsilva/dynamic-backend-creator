package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates src/app/app.component.ts and app.component.html —
 * root shell with a Material sidenav and router-outlet.
 */
@Component
public class AppComponentFrontendGenerator extends AbstractFrontendGenerator {

    private static final String APP_SCSS = """
            .sidenav-container {
              height: calc(100vh - 64px);
            }

            .mat-sidenav {
              width: 220px;
              padding: 8px 0;
            }

            a.active {
              background: rgba(0, 0, 0, 0.08);
              font-weight: 500;
            }
            """;

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity = ctx.entityName();
        String kebab  = toKebabCase(entity);

        String ts   = buildTs(entity);
        String html = buildHtml(entity, kebab);
        String scss = APP_SCSS;

        return List.of(
            writeFile(frontendDir, "src/app/app.component.ts",   ts),
            writeFile(frontendDir, "src/app/app.component.html", html),
            writeFile(frontendDir, "src/app/app.component.scss", scss)
        );
    }

    private String buildTs(String entity) {
        return """
                import { Component } from '@angular/core';
                import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
                import { MatToolbarModule } from '@angular/material/toolbar';
                import { MatSidenavModule } from '@angular/material/sidenav';
                import { MatListModule } from '@angular/material/list';
                import { MatIconModule } from '@angular/material/icon';
                import { MatButtonModule } from '@angular/material/button';

                @Component({
                  selector: 'app-root',
                  standalone: true,
                  imports: [
                    RouterOutlet,
                    RouterLink,
                    RouterLinkActive,
                    MatToolbarModule,
                    MatSidenavModule,
                    MatListModule,
                    MatIconModule,
                    MatButtonModule,
                  ],
                  templateUrl: './app.component.html',
                  styleUrl: './app.component.scss',
                })
                export class AppComponent {
                  title = '%s App';
                }
                """.formatted(entity);
    }

    private String buildHtml(String entity, String kebab) {
        StringBuilder sb = new StringBuilder();
        sb.append("<mat-toolbar color=\"primary\">\n");
        sb.append("  <span>").append(entity).append(" App</span>\n");
        sb.append("</mat-toolbar>\n\n");

        sb.append("<mat-sidenav-container class=\"sidenav-container\">\n");
        sb.append("  <mat-sidenav mode=\"side\" opened>\n");
        sb.append("    <mat-nav-list>\n");
        sb.append("      <a mat-list-item routerLink=\"/").append(kebab).append("s\" routerLinkActive=\"active\">\n");
        sb.append("        <mat-icon matListItemIcon>list</mat-icon>\n");
        sb.append("        <span matListItemTitle>").append(entity).append("s</span>\n");
        sb.append("      </a>\n");

        // Child entities are accessible via nested routes under the parent — no top-level nav links needed.

        sb.append("    </mat-nav-list>\n");
        sb.append("  </mat-sidenav>\n\n");
        sb.append("  <mat-sidenav-content class=\"page-container\">\n");
        sb.append("    <router-outlet />\n");
        sb.append("  </mat-sidenav-content>\n");
        sb.append("</mat-sidenav-container>\n");

        return sb.toString();
    }

}
