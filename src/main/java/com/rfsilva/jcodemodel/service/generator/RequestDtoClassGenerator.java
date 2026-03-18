package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.*;
import org.springframework.stereotype.Component;

@Component
public class RequestDtoClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();

        JDefinedClass cls = ctx.subPackage("dto")._class(ctx.entityName() + "Request");
        cls.annotate(cm.ref("lombok.Data"));
        cls.annotate(cm.ref("lombok.NoArgsConstructor"));
        cls.annotate(cm.ref("lombok.AllArgsConstructor"));
        cls.annotate(cm.ref("lombok.Builder"));

        for (FieldDefinition field : ctx.fields()) {
            JFieldVar f = cls.field(JMod.PRIVATE,
                    resolveJType(cm, field.getType()),
                    toCamelCase(field.getName()));

            if (field.isRequired()) {
                if (field.getType() == FieldDefinition.FieldType.STRING) {
                    f.annotate(cm.ref("jakarta.validation.constraints.NotBlank"));
                } else {
                    f.annotate(cm.ref("jakarta.validation.constraints.NotNull"));
                }
            }
            if (field.getType() == FieldDefinition.FieldType.STRING && field.getMaxLength() != null) {
                f.annotate(cm.ref("jakarta.validation.constraints.Size"))
                        .param("max", field.getMaxLength());
            }
        }

        return cls;
    }
}
