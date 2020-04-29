package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.constant.util.builder.LiteralBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.EnumLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.getFqString;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newLiteralBuilder;

public class JqlEnumLiteralTransformer<NE, P extends NE, PTE, E extends P, C extends NE, AP extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<EnumLiteral, NE, P, PTE, E, C, AP, RTE, S, M, U> {

    public JqlEnumLiteralTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(EnumLiteral enumLiteral, ExpressionBuildingVariableResolver context) {
        String value = enumLiteral.getEnumConstant().getValue();
        String enumName = getEnumName(enumLiteral);
        TypeName enumTypeName = jqlTransformers.getEnumType(enumName);
        if (enumTypeName == null) {
            throw new IllegalArgumentException("Unknown enum: " + enumName);
        }
        return newLiteralBuilder().withEnumeration(enumTypeName).withValue(value).build();
    }

    private String getEnumName(EnumLiteral enumLiteral) {
        return getFqString(enumLiteral.getNamespaceElements(), enumLiteral.getEnumConstant().getName());
    }

}
