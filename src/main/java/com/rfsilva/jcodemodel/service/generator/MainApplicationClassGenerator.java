package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.springframework.stereotype.Component;

@Component
public class MainApplicationClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();
        String name = ctx.entityName();

        JDefinedClass cls = ctx.getCm()._package(ctx.pkg())._class(name + "Application");
        cls.annotate(cm.ref("org.springframework.boot.autoconfigure.SpringBootApplication"));

        JMethod main = cls.method(JMod.PUBLIC | JMod.STATIC, cm.VOID, "main");
        JVar args = main.param(cm.ref(String.class).array(), "args");
        main.body().staticInvoke(cm.ref("org.springframework.boot.SpringApplication"), "run")
                .arg(cls.dotclass()).arg(args);

        return cls;
    }
}
