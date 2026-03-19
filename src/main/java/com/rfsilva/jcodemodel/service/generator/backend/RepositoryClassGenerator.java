package com.rfsilva.jcodemodel.service.generator.backend;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.springframework.stereotype.Component;

@Component
public class RepositoryClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();

        JDefinedClass iface = ctx.subPackage("repository")._interface(ctx.entityName() + "Repository");
        iface.annotate(cm.ref("org.springframework.stereotype.Repository"));
        // _implements() on an interface generates "extends" — _extends() fails because
        // cm.ref() returns JClass with isInterface()=false for external types.
        iface._implements(cm.ref("org.springframework.data.jpa.repository.JpaRepository")
                .narrow(ctx.getEntityClass(), cm.ref(Long.class)));

        return iface;
    }
}
