package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ControllerClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm   = ctx.getCm();
        String name     = ctx.entityName();
        JDefinedClass requestDto  = ctx.getRequestDto();
        JDefinedClass responseDto = ctx.getResponseDto();
        JDefinedClass serviceCls  = ctx.getServiceClass();

        JDefinedClass cls = ctx.subPackage("controller")._class(name + "Controller");
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RestController"));
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RequestMapping"))
                .param("value", "/api/" + toKebabCase(name) + "s");
        cls.annotate(cm.ref("lombok.RequiredArgsConstructor"));

        JFieldVar svc = cls.field(JMod.PRIVATE | JMod.FINAL, serviceCls, "service");

        JClass listResponse  = cm.ref(List.class).narrow(responseDto);
        JClass re            = cm.ref("org.springframework.http.ResponseEntity");
        JClass httpStatus    = cm.ref("org.springframework.http.HttpStatus");
        JClass pathVariable  = cm.ref("org.springframework.web.bind.annotation.PathVariable");
        JClass requestBody   = cm.ref("org.springframework.web.bind.annotation.RequestBody");
        JClass valid         = cm.ref("jakarta.validation.Valid");

        buildFindAll(cm, cls, svc, re, listResponse);
        buildFindById(cm, cls, svc, re, responseDto, pathVariable);
        buildCreate(cm, cls, svc, re, httpStatus, requestDto, responseDto, requestBody, valid);
        buildUpdate(cm, cls, svc, re, requestDto, responseDto, pathVariable, requestBody, valid);
        buildDelete(cm, cls, svc, re, pathVariable);

        return cls;
    }

    private void buildFindAll(JCodeModel cm, JDefinedClass cls, JFieldVar svc,
                               JClass re, JClass listResponse) {
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(listResponse), "findAll");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"));
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("findAll")));
    }

    private void buildFindById(JCodeModel cm, JDefinedClass cls, JFieldVar svc,
                                JClass re, JDefinedClass responseDto, JClass pathVariable) {
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "findById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"))
                .param("value", "/{id}");
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(pathVariable);
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("findById").arg(id)));
    }

    private void buildCreate(JCodeModel cm, JDefinedClass cls, JFieldVar svc,
                              JClass re, JClass httpStatus,
                              JDefinedClass requestDto, JDefinedClass responseDto,
                              JClass requestBody, JClass valid) {
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "create");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PostMapping"));
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        JVar created = m.body().decl(responseDto, "created", svc.invoke("create").arg(body));
        m.body()._return(re.staticInvoke("status").arg(httpStatus.staticRef("CREATED"))
                .invoke("body").arg(created));
    }

    private void buildUpdate(JCodeModel cm, JDefinedClass cls, JFieldVar svc,
                              JClass re, JDefinedClass requestDto, JDefinedClass responseDto,
                              JClass pathVariable, JClass requestBody, JClass valid) {
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "update");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PutMapping"))
                .param("value", "/{id}");
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(pathVariable);
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("update").arg(id).arg(body)));
    }

    private void buildDelete(JCodeModel cm, JDefinedClass cls, JFieldVar svc,
                              JClass re, JClass pathVariable) {
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(cm.ref(Void.class)), "deleteById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.DeleteMapping"))
                .param("value", "/{id}");
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(pathVariable);
        m.body().invoke(svc, "deleteById").arg(id);
        m.body()._return(re.staticInvoke("noContent").invoke("build"));
    }
}
