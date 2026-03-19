package com.rfsilva.jcodemodel.service.generator.backend;

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

    private static final String RE         = "org.springframework.http.ResponseEntity";
    private static final String PATH_VAR   = "org.springframework.web.bind.annotation.PathVariable";
    private static final String ANNO_VALUE = "value";
    @SuppressWarnings("java:S1075")
    private static final String ID_PATH    = "/{id}";

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm   = ctx.getCm();
        String name     = ctx.entityName();
        String path     = "/api/" + toKebabCase(name) + "s";
        JDefinedClass requestDto  = ctx.getRequestDto();
        JDefinedClass responseDto = ctx.getResponseDto();
        JDefinedClass serviceCls  = ctx.getServiceClass();

        JDefinedClass cls = ctx.subPackage("controller")._class(name + "Controller");
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RestController"));
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RequestMapping"))
                .param(ANNO_VALUE, path);
        cls.annotate(cm.ref("lombok.RequiredArgsConstructor"));
        cls.annotate(cm.ref("lombok.extern.slf4j.Slf4j"));

        JFieldVar svc = cls.field(JMod.PRIVATE | JMod.FINAL, serviceCls, "service");
        JClass listResponse = cm.ref(List.class).narrow(responseDto);

        buildFindAll(cm, cls, path, svc, listResponse);
        buildFindById(cm, cls, path, svc, responseDto);
        buildCreate(cm, cls, path, svc, requestDto, responseDto);
        buildUpdate(cm, cls, path, svc, requestDto, responseDto);
        buildDelete(cm, cls, path, svc);

        return cls;
    }

    // ── Build methods ─────────────────────────────────────────────────────────

    private void buildFindAll(JCodeModel cm, JDefinedClass cls, String path,
                               JFieldVar svc, JClass listResponse) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(listResponse), "findAll");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"));
        m.body().directStatement("log.debug(\"GET " + path + "\");");
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("findAll")));
    }

    private void buildFindById(JCodeModel cm, JDefinedClass cls, String path,
                                JFieldVar svc, JDefinedClass responseDto) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "findById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.GetMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        m.body().directStatement(logId("debug", "GET " + path));
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("findById").arg(id)));
    }

    private void buildCreate(JCodeModel cm, JDefinedClass cls, String path,
                              JFieldVar svc, JDefinedClass requestDto, JDefinedClass responseDto) {
        JClass re         = cm.ref(RE);
        JClass httpStatus = cm.ref("org.springframework.http.HttpStatus");
        JClass requestBody = cm.ref("org.springframework.web.bind.annotation.RequestBody");
        JClass valid       = cm.ref("jakarta.validation.Valid");
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "create");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PostMapping"));
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        m.body().directStatement("log.info(\"POST " + path + "\");");
        JVar created = m.body().decl(responseDto, "created", svc.invoke("create").arg(body));
        m.body()._return(re.staticInvoke("status").arg(httpStatus.staticRef("CREATED"))
                .invoke("body").arg(created));
    }

    private void buildUpdate(JCodeModel cm, JDefinedClass cls, String path,
                              JFieldVar svc, JDefinedClass requestDto, JDefinedClass responseDto) {
        JClass re          = cm.ref(RE);
        JClass requestBody = cm.ref("org.springframework.web.bind.annotation.RequestBody");
        JClass valid       = cm.ref("jakarta.validation.Valid");
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(responseDto), "update");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.PutMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        JVar body = m.param(requestDto, "request");
        body.annotate(valid);
        body.annotate(requestBody);
        m.body().directStatement(logId("info", "PUT " + path));
        m.body()._return(re.staticInvoke("ok").arg(svc.invoke("update").arg(id).arg(body)));
    }

    private void buildDelete(JCodeModel cm, JDefinedClass cls, String path, JFieldVar svc) {
        JClass re = cm.ref(RE);
        JMethod m = cls.method(JMod.PUBLIC, re.narrow(cm.ref(Void.class)), "deleteById");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.DeleteMapping"))
                .param(ANNO_VALUE, ID_PATH);
        JVar id = m.param(cm.ref(Long.class), "id");
        id.annotate(cm.ref(PATH_VAR));
        m.body().directStatement(logId("info", "DELETE " + path));
        m.body().invoke(svc, "deleteById").arg(id);
        m.body()._return(re.staticInvoke("noContent").invoke("build"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Produces e.g.: log.debug("GET /api/products/{}", id); */
    private static String logId(String level, String methodAndPath) {
        return "log." + level + "(\"" + methodAndPath + "/{}\", id);";
    }
}
