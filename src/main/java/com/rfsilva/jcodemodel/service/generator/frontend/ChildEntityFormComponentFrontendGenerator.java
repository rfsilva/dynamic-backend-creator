package com.rfsilva.jcodemodel.service.generator.frontend;

import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.service.generator.backend.GenerationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates form components (create/edit) for each child entity.
 * Receives parentId and optionally childId via route params (withComponentInputBinding).
 * Output: src/app/{child-kebab}/{child-kebab}-form/
 */
@Component
public class ChildEntityFormComponentFrontendGenerator extends AbstractFrontendGenerator {

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
        String entity  = child.getEntityName();
        String kebab   = toKebabCase(entity);
        String formDir = srcApp(kebab) + "/" + kebab + "-form";

        String ts   = buildTs(entity, kebab, parentName, child.getFields());
        String html = buildHtml(entity, kebab, parentName, child.getFields());

        return List.of(
            writeFile(frontendDir, formDir + "/" + kebab + "-form.component.ts",   ts),
            writeFile(frontendDir, formDir + "/" + kebab + "-form.component.html", html),
            writeFile(frontendDir, formDir + "/" + kebab + "-form.component.scss", "")
        );
    }

    private String buildTs(String entity, String kebab, String parentName,
                            List<FieldDefinition> fields) {
        boolean hasRequired = hasRequiredFields(fields);
        boolean hasBoolean  = fields.stream().anyMatch(f -> f.getType() == FieldDefinition.FieldType.BOOLEAN);
        String validatorsImport = hasRequired ? ", Validators" : "";
        String checkboxImport   = hasBoolean  ? "import { MatCheckboxModule } from '@angular/material/checkbox';\n" : "";
        String checkboxModule   = hasBoolean  ? "    MatCheckboxModule,\n" : "";
        String parentKebab = toKebabCase(parentName);

        StringBuilder formFields = new StringBuilder();
        for (FieldDefinition f : fields) {
            String def = toTsDefaultValue(f.getType());
            String val = f.isRequired() ? ", [Validators.required]" : "";
            formFields.append("      ").append(f.getName()).append(": [").append(def).append(val).append("],\n");
        }

        StringBuilder patchFields = new StringBuilder();
        for (FieldDefinition f : fields) {
            patchFields.append("          ").append(f.getName()).append(": item.").append(f.getName()).append(",\n");
        }

        StringBuilder requestFields = new StringBuilder();
        for (FieldDefinition f : fields) {
            requestFields.append("      ").append(f.getName()).append(": this.form.value.").append(f.getName()).append("!,\n");
        }

        return "import { Component, inject, input, OnInit, signal } from '@angular/core';\n"
             + "import { Router } from '@angular/router';\n"
             + "import { FormBuilder, ReactiveFormsModule" + validatorsImport + " } from '@angular/forms';\n"
             + "import { MatFormFieldModule } from '@angular/material/form-field';\n"
             + "import { MatInputModule } from '@angular/material/input';\n"
             + checkboxImport
             + "import { MatButtonModule } from '@angular/material/button';\n"
             + "import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';\n"
             + "import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';\n"
             + "import { " + entity + "Service } from '../" + kebab + ".service';\n\n"
             + "@Component({\n"
             + "  selector: 'app-" + kebab + "-form',\n"
             + "  standalone: true,\n"
             + "  imports: [\n"
             + "    ReactiveFormsModule,\n"
             + "    MatFormFieldModule,\n"
             + "    MatInputModule,\n"
             + checkboxModule
             + "    MatButtonModule,\n"
             + "    MatSnackBarModule,\n"
             + "    MatProgressSpinnerModule,\n"
             + "  ],\n"
             + "  templateUrl: './" + kebab + "-form.component.html',\n"
             + "  styleUrl: './" + kebab + "-form.component.scss',\n"
             + "})\n"
             + "export class " + entity + "FormComponent implements OnInit {\n"
             + "  private readonly service  = inject(" + entity + "Service);\n"
             + "  private readonly router   = inject(Router);\n"
             + "  private readonly fb       = inject(FormBuilder);\n"
             + "  private readonly snackBar = inject(MatSnackBar);\n\n"
             + "  readonly id       = input.required<number>();\n"
             + "  readonly childId  = input<number | undefined>(undefined);\n"
             + "  readonly loading  = signal(false);\n"
             + "  readonly isEdit   = signal(false);\n\n"
             + "  form = this.fb.group({\n"
             + formFields
             + "  });\n\n"
             + "  ngOnInit(): void {\n"
             + "    const cId = this.childId();\n"
             + "    if (cId !== undefined) {\n"
             + "      this.isEdit.set(true);\n"
             + "      this.loading.set(true);\n"
             + "      this.service.findById(this.id(), cId).subscribe({\n"
             + "        next: item => {\n"
             + "          this.form.patchValue({\n"
             + patchFields
             + "          });\n"
             + "          this.loading.set(false);\n"
             + "        },\n"
             + "        error: () => {\n"
             + "          this.loading.set(false);\n"
             + "          this.snackBar.open('Error loading " + entity + "', 'Close', { duration: 3000 });\n"
             + "        },\n"
             + "      });\n"
             + "    }\n"
             + "  }\n\n"
             + "  onSubmit(): void {\n"
             + "    if (this.form.invalid) return;\n"
             + "    const request = {\n"
             + requestFields
             + "    };\n"
             + "    const cId = this.childId();\n"
             + "    const action$ = cId !== undefined\n"
             + "      ? this.service.update(this.id(), cId, request)\n"
             + "      : this.service.create(this.id(), request);\n\n"
             + "    action$.subscribe({\n"
             + "      next: () => {\n"
             + "        this.snackBar.open('" + entity + " saved', 'Close', { duration: 2000 });\n"
             + "        this.router.navigate(['/" + parentKebab + "s', this.id(), '" + kebab + "s']);\n"
             + "      },\n"
             + "      error: () => this.snackBar.open('Error saving " + entity + "', 'Close', { duration: 3000 }),\n"
             + "    });\n"
             + "  }\n\n"
             + "  onCancel(): void {\n"
             + "    this.router.navigate(['/" + parentKebab + "s', this.id(), '" + kebab + "s']);\n"
             + "  }\n"
             + "}\n";
    }

    private String buildHtml(String entity, String kebab, String parentName,
                              List<FieldDefinition> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"page-container\">\n");
        sb.append("  <h2>{{ isEdit() ? 'Edit' : 'New' }} ").append(entity).append("</h2>\n\n");

        sb.append("  @if (loading()) {\n");
        sb.append("    <mat-spinner />\n");
        sb.append("  } @else {\n");
        sb.append("    <form [formGroup]=\"form\" (ngSubmit)=\"onSubmit()\">\n");
        sb.append("      <div class=\"form-row\">\n");

        for (FieldDefinition f : fields) {
            String inputType = toInputType(f.getType());
            if ("checkbox".equals(inputType)) {
                sb.append("        <mat-checkbox formControlName=\"").append(f.getName()).append("\">\n");
                sb.append("          ").append(capitalize(f.getName())).append("\n");
                sb.append("        </mat-checkbox>\n");
            } else {
                sb.append("        <mat-form-field appearance=\"outline\">\n");
                sb.append("          <mat-label>").append(capitalize(f.getName())).append("</mat-label>\n");
                sb.append("          <input matInput type=\"").append(inputType).append("\" formControlName=\"").append(f.getName()).append("\">\n");
                if (f.isRequired()) {
                    sb.append("          <mat-error>").append(capitalize(f.getName())).append(" is required</mat-error>\n");
                }
                sb.append("        </mat-form-field>\n");
            }
        }

        sb.append("      </div>\n\n");
        sb.append("      <div class=\"form-actions\">\n");
        sb.append("        <button mat-button type=\"button\" (click)=\"onCancel()\">Cancel</button>\n");
        sb.append("        <button mat-raised-button color=\"primary\" type=\"submit\" [disabled]=\"form.invalid\">\n");
        sb.append("          {{ isEdit() ? 'Update' : 'Create' }}\n");
        sb.append("        </button>\n");
        sb.append("      </div>\n");
        sb.append("    </form>\n");
        sb.append("  }\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
