package com.rfsilva.jcodemodel.service.generator.backend;

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
public class ChildServiceClassGenerator extends AbstractChildClassGenerator {

    private static final String FIELD_REPO        = "repository";
    private static final String FIELD_PARENT_REPO = "parentRepository";
    private static final String FIELD_MAPPER      = "mapper";

    @Override
    public JDefinedClass generate(ChildGenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm        = ctx.getCm();
        String childName     = ctx.entityName();
        String parentName    = ctx.parentEntityName();
        String parentCamel   = toCamelCase(parentName);
        String parentCamelId = parentCamel + "Id";

        JDefinedClass cls = ctx.subPackage("service")._class(childName + "Service");
        cls.annotate(cm.ref("org.springframework.stereotype.Service"));
        cls.annotate(cm.ref("lombok.RequiredArgsConstructor"));
        cls.annotate(cm.ref("lombok.extern.slf4j.Slf4j"));
        cls.annotate(cm.ref("org.springframework.transaction.annotation.Transactional"));

        cls.field(JMod.PRIVATE | JMod.FINAL, ctx.getRepositoryClass(), FIELD_REPO);
        cls.field(JMod.PRIVATE | JMod.FINAL, ctx.parentRepoClass(),    FIELD_PARENT_REPO);
        cls.field(JMod.PRIVATE | JMod.FINAL, ctx.getMapperClass(),     FIELD_MAPPER);

        JClass transactionalRef = cm.ref("org.springframework.transaction.annotation.Transactional");

        buildValidateParentExists(cm, cls, parentName, parentCamelId, ctx.parentNotFoundEx());
        buildFindAll(ctx, cls, childName, parentName, parentCamelId, transactionalRef);
        buildFindById(ctx, cls, childName, parentName, parentCamelId, transactionalRef);
        buildCreate(ctx, cls, childName, parentName, parentCamel, parentCamelId);
        buildUpdate(ctx, cls, childName, parentName, parentCamelId);
        buildDeleteById(cm, cls, childName, parentName, parentCamelId,
                ctx.getNotFoundException(), cls.fields().get(FIELD_REPO));

        return cls;
    }

    // ── Build methods ─────────────────────────────────────────────────────────

    private void buildValidateParentExists(JCodeModel cm, JDefinedClass cls,
                                            String parentName, String parentCamelId,
                                            JDefinedClass parentNotFoundEx) {
        JMethod m       = cls.method(JMod.PRIVATE, cm.VOID, validateCall(parentName));
        JVar idParam    = m.param(cm.ref(Long.class), parentCamelId);
        JFieldVar pRepo = cls.fields().get(FIELD_PARENT_REPO);
        m.body()._if(pRepo.invoke("existsById").arg(idParam).not())
                ._then()._throw(JExpr._new(parentNotFoundEx).arg(idParam));
    }

    private void buildFindAll(ChildGenerationContext ctx, JDefinedClass cls,
                               String childName, String parentName, String parentCamelId,
                               JClass transactionalRef) {
        JCodeModel cm        = ctx.getCm();
        JFieldVar repo       = cls.fields().get(FIELD_REPO);
        JFieldVar mapper     = cls.fields().get(FIELD_MAPPER);
        JClass listResponse  = cm.ref(List.class).narrow(ctx.getResponseDto());

        JMethod m    = cls.method(JMod.PUBLIC, listResponse, "findAllByParentId");
        m.annotate(transactionalRef).param("readOnly", true);
        JVar idParam = m.param(cm.ref(Long.class), parentCamelId);
        m.body().invoke(validateCall(parentName)).arg(idParam);
        m.body().directStatement(logChildId("debug", "Fetching all " + childName + "s for " + parentName, parentCamelId));
        m.body()._return(mapper.invoke("toResponseList")
                .arg(repo.invoke("findBy" + parentName + "Id").arg(idParam)));
    }

    private void buildFindById(ChildGenerationContext ctx, JDefinedClass cls,
                                String childName, String parentName, String parentCamelId,
                                JClass transactionalRef) {
        JCodeModel cm = ctx.getCm();
        JMethod m = cls.method(JMod.PUBLIC, ctx.getResponseDto(), "findByIdAndParentId");
        m.annotate(transactionalRef).param("readOnly", true);
        JVar parentIdParam = m.param(cm.ref(Long.class), parentCamelId);
        m.param(cm.ref(Long.class), "id");
        m.body().invoke(validateCall(parentName)).arg(parentIdParam);
        m.body().directStatement(logBothIds("debug", "Fetching", childName, parentName, parentCamelId));
        m.body()._return(JExpr.direct(
                "repository.findByIdAnd" + parentName + "Id(id, " + parentCamelId + ")\n" +
                "                .map(mapper::toResponse)\n" +
                "                .orElseThrow(() -> new " + childName + "NotFoundException(id))"));
    }

