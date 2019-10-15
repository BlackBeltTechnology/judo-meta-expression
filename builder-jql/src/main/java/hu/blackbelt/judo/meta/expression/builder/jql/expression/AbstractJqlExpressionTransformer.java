package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import org.eclipse.emf.common.util.EList;

import java.util.List;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;

public abstract class AbstractJqlExpressionTransformer<T extends hu.blackbelt.judo.meta.jql.jqldsl.Expression, NE, P, PTE, E, C extends NE, RTE, M, U> implements JqlExpressionTransformerFunction {

    protected final JqlTransformers<NE, P, PTE, E, C, RTE, M, U> jqlTransformers;

    protected AbstractJqlExpressionTransformer(JqlTransformers<NE, P, PTE, E, C, RTE, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    public final Expression apply(hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, List<ObjectVariable> variables) {
        return doTransform((T) jqlExpression, variables);
    }

    protected abstract Expression doTransform(T jqlExpression, List<ObjectVariable> variables);

    protected ModelAdapter<NE, P, PTE, E, C, RTE, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }
    
    /**
     * Create attribute selector.
     *
     * @param attribute        (transfer) attribute
     * @param attributeName    mapped attribute name
     * @param objectExpression object expression that attribute selector is created in
     * @param functionCalls
     * @return attribute selector
     */
    protected AttributeSelector createAttributeSelector(final PTE attribute, final String attributeName, final ObjectExpression objectExpression, EList<FunctionCall> functionCalls) {
        final Optional<? extends P> attributeType = getModelAdapter().getAttributeType(attribute);
        if (attributeType.isPresent()) {
            if (getModelAdapter().isBoolean(attributeType.get())) {
                return newLogicalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isDecimal(attributeType.get())) {
                return newDecimalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isDate(attributeType.get())) {
                return newDateAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isEnumeration(attributeType.get())) {
                return newEnumerationAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isInteger(attributeType.get())) {
                return newIntegerAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isString(attributeType.get())) {
                return newStringAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isTimestamp(attributeType.get())) {
                return newTimestampAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else {
                return newCustomAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            }
        } else {
            throw new IllegalStateException("Unknown attribute");
        }
    }

    /**
     * Create reference selector.
     *
     * @param reference           (transfer) relation
     * @param referenceName       mapped relation name
     * @param referenceExpression object expression that reference selector is created in
     * @return attribute selector
     */
    protected ReferenceExpression createReferenceSelector(final RTE reference, final String referenceName, final ReferenceExpression referenceExpression) {
        if (getModelAdapter().isCollectionReference(reference)) {
            if (referenceExpression instanceof CollectionExpression) {
                return newCollectionNavigationFromCollectionExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression)
                        .build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newCollectionNavigationFromObjectExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression)
                        .build();
            } else {
                throw new IllegalStateException("Reference expression must be object or collection");
            }
        } else {
            if (referenceExpression instanceof CollectionExpression) {
                return newObjectNavigationFromCollectionExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression)
                        .build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newObjectNavigationExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression)
                        .build();
            } else {
                throw new IllegalStateException("Reference expression must be object or collection");
            }
        }
    }

}
