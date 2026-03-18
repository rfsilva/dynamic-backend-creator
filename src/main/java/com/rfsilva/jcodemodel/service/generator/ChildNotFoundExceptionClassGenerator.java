package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.springframework.stereotype.Component;

@Component
public class ChildNotFoundExceptionClassGenerator extends AbstractChildClassGenerator {

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();
        String name   = ctx.entityName();

        JDefinedClass cls = ctx.subPackage("exception")._class(name + "NotFoundException");
        cls._extends(cm.ref(RuntimeException.class));

        JMethod ctor = cls.constructor(JMod.PUBLIC);
        JVar id = ctor.param(cm.ref(Long.class), "id");
        ctor.body().invoke("super")
                .arg(JExpr.lit(name + " not found with id: ").plus(id));

        return cls;
    }
}