    private void buildCreate(ChildGenerationContext ctx, JDefinedClass cls,
                              String childName, String parentName,
                              String parentCamel, String parentCamelId) {
        JCodeModel cm    = ctx.getCm();
        JFieldVar repo   = cls.fields().get(FIELD_REPO);
        JFieldVar mapper = cls.fields().get(FIELD_MAPPER);
        JMethod m        = cls.method(JMod.PUBLIC, ctx.getResponseDto(), "create");
        m.param(cm.ref(Long.class), parentCamelId);
        JVar req         = m.param(ctx.getRequestDto(), "request");
        JBlock body      = m.body();
        body.directStatement(logChildId("info", "Creating new " + childName + " for " + parentName, parentCamelId));
        JVar parent = body.decl(ctx.parentEntityClass(), parentCamel, JExpr.direct(
                "parentRepository.findById(" + parentCamelId + ")\n" +
                "                .orElseThrow(() -> new " + parentName + "NotFoundException(" + parentCamelId + "))"));
        JVar entity = body.decl(ctx.getEntityClass(), "entity", mapper.invoke("toEntity").arg(req));
        body.invoke(entity, setter(parentCamel)).arg(parent);
        JVar saved = body.decl(ctx.getEntityClass(), "saved", repo.invoke("save").arg(entity));
        body.directStatement("log.info(\"" + childName + " created with id={}\", saved.getId());");
        body._return(mapper.invoke("toResponse").arg(saved));
    }

    private void buildUpdate(ChildGenerationContext ctx, JDefinedClass cls,
                              String childName, String parentName, String parentCamelId) {
        JCodeModel cm    = ctx.getCm();
        JFieldVar repo   = cls.fields().get(FIELD_REPO);
        JFieldVar mapper = cls.fields().get(FIELD_MAPPER);
        JMethod m        = cls.method(JMod.PUBLIC, ctx.getResponseDto(), "update");
        JVar parentId    = m.param(cm.ref(Long.class), parentCamelId);
        m.param(cm.ref(Long.class), "id");
        JVar req         = m.param(ctx.getRequestDto(), "request");
        JBlock body      = m.body();
        body.invoke(validateCall(parentName)).arg(parentId);
        body.directStatement(logBothIds("info", "Updating", childName, parentName, parentCamelId));
        JVar entity = body.decl(ctx.getEntityClass(), "entity", JExpr.direct(
                "repository.findByIdAnd" + parentName + "Id(id, " + parentCamelId + ")\n" +
                "                .orElseThrow(() -> new " + childName + "NotFoundException(id))"));
        body.invoke(mapper, "updateEntity").arg(entity).arg(req);
        body._return(mapper.invoke("toResponse").arg(repo.invoke("save").arg(entity)));
    }

    private void buildDeleteById(JCodeModel cm, JDefinedClass cls,
                                  String childName, String parentName, String parentCamelId,
                                  JDefinedClass childNotFoundEx, JFieldVar repo) {
        JMethod m     = cls.method(JMod.PUBLIC, cm.VOID, "deleteById");
        JVar parentId = m.param(cm.ref(Long.class), parentCamelId);
        JVar id       = m.param(cm.ref(Long.class), "id");
        JBlock body   = m.body();
        body.invoke(validateCall(parentName)).arg(parentId);
        body.directStatement(logBothIds("info", "Deleting", childName, parentName, parentCamelId));
        body._if(repo.invoke("existsByIdAnd" + parentName + "Id").arg(id).arg(parentId).not())
                ._then()._throw(JExpr._new(childNotFoundEx).arg(id));
        body.invoke(repo, "deleteById").arg(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String validateCall(String parentName) {
        return "validate" + parentName + "Exists";
    }

    /** Produces e.g.: log.debug("Fetching all OrderItems for SalesOrder id={}", salesOrderId); */
    private static String logChildId(String level, String msg, String idVar) {
        return "log." + level + "(\"" + msg + " id={}\", " + idVar + ");";
    }

    /** Produces e.g.: log.info("Updating OrderItem id={} for SalesOrder id={}", id, salesOrderId); */
    private static String logBothIds(String level, String verb, String childName,
                                     String parentName, String parentCamelId) {
        return "log." + level + "(\"" + verb + " " + childName + " id={} for "
                + parentName + " id={}\", id, " + parentCamelId + ");";
    }
}
