package com.rfsilva.jcodemodel.service.generator.backend;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import org.springframework.stereotype.Component;

@Component
public class ChildResponseDtoClassGenerator extends AbstractChildClassGenerator {

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm          = ctx.getCm();
        String parentCamelId   = toCamelCase(ctx.parentEntityName()) + "Id"; // "salesOrderId"

        JDefinedClass cls = ctx.subPackage("dto")._class(ctx.entityName() + "Response");
        cls.annotate(cm.ref("lombok.Data"));
        cls.annotate(cm.ref("lombok.NoArgsConstructor"));
        cls.annotate(cm.ref("lombok.AllArgsConstructor"));
        cls.annotate(cm.ref("lombok.Builder"));

        cls.field(JMod.PRIVATE, cm.ref(Long.class), "id");
        cls.field(JMod.PRIVATE, cm.ref(Long.class), parentCamelId);   // parentId in response
        ctx.fields().forEach(field ->
                cls.field(JMod.PRIVATE,
                        resolveJType(cm, field.getType()),
                        toCamelCase(field.getName())));
        return cls;
    }
}
