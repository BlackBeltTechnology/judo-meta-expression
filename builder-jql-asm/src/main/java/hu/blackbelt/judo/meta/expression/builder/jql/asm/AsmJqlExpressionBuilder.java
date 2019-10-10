package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation;
import hu.blackbelt.judo.meta.jql.jqldsl.BooleanLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.StringLiteral;
import hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAritmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

public class AsmJqlExpressionBuilder {

    private static final Logger log = LoggerFactory.getLogger(AsmJqlExpressionBuilder.class.getName());

    private static final String SELF_NAME = "self";

    private final ExpressionModelResourceSupport expressionModelResourceSupport;
    private final AsmUtils asmUtils;
    private final ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> modelAdapter;

    private final EMap<EClass, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());

    private final JqlParser jqlParser = new JqlParser();

    public AsmJqlExpressionBuilder(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> modelAdapter, final URI uri) {
        this.modelAdapter = modelAdapter;
        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(uri)
                .build();

        asmUtils = new AsmUtils(asmResourceSet);

        asmUtils.all(EClass.class)
                .filter(c -> AsmUtils.isEntityType(c))
                .forEach(entityType -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Create type name and instance of entity type {}", AsmUtils.getClassifierFQName(entityType));
                    }

                    final TypeName typeName = newTypeNameBuilder()
                            .withName(entityType.getName())
                            .withNamespace(AsmUtils.getPackageFQName(entityType.getEPackage()).replace(".", "::"))
                            .build();

                    final Instance self = newInstanceBuilder()
                            .withElementName(typeName)
                            .withName(SELF_NAME)
                            .build();

                    expressionModelResourceSupport.addContent(typeName);
                    expressionModelResourceSupport.addContent(self);
                    entityInstances.put(entityType, self);
                });
    }

    /**
     * Create and return attribute binding for a given entity type.
     *
     * @param entityType            entity type
     * @param jqlExpressionAsString JQLexpression to process
     * @return data expression of attribute
     */
    public DataExpression createAttributeBinding(final EClass entityType, final String jqlExpressionAsString, final EvaluationContext context) {
        return createAttributeBinding(entityType, jqlParser.parseString(jqlExpressionAsString), context);
    }

    /**
     * Create and return attribute binding for a given entity type.
     *
     * @param entityType    entity type
     * @param jqlExpression JQL expression to process
     * @return data expression of attribute
     */
    public DataExpression createAttributeBinding(final EClass entityType, final hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, final EvaluationContext context) {
        final Instance instance = entityType != null ? entityInstances.get(entityType) : null;
        final Expression expression = transformJqlToExpression(jqlExpression, instance != null ? ECollections.singletonEList(instance) : ECollections.emptyEList());

        if (expression == null || (expression instanceof DataExpression)) {
            return (DataExpression) expression;
        } else {
            log.error("Invalid data expression (entity type: {}, feature: {}", AsmUtils.getClassifierFQName(entityType), context.getFeatureName());
            return null;
        }
    }

    /**
     * Create and return attribute binding for a given entity type.
     *
     * @param entityType            entity type
     * @param jqlExpressionAsString JQLexpression to process
     * @return data expression of attribute
     */
    public ReferenceExpression createRelationBinding(final EClass entityType, final String jqlExpressionAsString, final EvaluationContext context) {
        return createRelationBinding(entityType, jqlParser.parseString(jqlExpressionAsString), context);
    }

    /**
     * Create and return relation binding for a given entity type.
     *
     * @param entityType    entity type
     * @param jqlExpression JQL expression to process
     * @return reference expression of relation
     */
    public ReferenceExpression createRelationBinding(final EClass entityType, final hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, final EvaluationContext context) {
        final Instance instance = entityType != null ? entityInstances.get(entityType) : null;
        final Expression expression = transformJqlToExpression(jqlExpression, instance != null ? ECollections.singletonEList(instance) : ECollections.emptyEList());

        if (expression == null || (expression instanceof ReferenceExpression)) {
            return (ReferenceExpression) expression;
        } else {
            log.error("Invalid reference expression (entity type: {}, feature: {}", AsmUtils.getClassifierFQName(entityType), context.getFeatureName());
            return null;
        }
    }

    private Expression transformJqlToExpression(hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, final EList<ObjectVariable> variables) {
        if (jqlExpression instanceof NavigationExpression) {
            return transformJqlNavigationToExpression((NavigationExpression) jqlExpression, variables);
        } else if (jqlExpression instanceof BooleanLiteral) {
            return newBooleanConstantBuilder().withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build();
        } else if (jqlExpression instanceof DecimalLiteral) {
            return newDecimalConstantBuilder().withValue(((DecimalLiteral) jqlExpression).getValue()).build();
        } else if (jqlExpression instanceof IntegerLiteral) {
            return newIntegerConstantBuilder().withValue(((IntegerLiteral) jqlExpression).getValue()).build();
        } else if (jqlExpression instanceof StringLiteral) {
            return newStringConstantBuilder().withValue(((StringLiteral) jqlExpression).getValue()).build();
        } else if (jqlExpression instanceof BinaryOperation) {
            final Expression left = transformJqlToExpression(((hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation) jqlExpression).getLeftOperand(), variables);
            final Expression right = transformJqlToExpression(((hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation) jqlExpression).getRightOperand(), variables);
            final String operator = ((hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation) jqlExpression).getOperator();

            if ((left instanceof IntegerExpression) && (right instanceof IntegerExpression)) {
                switch (operator) {
                    case "+":
                        return newIntegerAritmeticExpressionBuilder()
                                .withLeft((IntegerExpression) left)
                                .withOperator(IntegerOperator.ADD)
                                .withRight((IntegerExpression) right)
                                .build();
                    case "-":
                        return newIntegerAritmeticExpressionBuilder()
                                .withLeft((IntegerExpression) left)
                                .withOperator(IntegerOperator.SUBSTRACT)
                                .withRight((IntegerExpression) right)
                                .build();
                    case "*":
                        return newIntegerAritmeticExpressionBuilder()
                                .withLeft((IntegerExpression) left)
                                .withOperator(IntegerOperator.MULTIPLY)
                                .withRight((IntegerExpression) right)
                                .build();
                    case "div":
                    case "DIV":
                        return newIntegerAritmeticExpressionBuilder()
                                .withLeft((IntegerExpression) left)
                                .withOperator(IntegerOperator.DIVIDE)
                                .withRight((IntegerExpression) right)
                                .build();
                    case "%":
                    case "mod":
                    case "MOD":
                        return newIntegerAritmeticExpressionBuilder()
                                .withLeft((IntegerExpression) left)
                                .withOperator(IntegerOperator.MODULO)
                                .withRight((IntegerExpression) right)
                                .build();
                    case "/":
                        return newDecimalAritmeticExpressionBuilder()
                                .withLeft((NumericExpression) left)
                                .withOperator(DecimalOperator.DIVIDE)
                                .withRight((NumericExpression) right)
                                .build();
                    default:
                        throw new UnsupportedOperationException("Invalid integer operation: " + operator);
                }
            } else if ((left instanceof NumericExpression) && (right instanceof NumericExpression)) {
                switch (operator) {
                    case "+":
                        return newDecimalAritmeticExpressionBuilder()
                                .withLeft((NumericExpression) left)
                                .withOperator(DecimalOperator.ADD)
                                .withRight((NumericExpression) right)
                                .build();
                    case "-":
                        return newDecimalAritmeticExpressionBuilder()
                                .withLeft((NumericExpression) left)
                                .withOperator(DecimalOperator.SUBSTRACT)
                                .withRight((NumericExpression) right)
                                .build();
                    case "*":
                        return newDecimalAritmeticExpressionBuilder()
                                .withLeft((NumericExpression) left)
                                .withOperator(DecimalOperator.MULTIPLY)
                                .withRight((NumericExpression) right)
                                .build();
                    case "/":
                        return newDecimalAritmeticExpressionBuilder()
                                .withLeft((NumericExpression) left)
                                .withOperator(DecimalOperator.DIVIDE)
                                .withRight((NumericExpression) right)
                                .build();
                    default:
                        throw new UnsupportedOperationException("Invalid numeric operation: " + operator);
                }
            } else {
                throw new UnsupportedOperationException("Not supported operand types");
            }
        } else if (jqlExpression instanceof UnaryOperation) {
            final Expression operand = transformJqlToExpression(((hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation) jqlExpression).getOperand(), variables);
            final String operator = ((hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation) jqlExpression).getOperator();

            if (operand instanceof IntegerExpression) {
                switch (operator) {
                    case "-":
                        return newIntegerOppositeExpressionBuilder().withExpression((IntegerExpression) operand).build();
                    default:
                        throw new UnsupportedOperationException("Invalid integer unary operation: " + operator);
                }
            } else if (operand instanceof DecimalExpression) {
                switch (operator) {
                    case "-":
                        return newDecimalOppositeExpressionBuilder().withExpression((DecimalExpression) operand).build();
                    default:
                        throw new UnsupportedOperationException("Invalid decimal unary operation: " + operator);
                }
            } else {
                throw new UnsupportedOperationException("Not supported operand type");
            }
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private Expression transformJqlNavigationToExpression(final NavigationExpression jqlNavigationExpression, final EList<ObjectVariable> variables) {
        final Optional<ObjectVariable> baseVariable = variables.stream()
                .filter(v -> Objects.equals(v.getName(), jqlNavigationExpression.getBase()))
                .findAny();

        if (!baseVariable.isPresent()) {
            throw new IllegalStateException("Base variable " + jqlNavigationExpression.getBase() + " not found");
        } else {
            Expression expression = newObjectVariableReferenceBuilder().withVariable(baseVariable.get()).build();
            EClass navigationBase = (EClass) baseVariable.get().getObjectType(modelAdapter);
            for (final Feature jqlFeature : jqlNavigationExpression.getFeatures()) {
                final EStructuralFeature feature = navigationBase.getEStructuralFeature(jqlFeature.getName());
                if (feature instanceof EAttribute) {
                    //TODO - handle data expressions that are not attribute selectors (necessary for getters only, setters must be selectors!)

                    expression = createAttributeSelector((EAttribute) feature, jqlFeature.getName(), (ObjectExpression) expression);
                    navigationBase = null;
                } else if (feature instanceof EReference) {
                    //TODO - handle reference expressions that are not reference selectors (necessary for getters only, setters must be selectors!)
                    expression = createReferenceSelector((EReference) feature, jqlFeature.getName(), (ReferenceExpression) expression);
                    navigationBase = ((EReference) feature).getEReferenceType();
                } else {
                    log.error("Feature {} of {} not found", jqlFeature.getName(), AsmUtils.getClassifierFQName(navigationBase));
                    throw new IllegalStateException("Feature not found");
                }
            }
            return expression;
        }
    }

    /**
     * Create attribute selector.
     *
     * @param attribute        (transfer) attribute
     * @param attributeName    mapped attribute name
     * @param objectExpression object expression that attribute selector is created in
     * @return attribute selector
     */
    private AttributeSelector createAttributeSelector(final EAttribute attribute, final String attributeName, final ObjectExpression objectExpression) {
        if (AsmUtils.isBoolean(attribute.getEAttributeType())) {
            return newLogicalAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isDecimal(attribute.getEAttributeType())) {
            return newDecimalAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isDate(attribute.getEAttributeType())) {
            return newDateAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isEnumeration(attribute.getEAttributeType())) {
            return newEnumerationAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isInteger(attribute.getEAttributeType())) {
            return newIntegerAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isString(attribute.getEAttributeType())) {
            // text (unlimited) type is not supporting string operations
            return newStringAttributeBuilder()
                    .withAttributeName(attributeName)
                    .withObjectExpression(objectExpression)
                    .build();
        } else if (AsmUtils.isTimestamp(attribute.getEAttributeType())) {
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
    }

    /**
     * Create reference selector.
     *
     * @param reference           (transfer) relation
     * @param referenceName       mapped relation name
     * @param referenceExpression object expression that reference selector is created in
     * @return attribute selector
     */
    private ReferenceExpression createReferenceSelector(final EReference reference, final String referenceName, final ReferenceExpression referenceExpression) {
        if (reference.isMany()) {
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

    public static class EvaluationContext {

        private final String featureName;

        private final boolean setter;

        public EvaluationContext(final String featureName, final boolean setter) {
            this.featureName = featureName;
            this.setter = setter;
        }

        public String getFeatureName() {
            return featureName;
        }

        public boolean isSetter() {
            return setter;
        }
    }
}
