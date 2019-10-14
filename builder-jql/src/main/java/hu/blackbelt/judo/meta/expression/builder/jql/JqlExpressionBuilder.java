package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
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
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newAttributeBindingBuilder;
import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newReferenceBindingBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;

/**
 * Expression builder from JQL.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <RTE> reference typed element (ie. reference)
 * @param <M>   measure
 * @param <U>   unit
 */
public class JqlExpressionBuilder<NE, P, PTE, E, C extends NE, RTE, M, U> {

    private static final Logger log = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public static final String SELF_NAME = "self";

    private final Resource expressionResource;
    private final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());

    private final JqlParser jqlParser = new JqlParser();

    public JqlExpressionBuilder(final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter, final Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;

        modelAdapter.getAllClasses().forEach(clazz -> {
            final TypeName typeName = getTypeName(clazz).get();

            if (!all(expressionResource.getResourceSet(), TypeName.class)
                    .anyMatch(tn -> Objects.equals(tn.getName(), typeName.getName()) && Objects.equals(tn.getNamespace(), typeName.getNamespace()))) {
                expressionResource.getContents().add(typeName);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("  - type name is already added to resource set: {}", clazz);
                }
            }

            Optional<Instance> foundSelf = all(expressionResource.getResourceSet(), Instance.class)
                    .filter(i -> Objects.equals(i.getName(), SELF_NAME) && EcoreUtil.equals(i.getElementName(), typeName))
                    .findAny();

            final Instance self;
            if (!foundSelf.isPresent()) {
                self = newInstanceBuilder()
                        .withElementName(typeName)
                        .withName(SELF_NAME)
                        .build();
                expressionResource.getContents().add(self);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("  - self instance is already added to resource set: {}", clazz);
                }
                self = foundSelf.get();
            }

            entityInstances.put(clazz, self);
        });
    }

    /**
     * Get type name of a given class (ASM/PSM/ESM model element).
     *
     * @param clazz entity type of data model
     * @return type name (expression metamodel element)
     */
    public Optional<TypeName> getTypeName(final C clazz) {
        return modelAdapter.getTypeName(clazz);
    }

    /**
     * Create and return expression of a given JQL string.
     *
     * @param entityType            entity type
     * @param jqlExpressionAsString JQL expression as string
     * @return expression
     */
    public Expression createExpression(final C entityType, String jqlExpressionAsString) {
        final hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression = jqlParser.parseString(jqlExpressionAsString);
        return createExpression(entityType, jqlExpression);
    }

    /**
     * Create and return expression of a given JQL expression.
     *
     * @param entityType    entity type
     * @param jqlExpression JQL expression
     * @return expression
     */
    public Expression createExpression(final C entityType, hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression) {
        final Instance instance = entityType != null ? entityInstances.get(entityType) : null;
        final Expression expression = transformJqlToExpression(jqlExpression, instance != null ? ECollections.singletonEList(instance) : ECollections.emptyEList());

        if (expression != null) {
            log.debug("Expression created: {}", expression);
            expressionResource.getContents().add(expression);
        } else {
            log.warn("No expression created");
        }

        return expression;
    }

    /**
     * Create binding of a given expression.
     *
     * @param bindingContext binding context
     * @param expression     expression
     * @return expression binding
     */
    public Binding createBinding(final BindingContext bindingContext, final C entityType, final Expression expression) {
        final Instance instance = entityInstances.get(entityType);

        final Binding binding;
        switch (bindingContext.getType()) {
            case ATTRIBUTE:
                final AttributeBindingRole attributeBindingRole;
                switch (bindingContext.getRole()) {
                    case GETTER:
                        attributeBindingRole = AttributeBindingRole.GETTER;
                        break;
                    case SETTER:
                        attributeBindingRole = AttributeBindingRole.SETTER;
                        break;
                    case DEFAULT:
                        attributeBindingRole = AttributeBindingRole.DEFAULT;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported binding role");
                }

                binding = newAttributeBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(instance.getElementName())
                        .withAttributeName(bindingContext.getFeatureName())
                        .withRole(attributeBindingRole)
                        .build();
                break;
            case RELATION:
                final ReferenceBindingRole referenceBindingRole;
                switch (bindingContext.getRole()) {
                    case GETTER:
                        referenceBindingRole = ReferenceBindingRole.GETTER;
                        break;
                    case SETTER:
                        referenceBindingRole = ReferenceBindingRole.SETTER;
                        break;
                    case DEFAULT:
                        referenceBindingRole = ReferenceBindingRole.DEFAULT;
                        break;
                    case RANGE:
                        referenceBindingRole = ReferenceBindingRole.RANGE;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported binding role");
                }

                binding = newReferenceBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(instance.getElementName())
                        .withReferenceName(bindingContext.getFeatureName())
                        .withRole(referenceBindingRole)
                        .build();
                break;
            default:
                throw new IllegalStateException("Unsupported feature");
        }

        log.debug("Binding created for expression: {}", expression);
        expressionResource.getContents().add(binding);

        return binding;
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
            C navigationBase = (C) baseVariable.get().getObjectType(modelAdapter);
            for (final Feature jqlFeature : jqlNavigationExpression.getFeatures()) {
                final Optional<? extends PTE> attribute = modelAdapter.getAttribute(navigationBase, jqlFeature.getName());
                final Optional<? extends RTE> reference = modelAdapter.getReference(navigationBase, jqlFeature.getName());
                if (attribute.isPresent()) {
                    //TODO - handle data expressions that are not attribute selectors (necessary for getters only, setters must be selectors!)
                    expression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) expression);
                    navigationBase = null;
                } else if (reference.isPresent()) {
                    //TODO - handle reference expressions that are not reference selectors (necessary for getters only, setters must be selectors!)
                    expression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) expression);
                    navigationBase = modelAdapter.getTarget(reference.get());
                } else {
                    log.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
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
    private AttributeSelector createAttributeSelector(final PTE attribute, final String attributeName, final ObjectExpression objectExpression) {
        final Optional<? extends P> attributeType = modelAdapter.getAttributeType(attribute);
        if (attributeType.isPresent()) {
            if (modelAdapter.isBoolean(attributeType.get())) {
                return newLogicalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isDecimal(attributeType.get())) {
                return newDecimalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isDate(attributeType.get())) {
                return newDateAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isEnumeration(attributeType.get())) {
                return newEnumerationAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isInteger(attributeType.get())) {
                return newIntegerAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isString(attributeType.get())) {
                // text (unlimited) type is not supporting string operations
                return newStringAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (modelAdapter.isTimestamp(attributeType.get())) {
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
    private ReferenceExpression createReferenceSelector(final RTE reference, final String referenceName, final ReferenceExpression referenceExpression) {
        if (modelAdapter.isCollectionReference(reference)) {
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

    static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
        final Iterable<Notifier> asmContents = resourceSet::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    public static class BindingContext {

        private final String featureName;
        private final BindingType type;
        private final BindingRole role;

        public BindingContext(final String featureName, final BindingType bindingType, final BindingRole bindingRole) {
            this.featureName = featureName;
            type = bindingType;
            role = bindingRole;
        }

        public String getFeatureName() {
            return featureName;
        }

        public BindingType getType() {
            return type;
        }

        public BindingRole getRole() {
            return role;
        }
    }

    public enum BindingRole {
        GETTER, SETTER, DEFAULT, RANGE
    }

    public enum BindingType {
        ATTRIBUTE, RELATION
    }
}
