package com.rfsilva.jcodemodel.service.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm   = ctx.getCm();
        String name     = ctx.entityName();
        JDefinedClass entityCls   = ctx.getEntityClass();
        JDefinedClass requestDto  = ctx.getRequestDto();
        JDefinedClass responseDto = ctx.getResponseDto();
        JDefinedClass repoCls     = ctx.getRepositoryClass();
        JDefinedClass mapperCls   = ctx.getMapperClass();
        JDefinedClass notFoundEx  = ctx.getNotFoundException();

        JDefinedClass cls = ctx.subPackage("service")._class(name + "Service");
        cls.annotate(cm.ref("org.springframework.stereotype.Service"));
        cls.annotate(cm.ref("lombok.RequiredArgsConstructor"));
        cls.annotate(cm.ref("lombok.extern.slf4j.Slf4j"));
        cls.annotate(cm.ref("org.springframework.transaction.annotation.Transactional"));

        JFieldVar repo   = cls.field(JMod.PRIVATE | JMod.FINAL, repoCls,   "repository");
        JFieldVar mapper = cls.field(JMod.PRIVATE | JMod.FINAL, mapperCls, "mapper");

        JClass listResponse     = cm.ref(List.class).narrow(responseDto);
        JClass transactionalRef = cm.ref("org.springframework.transaction.annotation.Transactional");

        buildFindAll(cm, cls, name, repo, mapper, listResponse, transactionalRef);
        buildFindById(cm, cls, name, responseDto, transactionalRef);
        buildCreate(cm, cls, name, entityCls, requestDto, responseDto, repo, mapper);
        buildUpdate(cm, cls, name, entityCls, requestDto, responseDto, repo, mapper);
        buildDeleteById(cm, cls, name, repo, notFoundEx);

        return cls;
    }

    // ── Build methods ─────────────────────────────────────────────────────────

    private void buildFindAll(JCodeModel cm, JDefinedClass cls, String name,
                               JFieldVar repo, JFieldVar mapper,
                               JClass listResponse, JClass transactionalRef) {
        JMethod m = cls.method(JMod.PUBLIC, listResponse, "findAll");
        m.annotate(transactionalRef).param("readOnly", true);
        m.body().directStatement("log.debug(\"Fetching all " + name + "s\");");
        m.body()._return(mapper.invoke("toResponseList").arg(repo.invoke("findAll")));
    }

    private void buildFindById(JCodeModel cm, JDefinedClass cls, String name,
                                JDefinedClass responseDto, JClass transactionalRef) {
        JMethod m = cls.method(JMod.PUBLIC, responseDto, "findById");
        m.annotate(transactionalRef).param("readOnly", true);
        m.param(cm.ref(Long.class), "id");
        m.body().directStatement(logId("debug", "Fetching " + name, "id"));
        m.body()._return(JExpr.direct(
                "repository.findById(id)\n" +
                "                .map(mapper::toResponse)\n" +
                "                .orElseThrow(() -> new " + name + "NotFoundException(id))"));
    }

    private void buildCreate(JCodeModel cm, JDefinedClass cls, String name,
                              JDefinedClass entityCls, JDefinedClass requestDto,
                              JDefinedClass responseDto, JFieldVar repo, JFieldVar mapper) {
        JMethod m = cls.method(JMod.PUBLIC, responseDto, "create");
        JVar req    = m.param(requestDto, "request");
        JBlock body = m.body();
        body.directStatement("log.info(\"Creating new " + name + "\");");
        JVar entity = body.decl(entityCls, "entity", mapper.invoke("toEntity").arg(req));
        JVar saved  = body.decl(entityCls, "saved",  repo.invoke("save").arg(entity));
        body.directStatement("log.info(\"" + name + " created with id={}\", saved.getId());");
        body._return(mapper.invoke("toResponse").arg(saved));
    }

    private void buildUpdate(JCodeModel cm, JDefinedClass cls, String name,
                              JDefinedClass entityCls, JDefinedClass requestDto,
                              JDefinedClass responseDto, JFieldVar repo, JFieldVar mapper) {
        JMethod m = cls.method(JMod.PUBLIC, responseDto, "update");
        m.param(cm.ref(Long.class), "id");
        JVar req    = m.param(requestDto, "request");
        JBlock body = m.body();
        body.directStatement(logId("info", "Updating " + name, "id"));
        JVar entity = body.decl(entityCls, "entity", JExpr.direct(
                "repository.findById(id)\n" +
                "                .orElseThrow(() -> new " + name + "NotFoundException(id))"));
        body.invoke(mapper, "updateEntity").arg(entity).arg(req);
        body._return(mapper.invoke("toResponse").arg(repo.invoke("save").arg(entity)));
    }

    private void buildDeleteById(JCodeModel cm, JDefinedClass cls, String name,
                                  JFieldVar repo, JDefinedClass notFoundEx) {
        JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "deleteById");
        JVar id = m.param(cm.ref(Long.class), "id");
        JBlock body = m.body();
        body.directStatement(logId("info", "Deleting " + name, "id"));
        body._if(repo.invoke("existsById").arg(id).not())
                ._then()._throw(JExpr._new(notFoundEx).arg(id));
        body.invoke(repo, "deleteById").arg(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Produces e.g.: log.info("Updating Product with id={}", id); */
    private static String logId(String level, String msg, String varName) {
        return "log." + level + "(\"" + msg + " with id={}\", " + varName + ");";
    }
}
