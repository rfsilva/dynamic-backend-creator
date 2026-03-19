package com.rfsilva.jcodemodel.service.generator.backend;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChildRepositoryClassGenerator extends AbstractChildClassGenerator {

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm       = ctx.getCm();
        String childName    = ctx.entityName();                          // "OrderItem"
        String parentName   = ctx.parentEntityName();                    // "SalesOrder"
        String parentCamelId = toCamelCase(parentName) + "Id";          // "salesOrderId"
        JDefinedClass childCls = ctx.getEntityClass();

        JDefinedClass iface = ctx.subPackage("repository")._interface(childName + "Repository");
        iface.annotate(cm.ref("org.springframework.stereotype.Repository"));
        iface._implements(cm.ref("org.springframework.data.jpa.repository.JpaRepository")
                .narrow(childCls, cm.ref(Long.class)));

        JClass listChild     = cm.ref(List.class).narrow(childCls);
        JClass optionalChild = cm.ref("java.util.Optional").narrow(childCls);

        // List<Child> findBy{Parent}Id(Long {parentCamelId})
        iface.method(JMod.NONE, listChild, "findBy" + parentName + "Id")
                .param(cm.ref(Long.class), parentCamelId);

        // Optional<Child> findByIdAnd{Parent}Id(Long id, Long {parentCamelId})
        var findByIdAndParent = iface.method(JMod.NONE, optionalChild,
                "findByIdAnd" + parentName + "Id");
        findByIdAndParent.param(cm.ref(Long.class), "id");
        findByIdAndParent.param(cm.ref(Long.class), parentCamelId);

        // boolean existsByIdAnd{Parent}Id(Long id, Long {parentCamelId})
        var existsByIdAndParent = iface.method(JMod.NONE, cm.BOOLEAN,
                "existsByIdAnd" + parentName + "Id");
        existsByIdAndParent.param(cm.ref(Long.class), "id");
        existsByIdAndParent.param(cm.ref(Long.class), parentCamelId);

        return iface;
    }
}
