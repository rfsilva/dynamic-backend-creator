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
public class ChildControllerClassGenerator extends AbstractChildClassGenerator {

    private static final String RE         = "org.springframework.http.ResponseEntity";
    private static final String PATH_VAR   = "org.springframework.web.bind.annotation.PathVariable";
    private static final String ANNO_VALUE = "value";

    @SuppressWarnings("java:S1075")
    private static final String ID_PATH    = "/{id}";

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm        = ctx.getCm();
        String childName     = ctx.entityName();
        String parentName    = ctx.parentEntityName();
        String parentCamelId = toCamelCase(parentName) + "Id";
        String basePath      = "/api/" + toKebabCase(parentName) + "s/{"
                + parentCamelId + "}/" + toKebabCase(childName) + "s";

        JDefinedClass cls = ctx.subPackage("controller")._class(childName + "Controller");
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RestController"));
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RequestMapping"))
                .param(ANNO_VALUE, basePath);
        cls.annotate(cm.ref("lombok.RequiredArgsConstructor"));
        cls.annotate(cm.ref("lombok.extern.slf4j.Slf4j"));

        JFieldVar svc       = cls.field(JMod.PRIVATE | JMod.FINAL, ctx.getServiceClass(), "service");
        JClass listResponse = cm.ref(List.class).narrow(ctx.getResponseDto());

        buildFindAll(cm, cls, basePath, svc, listResponse, parentCamelId);
        buildFindById(cm, cls, basePath, svc, ctx.getResponseDto(), parentCamelId);
        buildCreate(cm, cls, basePath, svc, ctx.getRequestDto(), ctx.getResponseDto(), parentCamelId);
        buildUpdate(cm, cls, basePath, svc, ctx.getRequestDto(), ctx.getResponseDto(), parentCamelId);
        buildDelete(cm, cls, basePath, svc, parentCamelId);

        return cls;
    }

    // ── Build methods ─────────────────────────────────────────────────────────

    private void buildFindAll(JCodeModel cm, JDefinedClass cls, String basePath,
                               JFieldVar svc, JClass listResponse, String parentCamelId) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(listResponse), "findAll");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"));
        JVar pid = m.param(cm.ref(Long.class), parentCamelId);
        pid.annotate(cm.ref(PATH_VAR));
        m.body().directStatement("log.debug(\"GET " + basePath + "\");");
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("findAllByParentId").arg(pid)));
    }

    private void buildFindById(JCodeModel cm, JDefinedClass cls, String basePath,
                                JFieldVar svc, JDefinedClass responseDto, String parentCamelId) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "findById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar pid = m.param(cm.ref(Long.class), parentCamelId);
        pid.annotate(cm.ref(PATH_VAR));
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        m.body().directStatement(logId("debug", "GET " + basePath));
        m.body()._return(re.staticInvoke("ok")
                .arg(svc.invoke("findByIdAndParentId").arg(pid).arg(id)));
    }

    private void buildCreate(JCodeModel cm, JDefinedClass cls, String basePath,
                              JFieldVar svc, JDefinedClass requestDto,
                              JDefinedClass responseDto, String parentCamelId) {
        JClass re          = cm.ref(RE);
        JClass httpStatus  = cm.ref("org.springframework.http.HttpStatus");
        JClass requestBody = cm.ref("org.springframework.web.bind.annotation.RequestBody");
        JClass valid       = cm.ref("jakarta.validation.Valid");
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "create");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PostMapping"));
        JVar pid = m.param(cm.ref(Long.class), parentCamelId);
        pid.annotate(cm.ref(PATH_VAR));
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        m.body().directStatement("log.info(\"POST " + basePath + "\");");
        JVar created = m.body().decl(responseDto, "created",
                svc.invoke("create").arg(pid).arg(body));
        m.body()._return(re.staticInvoke("status").arg(httpStatus.staticRef("CREATED"))
                .invoke("body").arg(created));
    }

    private void buildUpdate(JCodeModel cm, JDefinedClass cls, String basePath,
                              JFieldVar svc, JDefinedClass requestDto,
                              JDefinedClass responseDto, String parentCamelId) {
        JClass re          = cm.ref(RE);
        JClass requestBody = cm.ref("org.springframework.web.bind.annotation.RequestBody");
        JClass valid       = cm.ref("jakarta.validation.Valid");
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "update");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PutMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar pid = m.param(cm.ref(Long.class), parentCamelId);
        pid.annotate(cm.ref(PATH_VAR));
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        m.body().directStatement(logId("info", "PUT " + basePath));
        m.body()._return(re.staticInvoke("ok")
                .arg(svc.invoke("update").arg(pid).arg(id).arg(body)));
    }

    private void buildDelete(JCodeModel cm, JDefinedClass cls, String basePath,
                              JFieldVar svc, String parentCamelId) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(cm.ref(Void.class)), "deleteById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.DeleteMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar pid = m.param(cm.ref(Long.class), parentCamelId);
        pid.annotate(cm.ref(PATH_VAR));
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        m.body().directStatement(logId("info", "DELETE " + basePath));
        m.body().invoke(svc, "deleteById").arg(pid).arg(id);
        m.body()._return(re.staticInvoke("noContent").invoke("build"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Produces e.g.: log.info("PUT /api/sales-orders/{salesOrderId}/order-items/{}", id); */
    private static String logId(String level, String methodAndPath) {
        return "log." + level + "(\"" + methodAndPath + "/{}\", id);";
    }
}
