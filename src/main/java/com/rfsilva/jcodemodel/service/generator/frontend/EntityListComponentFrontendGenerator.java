package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Generates the entity list component (ts + html) using signals, inject(), and Angular Material table.
 * Output: src/app/{entity-kebab}/{entity-kebab}-list/
 */
@Component
public class EntityListComponentFrontendGenerator extends AbstractFrontendGenerator {

    @Override
    public List<String> generate(GenerationContext ctx, String frontendDir) throws IOException {
        String entity    = ctx.entityName();
        String kebab     = toKebabCase(entity);
        String camel     = toCamelCase(entity);
        String listDir   = srcApp(kebab) + "/" + kebab + "-list";

        String ts   = buildTs(entity, kebab, camel, ctx.fields());
        String html = buildHtml(entity, kebab, camel, ctx.fields());

        return List.of(
            writeFile(frontendDir, listDir + "/" + kebab + "-list.component.ts",   ts),
            writeFile(frontendDir, listDir + "/" + kebab + "-list.component.html", html),
            writeFile(frontendDir, listDir + "/" + kebab + "-list.component.scss", "")
        );
    }

    private String buildTs(String entity, String kebab, String camel, List<FieldDefinition> fields) {
        StringBuilder columns = new StringBuilder();
        for (FieldDefinition f : fields) {
            columns.append("'").append(f.getName()).append("', ");
        }
        String displayedColumns = "['id', " + columns + "'actions']";

        return "import { Component, inject, OnInit, signal } from '@angular/core';\n"
             + "import { Router } from '@angular/router';\n"
             + "import { MatTableModule } from '@angular/material/table';\n"
             + "import { MatButtonModule } from '@angular/material/button';\n"
             + "import { MatIconModule } from '@angular/material/icon';\n"
             + "import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';\n"
             + "import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';\n"
             + "import { " + entity + "Service } from '../" + kebab + ".service';\n"
             + "import { " + entity + " } from '../" + kebab + ".model';\n\n"
             + "@Component({\n"
             + "  selector: 'app-" + kebab + "-list',\n"
             + "  standalone: true,\n"
             + "  imports: [\n"
             + "    MatTableModule,\n"
             + "    MatButtonModule,\n"
             + "    MatIconModule,\n"
             + "    MatProgressSpinnerModule,\n"
             + "    MatSnackBarModule,\n"
             + "  ],\n"
             + "  templateUrl: './" + kebab + "-list.component.html',\n"
             + "  styleUrl: './" + kebab + "-list.component.scss',\n"
             + "})\n"
             + "export class " + entity + "ListComponent implements OnInit {\n"
             + "  private readonly service = inject(" + entity + "Service);\n"
             + "  private readonly router  = inject(Router);\n"
             + "  private readonly snackBar = inject(MatSnackBar);\n\n"
             + "  readonly items    = this.service.items;\n"
             + "  readonly loading  = signal(false);\n"
             + "  readonly columns  = " + displayedColumns + ";\n\n"
             + "  ngOnInit(): void {\n"
             + "    this.loading.set(true);\n"
             + "    this.service.findAll().subscribe({\n"
             + "      next: () => this.loading.set(false),\n"
             + "      error: () => {\n"
             + "        this.loading.set(false);\n"
             + "        this.snackBar.open('Error loading " + entity + "s', 'Close', { duration: 3000 });\n"
             + "      },\n"
             + "    });\n"
             + "  }\n\n"
             + "  onCreate(): void {\n"
             + "    this.router.navigate(['/" + toKebabCase(entity) + "s', 'new']);\n"
             + "  }\n\n"
             + "  onEdit(item: " + entity + "): void {\n"
             + "    this.router.navigate(['/" + toKebabCase(entity) + "s', item.id, 'edit']);\n"
             + "  }\n\n"
             + "  onDelete(item: " + entity + "): void {\n"
             + "    if (!confirm('Delete this " + entity + "?')) return;\n"
             + "    this.service.delete(item.id).subscribe({\n"
             + "      next: () => this.snackBar.open('" + entity + " deleted', 'Close', { duration: 2000 }),\n"
             + "      error: () => this.snackBar.open('Error deleting " + entity + "', 'Close', { duration: 3000 }),\n"
             + "    });\n"
             + "  }\n"
             + "}\n";
    }

    private String buildHtml(String entity, String kebab, String camel, List<FieldDefinition> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-container\">\n");
        sb.append("  <div class=\"action-bar\">\n");
        sb.append("    <h2>").append(entity).append("s</h2>\n");
        sb.append("    <button mat-raised-button color=\"primary\" (click)=\"onCreate()\">\n");
        sb.append("      <mat-icon>add</mat-icon> New ").append(entity).append("\n");
        sb.append("    </button>\n");
        sb.append("  </div>\n\n");

        sb.append("  @if (loading()) {\n");
        sb.append("    <mat-spinner />\n");
        sb.append("  } @else {\n");
        sb.append("    <table mat-table [dataSource]=\"items()\" class=\"mat-elevation-z4\">\n\n");

        // id column
        sb.append("      <ng-container matColumnDef=\"id\">\n");
        sb.append("        <th mat-header-cell *matHeaderCellDef>ID</th>\n");
        sb.append("        <td mat-cell *matCellDef=\"let row\">{{ row.id }}</td>\n");
        sb.append("      </ng-container>\n\n");

        // data columns
        for (FieldDefinition f : fields) {
            sb.append("      <ng-container matColumnDef=\"").append(f.getName()).append("\">\n");
            sb.append("        <th mat-header-cell *matHeaderCellDef>").append(capitalize(f.getName())).append("</th>\n");
            sb.append("        <td mat-cell *matCellDef=\"let row\">{{ row.").append(f.getName()).append(" }}</td>\n");
            sb.append("      </ng-container>\n\n");
        }

        // actions column
        sb.append("      <ng-container matColumnDef=\"actions\">\n");
        sb.append("        <th mat-header-cell *matHeaderCellDef>Actions</th>\n");
        sb.append("        <td mat-cell *matCellDef=\"let row\">\n");
        sb.append("          <button mat-icon-button color=\"primary\" (click)=\"onEdit(row)\">\n");
        sb.append("            <mat-icon>edit</mat-icon>\n");
        sb.append("          </button>\n");
        sb.append("          <button mat-icon-button color=\"warn\" (click)=\"onDelete(row)\">\n");
        sb.append("            <mat-icon>delete</mat-icon>\n");
        sb.append("          </button>\n");
        sb.append("        </td>\n");
        sb.append("      </ng-container>\n\n");

        sb.append("      <tr mat-header-row *matHeaderRowDef=\"columns\"></tr>\n");
        sb.append("      <tr mat-row *matRowDef=\"let row; columns: columns\"></tr>\n");
        sb.append("    </table>\n");
        sb.append("  }\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
