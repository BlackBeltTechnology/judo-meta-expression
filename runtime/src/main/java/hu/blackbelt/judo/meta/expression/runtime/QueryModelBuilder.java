package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.*;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.runtime.query.*;
import hu.blackbelt.judo.meta.expression.runtime.query.Constant;
import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class QueryModelBuilder {

    private ModelAdapter modelAdapter;
    private ExpressionEvaluator evaluator;

    private static final String TABLE_ALIAS_FORMAT = "_t{0,number,00}";

    public QueryModelBuilder(final ModelAdapter modelAdapter, final ExpressionEvaluator evaluator) {
        this.modelAdapter = modelAdapter;
        this.evaluator = evaluator;
    }

    public Select createQueryModel(final EvaluationNode evaluationNode, final EClass targetType) {
        return createQueryModel(evaluationNode,
                ImmutableList.of(Target.builder().type(targetType).build()),
                new AtomicInteger(0));
    }

    private Select createQueryModel(final EvaluationNode evaluationNode, final List<Target> targets, final AtomicInteger nextAliasIndex) {
        final Expression expression = evaluationNode.getExpression();
        final EClass sourceType;
        if (expression instanceof Instance) {
            sourceType = (EClass) modelAdapter.get(((Instance) expression).getElementName()).get();
        } else if (expression instanceof ImmutableCollection) {
            sourceType = (EClass) modelAdapter.get(((ImmutableCollection) expression).getElementName()).get();
        } else if (expression instanceof ReferenceExpression) {
            sourceType = (EClass) ((ReferenceExpression) expression).getObjectType(modelAdapter);
        } else {
            throw new UnsupportedOperationException("Unsupported base expression");
        }
        log.debug("Base: {}, type: {} ({})", new Object[]{expression, sourceType.getName(), expression.getClass().getSimpleName() + "#" + expression.hashCode()});

        final Select select = Select.builder()
                .from(sourceType)
                .build();
        select.getTargets().addAll(targets);
        select.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));

        addIdAttribute(select, select);
        createIdFilters(evaluationNode, select);
        processEvaluationNode(evaluationNode, select, select, nextAliasIndex);

        return select;
    }

    private void addIdAttribute(final Select select, final Source source) {
        select.getTargets().forEach(target -> {
            target.getIdAttributes().add(IdAttribute.builder()
                    .source(source)
                    .target(target)
                    .build());
        });
    }

    private void createIdFilters(final EvaluationNode evaluationNode, final Select select) {
        if (evaluationNode.getExpression() instanceof Instance) {
            final Instance instance = (Instance) evaluationNode.getExpression();

            if (instance.getDefinition() instanceof InstanceId) {
                final InstanceId instanceId = (InstanceId) instance.getDefinition();
                log.debug("Base instance ID: {}", instanceId.getId());

                final IdAttribute id = select.getTargets().get(0).getIdAttributes().iterator().next();

                select.getFilters().add(FunctionSignature.EQUALS.create(
                        ImmutableMap.of(
                                FunctionSignature.TwoOperandFunctionParameterName.left.name(), SingleParameter.builder().feature(id).build(),
                                FunctionSignature.TwoOperandFunctionParameterName.right.name(), SingleParameter.builder().feature(Constant.<UUID>builder().value(UUID.fromString(instanceId.getId())).build()).build()
                        )));
            } else if (instance.getDefinition() instanceof InstanceReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            } else {
                throw new IllegalStateException("Missing instance definition");
            }
        } else if (evaluationNode.getExpression() instanceof ImmutableCollection) {
            final ImmutableCollection immutableCollection = (ImmutableCollection) evaluationNode.getExpression();
            if (immutableCollection.getDefinition() instanceof CollectionReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            }
        }
    }

    private void processEvaluationNode(final EvaluationNode evaluationNode, final Select select, final Source source, final AtomicInteger nextAliasIndex) {
        final Map<Expression, List<Instance>> instanceMap = evaluator.getAllInstances(Expression.class)
                .filter(expressionWithEvaluationNodeBase -> EcoreUtil.equals(evaluator.getBase(expressionWithEvaluationNodeBase), evaluationNode.getExpression()))
                .filter(expressionWithEvaluationNodeBase -> expressionWithEvaluationNodeBase instanceof NavigationExpression)
                .collect(Collectors.toMap(expressionWithEvaluationNodeBase -> expressionWithEvaluationNodeBase,
                        expressionWithEvaluationNodeBase -> evaluator.getAllInstances(Instance.class)
                                .filter(i -> i.getDefinition() instanceof InstanceReference)
                                .filter(i -> EcoreUtil.equals(((InstanceReference) i.getDefinition()).getVariable(), expressionWithEvaluationNodeBase))
                                .collect(Collectors.toList())));

        log.info("Expression: {}, instance map: {}", evaluationNode.getExpression(), instanceMap);

        evaluationNode.getTerminals().entrySet().stream()
                .forEach(t -> {
                    final AttributeRole attributeRole = ((Map.Entry<AttributeRole, Expression>) t).getKey();
                    final Expression expr = ((Map.Entry<AttributeRole, Expression>) t).getValue();

                    if (expr instanceof DataConstant) {
                        throw new UnsupportedOperationException("Not supported yet");
//                        final DataConstant dataConstant = (DataConstant) expr;
//                        feature = Constant.builder().value(dataConstant.getValue()).build();
                    } else if (expr instanceof AttributeSelector) {
                        final AttributeSelector attributeSelector = (AttributeSelector) expr;

                        final EAttribute sourceAttribute = (EAttribute) source.getType().getEStructuralFeature(attributeSelector.getAttributeName());
                        if (sourceAttribute == null) {
                            log.error("Attribute {} of {} not found", attributeSelector.getAttributeName(), source.getType().getName());
                            throw new IllegalStateException("Invalid source attribute");
                        }

                        // FIXME - use expression base (self transfer objects) to select relevant targets
                        select.getTargets().stream()
                                .filter(target -> target.getType().getEStructuralFeature(attributeRole.getAlias()) != null)
                                .forEach(target -> {
                                    final EAttribute targetAttribute = (EAttribute) target.getType().getEStructuralFeature(attributeRole.getAlias());
                                    if (targetAttribute == null) {
                                        log.error("Attribute {} of {} not found", attributeRole.getAlias(), target.getType().getName());
                                        throw new IllegalStateException("Invalid target attribute");
                                    }

                                    final Feature feature = Attribute.builder()
                                            .source(source)
                                            .sourceAttribute(sourceAttribute)
                                            .target(target)
                                            .targetAttribute(targetAttribute)
                                            .build();
                                    target.getFeatures().add(feature);
                                });
                    } else {
                        throw new UnsupportedOperationException("Not supported yet");
                    }
                });

        evaluationNode.getNavigations().entrySet().stream()
                .forEach(n -> {
                    final SetTransformation t = ((Map.Entry<SetTransformation, ?>) n).getKey();
                    final EvaluationNode next = ((Map.Entry<SetTransformation, EvaluationNode>) n).getValue();
                    final Expression expr = next.getExpression();

                    final EClass joined;
                    if (expr instanceof ReferenceExpression) {
                        joined = (EClass) ((ReferenceExpression) next.getExpression()).getObjectType(modelAdapter);
                    } else {
                        throw new IllegalStateException("Expression must be reference");
                    }

                    final String referenceName;
                    if (expr instanceof NavigationExpression) {
                        final NavigationExpression ne = (NavigationExpression) expr;

                        referenceName = ne.getReferenceName();
                    } else {
                        throw new UnsupportedOperationException("Not supported yet");
                    }

                    final EReference sourceReference = (EReference) source.getType().getEStructuralFeature(referenceName);
                    if (sourceReference == null) {
                        log.error("Reference {} of {} not found", referenceName, source.getType().getName());
                        throw new IllegalStateException("Invalid source reference");
                    }

                    if (!EcoreUtil.equals(joined, sourceReference.getEReferenceType())) {
                        log.error("Type of source reference {} is not matching with type of expression {}", sourceReference.getEReferenceType(), joined);
                        throw new IllegalStateException("Invalid reference");
                    }

                    if ((expr instanceof ObjectNavigationExpression) || (expr instanceof ObjectNavigationFromCollectionExpression)) {
                        // TODO - process expression as collection if alias is set!!!
                        final Join join = Join.builder()
                                .partner(source)
                                .reference(sourceReference)
                                .build();
                        join.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));
                        select.getJoins().add(join);

                        addIdAttribute(select, join);
                        createIdFilters(next, select);
                        processEvaluationNode(next, select, join, nextAliasIndex);
                    } else if ((expr instanceof CollectionNavigationFromObjectExpression) || (expr instanceof CollectionNavigationFromCollectionExpression)) {
                        final String alias = ((NavigationExpression) expr).getAlias();
                        log.info("ALIAS: {}", alias);
                        select.getTargets().stream()
                                .filter(target -> target.getType().getEStructuralFeature(alias) != null)
                                .forEach(target -> {
                                    final EReference targetReference = (EReference) target.getType().getEStructuralFeature(alias);
                                    if (targetReference == null) {
                                        log.error("Attribute {} of {} not found", alias, target.getType().getName());
                                        throw new IllegalStateException("Invalid target reference");
                                    }

                                    final Select nestedSelect = Select.builder()
                                            .from(joined)
                                            .build();
                                    nestedSelect.getTargets().add(Target.builder()
                                            .type(targetReference.getEReferenceType())
                                            .build());
                                    nestedSelect.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));

                                    addIdAttribute(nestedSelect, nestedSelect);
                                    createIdFilters(next, nestedSelect);
                                    processEvaluationNode(next, nestedSelect, nestedSelect, nextAliasIndex);
                                    // TODO - add IN filter expression

                                    final SubSelect subSelect = SubSelect.builder()
                                            .select(nestedSelect)
                                            .alias(alias)
                                            .build();
                                    final Join join = Join.builder()
                                            .partner(source)
                                            .reference(sourceReference)
                                            .build();
                                    join.setSourceAlias(nestedSelect.getSourceAlias());
                                    subSelect.getJoins().add(join);

                                    select.getSubSelects().add(subSelect);
                                });
                    } else {
                        throw new IllegalArgumentException("Navigation must be object/collection expression");
                    }

                    //t.getFiltering() // TODO
                    //t.getOrdering() // TODO
                    //t.getWindowingExpression() // TODO
                });
    }
}
