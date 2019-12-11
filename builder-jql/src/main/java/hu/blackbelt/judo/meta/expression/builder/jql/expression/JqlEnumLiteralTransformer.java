package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.LiteralBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.EnumLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newLiteralBuilder;

public class JqlEnumLiteralTransformer<NE, P extends NE, PTE, E extends P, C extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<EnumLiteral, NE, P, PTE, E, C, RTE, S, M, U> {

    public JqlEnumLiteralTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(EnumLiteral jqlExpression, JqlExpressionBuildingContext context) {
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
