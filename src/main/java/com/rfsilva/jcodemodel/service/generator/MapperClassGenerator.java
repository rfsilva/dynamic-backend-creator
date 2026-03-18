package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
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
public class MapperClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm   = ctx.getCm();
        String name     = ctx.entityName();
        JDefinedClass entityCls   = ctx.getEntityClass();
        JDefinedClass requestDto  = ctx.getRequestDto();
        JDefinedClass responseDto = ctx.getResponseDto();

        JDefinedClass cls = ctx.subPackage("mapper")._class(name + "Mapper");
        cls.annotate(cm.ref("org.springframework.stereotype.Component"));

        JClass listEntity   = cm.ref(List.class).narrow(entityCls);
        JClass listResponse = cm.ref(List.class).narrow(responseDto);

        buildToEntity(cm, cls, name, entityCls, requestDto, ctx.fields());
        buildToResponse(cm, cls, name, responseDto, entityCls, ctx.fields());
        buildToResponseList(cm, cls, entityCls, responseDto, listEntity, listResponse);
        buildUpdateEntity(cm, cls, entityCls, requestDto, ctx.fields());

        return cls;
    }

    private void buildToEntity(JCodeModel cm, JDefinedClass cls, String name,
                                JDefinedClass entityCls, JDefinedClass requestDto,
                                List<FieldDefinition> fields) {
        JMethod m = cls.method(JMod.PUBLIC, entityCls, "toEntity");
        m.param(requestDto, "request");

        // Builder chain via direct expression — JCodeModel 2.6 has no fluent chain API
        StringBuilder expr = new StringBuilder(name + ".builder()");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            expr.append("\n                .").append(camel)
                    .append("(request.").append(getter(camel)).append("())");
        }
        expr.append("\n                .build()");
        m.body()._return(JExpr.direct(expr.toString()));
    }

    private void buildToResponse(JCodeModel cm, JDefinedClass cls, String name,
                                  JDefinedClass responseDto, JDefinedClass entityCls,
                                  List<FieldDefinition> fields) {
        JMethod m = cls.method(JMod.PUBLIC, responseDto, "toResponse");
        m.param(entityCls, "entity");

        StringBuilder expr = new StringBuilder(name + "Response.builder()");
        expr.append("\n                .id(entity.getId())");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            expr.append("\n                .").append(camel)
                    .append("(entity.").append(getter(camel)).append("())");
        }
        expr.append("\n                .build()");
        m.body()._return(JExpr.direct(expr.toString()));
    }

    private void buildToResponseList(JCodeModel cm, JDefinedClass cls,
                                      JDefinedClass entityCls, JDefinedClass responseDto,
                                      JClass listEntity, JClass listResponse) {
        JMethod m = cls.method(JMod.PUBLIC, listResponse, "toResponseList");
        JVar entities = m.param(listEntity, "entities");
        JBlock body = m.body();

        JVar result = body.decl(
                cm.ref(ArrayList.class).narrow(responseDto), "result",
                JExpr._new(cm.ref(ArrayList.class).narrow(responseDto)));
        JForEach forEach = body.forEach(entityCls, "entity", entities);
        forEach.body().invoke(result, "add")
                .arg(JExpr.invoke("toResponse").arg(forEach.var()));
        body._return(result);
    }

    private void buildUpdateEntity(JCodeModel cm, JDefinedClass cls,
                                    JDefinedClass entityCls, JDefinedClass requestDto,
                                    List<FieldDefinition> fields) {
        JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "updateEntity");
        JVar entity  = m.param(entityCls, "entity");
        JVar request = m.param(requestDto, "request");
        for (FieldDefinition f : fields) {
            String camel = toCamelCase(f.getName());
            m.body().invoke(entity, setter(camel)).arg(request.invoke(getter(camel)));
        }
    }
}
