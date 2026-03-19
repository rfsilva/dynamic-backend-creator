package com.rfsilva.jcodemodel.service.generator.backend;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChildMapperClassGenerator extends AbstractChildClassGenerator {

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm          = ctx.getCm();
        String childName       = ctx.entityName();
        String parentCamel     = toCamelCase(ctx.parentEntityName());   // "salesOrder"
        String parentCamelId   = parentCamel + "Id";                    // "salesOrderId"
        JDefinedClass childCls = ctx.getEntityClass();
        JDefinedClass reqDto   = ctx.getRequestDto();
        JDefinedClass resDto   = ctx.getResponseDto();

        JDefinedClass cls = ctx.subPackage("mapper")._class(childName + "Mapper");
        cls.annotate(cm.ref("org.springframework.stereotype.Component"));

        JClass listChild    = cm.ref(List.class).narrow(childCls);
        JClass listResponse = cm.ref(List.class).narrow(resDto);

        buildToEntity(cls, childName, childCls, reqDto, ctx.fields());
        buildToResponse(cls, childName, parentCamel, parentCamelId, resDto, childCls, ctx.fields());
        buildToResponseList(cm, cls, childCls, resDto, listChild, listResponse);
        buildUpdateEntity(cls, childCls, reqDto, ctx.fields());

        return cls;
    }

    private void buildToEntity(JDefinedClass cls, String childName,
                                JDefinedClass childCls, JDefinedClass reqDto,
                                List<FieldDefinition> fields) {
        JMethod m = cls.method(JMod.PUBLIC, childCls, "toEntity");
        m.param(reqDto, "request");

        StringBuilder expr = new StringBuilder(childName + ".builder()");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            expr.append("\n                .").append(camel)
                    .append("(request.").append(getter(camel)).append("())");
        }
        expr.append("\n                .build()");
        m.body()._return(JExpr.direct(expr.toString()));
    }

    private void buildToResponse(JDefinedClass cls, String childName,
                                  String parentCamel, String parentCamelId,
                                  JDefinedClass resDto, JDefinedClass childCls,
                                  List<FieldDefinition> fields) {
        JMethod m = cls.method(JMod.PUBLIC, resDto, "toResponse");
        m.param(childCls, "entity");

        // entity.getSalesOrder().getId()
        String parentIdExpr = "entity." + getter(parentCamel) + "().getId()";

        StringBuilder expr = new StringBuilder(childName + "Response.builder()");
        expr.append("\n                .id(entity.getId())");
        expr.append("\n                .").append(parentCamelId).append("(").append(parentIdExpr).append(")");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            expr.append("\n                .").append(camel)
                    .append("(entity.").append(getter(camel)).append("())");
        }
        expr.append("\n                .build()");
        m.body()._return(JExpr.direct(expr.toString()));
    }

    private void buildToResponseList(JCodeModel cm, JDefinedClass cls,
                                      JDefinedClass childCls, JDefinedClass resDto,
                                      JClass listChild, JClass listResponse) {
        JMethod m = cls.method(JMod.PUBLIC, listResponse, "toResponseList");
        JVar entities = m.param(listChild, "entities");
        JBlock body   = m.body();

        JVar result = body.decl(
                cm.ref(ArrayList.class).narrow(resDto), "result",
                JExpr._new(cm.ref(ArrayList.class).narrow(resDto)));
        JForEach forEach = body.forEach(childCls, "entity", entities);
        forEach.body().invoke(result, "add")
                .arg(JExpr.invoke("toResponse").arg(forEach.var()));
        body._return(result);
    }

    private void buildUpdateEntity(JDefinedClass cls,
                                    JDefinedClass childCls, JDefinedClass reqDto,
                                    List<FieldDefinition> fields) {
        JMethod m   = cls.method(JMod.PUBLIC, cls.owner().VOID, "updateEntity");
        JVar entity  = m.param(childCls, "entity");
        JVar request = m.param(reqDto,   "request");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            m.body().invoke(entity, setter(camel)).arg(request.invoke(getter(camel)));
        }
    }
}
