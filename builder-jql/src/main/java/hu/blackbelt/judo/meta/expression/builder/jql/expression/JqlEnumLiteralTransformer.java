package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.LiteralBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.EnumLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newLiteralBuilder;

public class JqlEnumLiteralTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<EnumLiteral, NE, P, PTE, E, C, RTE, M, U> {

    public JqlEnumLiteralTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(EnumLiteral jqlExpression, List<ObjectVariable> variables) {
        String value = jqlExpression.getValue();
        LiteralBuilder builder = newLiteralBuilder().withValue(value);
        QualifiedName type = jqlExpression.getType();
        if (type != null) {
            String name = type.getName();
            String namespace = type.getNamespaceElements() != null ? String.join("::", type.getNamespaceElements()) + "::" : "";
            TypeName enumTypeName = jqlTransformers.getEnumType(namespace + name);
            builder.withEnumeration(enumTypeName);
        }
        return builder.build();
    }

}