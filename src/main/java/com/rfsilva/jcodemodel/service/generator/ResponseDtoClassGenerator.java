package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import org.springframework.stereotype.Component;

@Component
public class ResponseDtoClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();

        JDefinedClass cls = ctx.subPackage("dto")._class(ctx.entityName() + "Response");
        cls.annotate(cm.ref("lombok.Data"));
        cls.annotate(cm.ref("lombok.NoArgsConstructor"));
        cls.annotate(cm.ref("lombok.AllArgsConstructor"));
        cls.annotate(cm.ref("lombok.Builder"));

        cls.field(JMod.PRIVATE, cm.ref(Long.class), "id");
        ctx.fields().forEach(field ->
                cls.field(JMod.PRIVATE,
                        resolveJType(cm, field.getType()),
                        toCamelCase(field.getName())));

        return cls;
    }
}
