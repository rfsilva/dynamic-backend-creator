package com.rfsilva.jcodemodel.service.generator;

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

@Component
public class GlobalExceptionHandlerClassGenerator extends AbstractClassGenerator {

    @Override
    public JDefinedClass generate(GenerationContext ctx) throws JClassAlreadyExistsException {
        JCodeModel cm = ctx.getCm();
        JDefinedClass notFoundEx = ctx.getNotFoundException();

        JDefinedClass cls = ctx.subPackage("exception")._class("GlobalExceptionHandler");
        cls.annotate(cm.ref("org.springframework.web.bind.annotation.RestControllerAdvice"));

        JClass problemDetail = cm.ref("org.springframework.http.ProblemDetail");
        JClass httpStatus    = cm.ref("org.springframework.http.HttpStatus");

        buildNotFoundHandler(cm, cls, problemDetail, httpStatus, notFoundEx);
        buildValidationHandler(cm, cls, problemDetail, httpStatus);
        buildGenericHandler(cm, cls, problemDetail, httpStatus);

        return cls;
    }

    private void buildNotFoundHandler(JCodeModel cm, JDefinedClass cls,
                                       JClass problemDetail, JClass httpStatus,
                                       JDefinedClass notFoundEx) {
        JMethod m = cls.method(JMod.PUBLIC, problemDetail, "handleNotFound");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.ExceptionHandler"))
                .param("value", notFoundEx.dotclass());
        JVar ex = m.param(notFoundEx, "ex");
        JVar pd = m.body().decl(problemDetail, "pd",
                problemDetail.staticInvoke("forStatus").arg(httpStatus.staticRef("NOT_FOUND")));
        m.body().invoke(pd, "setTitle").arg(JExpr.lit("Resource not found"));
        m.body().invoke(pd, "setDetail").arg(ex.invoke("getMessage"));
        m.body()._return(pd);
    }

    private void buildValidationHandler(JCodeModel cm, JDefinedClass cls,
                                         JClass problemDetail, JClass httpStatus) {
        JClass maneClass       = cm.ref("org.springframework.web.bind.MethodArgumentNotValidException");
        JClass objectErrorClass = cm.ref("org.springframework.validation.ObjectError");
        JClass fieldErrorClass  = cm.ref("org.springframework.validation.FieldError");
        JClass mapType          = cm.ref("java.util.Map").narrow(cm.ref(String.class), cm.ref(String.class));
        JClass hashMapType      = cm.ref("java.util.HashMap").narrow(cm.ref(String.class), cm.ref(String.class));

        JMethod m = cls.method(JMod.PUBLIC, problemDetail, "handleValidation");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.ExceptionHandler"))
                .param("value", maneClass.dotclass());
        JVar ex     = m.param(maneClass, "ex");
        JBlock body = m.body();
        JVar errors = body.decl(mapType, "errors", JExpr._new(hashMapType));

        JForEach forEach = body.forEach(objectErrorClass, "error",
                ex.invoke("getBindingResult").invoke("getAllErrors"));
        // JCast is package-private — use direct expression for the cast
        forEach.body().invoke(errors, "put")
                .arg(JExpr.direct("((org.springframework.validation.FieldError) error).getField()"))
                .arg(forEach.var().invoke("getDefaultMessage"));

        JVar pd = body.decl(problemDetail, "pd",
                problemDetail.staticInvoke("forStatus").arg(httpStatus.staticRef("BAD_REQUEST")));
        body.invoke(pd, "setTitle").arg(JExpr.lit("Validation failed"));
        body.invoke(pd, "setProperty").arg(JExpr.lit("errors")).arg(errors);
        body._return(pd);
    }

    private void buildGenericHandler(JCodeModel cm, JDefinedClass cls,
                                      JClass problemDetail, JClass httpStatus) {
        JMethod m = cls.method(JMod.PUBLIC, problemDetail, "handleGeneric");
        m.annotate(cm.ref("org.springframework.web.bind.annotation.ExceptionHandler"))
                .param("value", cm.ref(Exception.class).dotclass());
        JVar ex = m.param(cm.ref(Exception.class), "ex");
        JVar pd = m.body().decl(problemDetail, "pd",
                problemDetail.staticInvoke("forStatus").arg(httpStatus.staticRef("INTERNAL_SERVER_ERROR")));
        m.body().invoke(pd, "setTitle").arg(JExpr.lit("Unexpected error"));
        m.body().invoke(pd, "setDetail").arg(ex.invoke("getMessage"));
        m.body()._return(pd);
    }
}
