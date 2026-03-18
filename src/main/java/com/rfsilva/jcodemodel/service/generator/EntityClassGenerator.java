package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.*;
import org.springframework.stereotype.Component;

@Component
public class EntityClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();
        String name = ctx.entityName();

        JDefinedClass cls = ctx.subPackage("entity")._class(name);
        cls.annotate(cm.ref("jakarta.persistence.Entity"));
        cls.annotate(cm.ref("jakarta.persistence.Table"))
                .param("name", toSnakeCase(name).toUpperCase());
        cls.annotate(cm.ref("lombok.Getter"));
        cls.annotate(cm.ref("lombok.Setter"));
        cls.annotate(cm.ref("lombok.NoArgsConstructor"));
        cls.annotate(cm.ref("lombok.AllArgsConstructor"));
        cls.annotate(cm.ref("lombok.Builder"));

        JFieldVar id = cls.field(JMod.PRIVATE, cm.ref(Long.class), "id");
        id.annotate(cm.ref("jakarta.persistence.Id"));
        id.annotate(cm.ref("jakarta.persistence.GeneratedValue"))
                .param("strategy", cm.ref("jakarta.persistence.GenerationType").staticRef("IDENTITY"));

        for (FieldDefinition field : ctx.fields()) {
            JFieldVar f = cls.field(JMod.PRIVATE,
                    resolveJType(cm, field.getType()),
                    toCamelCase(field.getName()));
            if (field.getType() == FieldDefinition.FieldType.STRING && field.getMaxLength() != null) {
                f.annotate(cm.ref("jakarta.persistence.Column"))
                        .param("length", field.getMaxLength());
            }
        }

        return cls;
    }
}
