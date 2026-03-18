package com.rfsilva.jcodemodel.service.generator;

import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the child JPA entity and wires the bidirectional relationship:
 * - adds @ManyToOne to the child pointing to the parent
 * - adds @OneToMany + @Builder.Default to the already-generated parent entity
 */
@Component
public class ChildEntityClassGenerator extends AbstractChildClassGenerator {

    @Override
    public JDefinedClass generate(ChildGenerationContext childCtx) throws JClassAlreadyExistsException {
        JCodeModel cm      = childCtx.getCm();
        String name        = childCtx.entityName();
        String parentName  = childCtx.parentEntityName();    // "SalesOrder"
        String parentCamel = toCamelCase(parentName);        // "salesOrder"

        JDefinedClass cls = childCtx.subPackage("entity")._class(name);
        cls.annotate(cm.ref("jakarta.persistence.Entity"));
        cls.annotate(cm.ref("jakarta.persistence.Table"))
                .param("name", toSnakeCase(name).toUpperCase());
        cls.annotate(cm.ref("lombok.Getter"));
        cls.annotate(cm.ref("lombok.Setter"));
        cls.annotate(cm.ref("lombok.NoArgsConstructor"));
        cls.annotate(cm.ref("lombok.AllArgsConstructor"));
        cls.annotate(cm.ref("lombok.Builder"));

        // @Id
        JFieldVar id = cls.field(JMod.PRIVATE, cm.ref(Long.class), "id");
        id.annotate(cm.ref("jakarta.persistence.Id"));
        id.annotate(cm.ref("jakarta.persistence.GeneratedValue"))
                .param("strategy", cm.ref("jakarta.persistence.GenerationType").staticRef("IDENTITY"));

        // @ManyToOne — reference to parent
        JFieldVar parentField = cls.field(JMod.PRIVATE, childCtx.parentEntityClass(), parentCamel);
        JAnnotationUse manyToOne = parentField.annotate(cm.ref("jakarta.persistence.ManyToOne"));
        manyToOne.param("fetch", cm.ref("jakarta.persistence.FetchType").staticRef("LAZY"));
        JAnnotationUse joinColumn = parentField.annotate(cm.ref("jakarta.persistence.JoinColumn"));
        joinColumn.param("name", toSnakeCase(parentName) + "_id");
        joinColumn.param("nullable", false);

        // User-defined fields
        for (FieldDefinition field : childCtx.fields()) {
            JFieldVar f = cls.field(JMod.PRIVATE,
                    resolveJType(cm, field.getType()),
                    toCamelCase(field.getName()));
            if (field.getType() == FieldDefinition.FieldType.STRING && field.getMaxLength() != null) {
                f.annotate(cm.ref("jakarta.persistence.Column")).param("length", field.getMaxLength());
            }
        }

        // Add @OneToMany back to the already-generated parent entity
        // (JCodeModel allows adding fields to JDefinedClass at any point before cm.build())
        addOneToManyToParent(cm, childCtx, cls, parentCamel);

        return cls;
    }

    private void addOneToManyToParent(JCodeModel cm, ChildGenerationContext childCtx,
                                       JDefinedClass childCls, String mappedByField) {
        JDefinedClass parentCls = childCtx.parentEntityClass();
        String childListField   = toCamelCase(childCtx.entityName()) + "s"; // "orderItems"

        JClass listChild      = cm.ref(List.class).narrow(childCls);
        JClass arrayListChild = cm.ref(ArrayList.class).narrow(childCls);

        JFieldVar field = parentCls.field(JMod.PRIVATE, listChild, childListField,
                JExpr._new(arrayListChild));

        JAnnotationUse oneToMany = field.annotate(cm.ref("jakarta.persistence.OneToMany"));
        oneToMany.param("mappedBy", mappedByField);
        oneToMany.paramArray("cascade")
                .param(cm.ref("jakarta.persistence.CascadeType").staticRef("ALL"));
        oneToMany.param("orphanRemoval", true);
        field.annotate(cm.ref("lombok.Builder.Default"));
    }
}
