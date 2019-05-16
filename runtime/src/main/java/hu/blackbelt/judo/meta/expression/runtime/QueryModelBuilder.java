package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.*;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.runtime.query.*;
import hu.blackbelt.judo.meta.expression.runtime.query.Constant;
import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class QueryModelBuilder {

    private ModelAdapter modelAdapter;
    private ExpressionEvaluator evaluator;

    private static final String TABLE_ALIAS_FORMAT = "_t{0,number,00}";

    private final static Predicate<NavigationExpression> SEPARATE_QUERY_FOR_REFERENCED_TRANSFEROBJECT = navigationExpression -> navigationExpression instanceof CollectionExpression;

    public QueryModelBuilder(final ModelAdapter modelAdapter, final ExpressionEvaluator evaluator) {
        this.modelAdapter = modelAdapter;
        this.evaluator = evaluator;
    }

    public Select createQueryModel(final EvaluationNode evaluationNode, final EClass targetType) {
        return createQueryModel(evaluationNode,
                ImmutableList.of(Target.builder().type(targetType).base(evaluationNode).build()),
                new AtomicInteger(0),
                0);
    }

    private Select createQueryModel(final EvaluationNode evaluationNode, final List<Target> targets, final AtomicInteger nextAliasIndex, final int level) {
        log.debug(pad(level) + "Processing evaluation expression: {}", evaluationNode.getExpression());

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
        log.debug(pad(level) + " - base: {}, type: {} ({})", new Object[]{expression, sourceType.getName(), expression.getClass().getSimpleName() + "#" + expression.hashCode()});

        final Select select = Select.builder()
                .from(sourceType)
                .build();
        select.getTargets().addAll(targets);
        select.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));

        addIdAttribute(select, select);
        createIdFilters(evaluationNode, select, 0);
        processEvaluationNode(evaluationNode, select, select, nextAliasIndex, level);

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

    private void createIdFilters(final EvaluationNode evaluationNode, final Select select, final int level) {
        if (evaluationNode.getExpression() instanceof Instance) {
            final Instance instance = (Instance) evaluationNode.getExpression();

            if (instance.getDefinition() instanceof InstanceId) {
                final InstanceId instanceId = (InstanceId) instance.getDefinition();
                log.debug(pad(level) + "   - base instance ID: {}", instanceId.getId());

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

    private void processEvaluationNode(final EvaluationNode evaluationNode, final Select select, final Source source, final AtomicInteger nextAliasIndex, final int level) {
        log.debug(pad(level) + " - processing expression: {}", evaluationNode.getExpression());

        final Map<NavigationExpression, List<Instance>> instanceMap = evaluator.getAllInstances(NavigationExpression.class)
                .filter(expressionWithEvaluationNodeBase -> EcoreUtil.equals(evaluator.getBase(expressionWithEvaluationNodeBase), evaluationNode.getExpression()))
                .collect(Collectors.toMap(expressionWithEvaluationNodeBase -> expressionWithEvaluationNodeBase,
                        expressionWithEvaluationNodeBase -> evaluator.getAllInstances(Instance.class)
                                .filter(i -> i.getDefinition() instanceof InstanceReference)
                                .filter(i -> EcoreUtil.equals(((InstanceReference) i.getDefinition()).getVariable(), expressionWithEvaluationNodeBase))
                                .collect(Collectors.toList())))
                .entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        log.debug(pad(level) + "   - referenced instances: {}", instanceMap);

        select.getTargets().forEach(target -> {
            log.debug(pad(level) + "   - checking target {} ...", target);
            instanceMap.entrySet().stream()
                    .filter(e -> SEPARATE_QUERY_FOR_REFERENCED_TRANSFEROBJECT.test(e.getKey()))
                    .forEach(entry -> {
                        log.debug(pad(level) + "     ... in navigation expression: {}", entry.getKey());
                        entry.getValue().forEach(instance -> {
                            log.debug(pad(level) + "        - processing instance: {}", instance);
                            final String alias = instance.getAlias();
                            if (alias == null) {
                                log.error("No alias is defined for target relation");
                            } else {
                                log.debug(pad(level) + "        - alias for target relation: {}", alias);
                                final EReference targetReference = (EReference) target.getType().getEStructuralFeature(alias);
                                if (targetReference.isDerived()) {
                                    throw new UnsupportedOperationException("Derived references are not supported yet");
                                }

                                final EvaluationNode next = evaluator.getEvaluationNode(instance);

                                final Select nestedSelect = Select.builder()
                                        .from((EClass) instance.getObjectType(modelAdapter))
                                        .build();
                                nestedSelect.getTargets().add(Target.builder()
                                        .base(evaluator.getEvaluationNode(instance))
                                        .role(targetReference)
                                        .type(targetReference.getEReferenceType())
                                        .build());
                                nestedSelect.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));

                                addIdAttribute(nestedSelect, nestedSelect);

//                        createIdFilters(next, nestedSelect, level); // TODO - add IN filter expression
                                processEvaluationNode(next, nestedSelect, nestedSelect, nextAliasIndex, level + 1);

                                final SubSelect subSelect = SubSelect.builder()
                                        .select(nestedSelect)
                                        .alias(alias)
                                        .build();

                                NavigationExpression n = entry.getKey();

                                final List<String> referenceNames = new LinkedList<>();
                                final List<EClass> targetTypes = new LinkedList<>();
                                while (n != null) {
                                    final EClass targetType = ((EClass) ((ReferenceExpression) n).getObjectType(modelAdapter));
                                    targetTypes.add(targetType);
                                    referenceNames.add(n.getReferenceName());
                                    log.debug(pad(level) + "        - marking JOIN to {} with reference name: {} ({})", new Object[]{targetType.getName(), n.getReferenceName(), n});
                                    if (n.getOperands().isEmpty()) {
                                        n = null;
                                    } else if (n.getOperands().size() == 1) {
                                        final Expression e = n.getOperands().get(0);
                                        n = (e instanceof NavigationExpression) ? (NavigationExpression) e : null;
                                    } else {
                                        throw new IllegalStateException("Multiple sources are not supported");
                                    }
                                }

                                Source nestedSource = select;
                                for (int i = referenceNames.size(); i > 0; i--) {
                                    final String referenceName = referenceNames.get(i - 1);
                                    final EClass targetType = targetTypes.get(i - 1);

                                    log.debug(pad(level) + "        - adding JOIN from {} to {} with reference name: {}", new Object[]{nestedSource.getType().getName(), targetType.getName(), referenceName});

                                    final EReference joinedTargetReference = (EReference) nestedSource.getType().getEStructuralFeature(referenceName);
                                    if (joinedTargetReference.isDerived()) {
                                        throw new UnsupportedOperationException("Derived references are not supported yet");
                                    }

                                    final Join join = Join.builder().partner(nestedSource).reference(joinedTargetReference).build();
                                    join.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));

                                    if (!EcoreUtil.equals(joinedTargetReference.getEReferenceType(), targetType)) {
                                        log.error("Type of reference {} of {} is {} instead of {}", new Object[]{referenceName, source.getType().getName(), nestedSource.getType().getName(), targetType});
                                        throw new IllegalStateException("Invalid reference");
                                    }
                                    nestedSource = join;
                                    subSelect.getJoins().add(join);
                                }


                                select.getSubSelects().add(subSelect);
                            }
                        });
                    });
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
                    log.debug(pad(level) + "   - adding navigation: {}", referenceName);

                    final EReference sourceReference = (EReference) source.getType().getEStructuralFeature(referenceName);
                    if (sourceReference.isDerived()) {
                        throw new UnsupportedOperationException("Derived references are not supported yet");
                    }

                    if (!EcoreUtil.equals(joined, sourceReference.getEReferenceType())) {
                        log.error("Type of source reference {} is not matching with type of expression {}", sourceReference.getEReferenceType(), joined);
                        throw new IllegalStateException("Invalid reference");
                    }

                    if ((expr instanceof ObjectNavigationExpression) || (expr instanceof ObjectNavigationFromCollectionExpression)) {
                        if (instanceMap.containsKey(expr) && SEPARATE_QUERY_FOR_REFERENCED_TRANSFEROBJECT.test((NavigationExpression) expr)) {
                            log.debug(pad(level) + "     - processed in separate query: {}", expr);
                        } else {
                            log.debug(pad(level) + "     - processing navigation {} of {}", referenceName, source.getType().getName());

                            if (instanceMap.containsKey(expr)) {
                                final List<Target> newTargets = new ArrayList<>();
                                select.getTargets().forEach(target -> {
                                    log.debug(pad(level) + "       - checking target {} ...", target);
                                    instanceMap.get(expr).forEach(instance -> {
                                        final String alias = instance.getAlias();
                                        if (alias == null) {
                                            log.error("No alias is defined for target relation");
                                        } else {
                                            log.debug(pad(level) + "     - adding new target: {} AS {} (filled by JOIN queries)", instance, instance.getAlias());
                                            final EReference targetReference = (EReference) target.getType().getEStructuralFeature(alias);
                                            if (targetReference.isDerived()) {
                                                throw new UnsupportedOperationException("Derived references are not supported yet");
                                            }
                                            newTargets.add(Target.builder()
                                                    .base(evaluator.getEvaluationNode(instance))
                                                    .role(targetReference)
                                                    .type(targetReference.getEReferenceType())
                                                    .build());
                                        }
                                    });
                                });
                                select.getTargets().addAll(newTargets);
                            }

                            // TODO - process expression as collection if alias is set!!!
                            final Join join = Join.builder()
                                    .partner(source)
                                    .reference(sourceReference)
                                    .build();
                            join.setSourceAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextAliasIndex.getAndIncrement()));
                            select.getJoins().add(join);

                            addIdAttribute(select, join);
                            createIdFilters(next, select, level);
                            processEvaluationNode(next, select, join, nextAliasIndex, level);
                        }
                    } else if ((expr instanceof CollectionNavigationFromObjectExpression) || (expr instanceof CollectionNavigationFromCollectionExpression)) {
                        if (instanceMap.containsKey(expr) && SEPARATE_QUERY_FOR_REFERENCED_TRANSFEROBJECT.test((NavigationExpression) expr)) {
                            log.debug(pad(level) + "     - processed in separate query: {}", expr);
                        } else {
                            log.debug(pad(level) + "     - processed (???) in separate query: {}", expr); // TODO - check
                        }
                    } else {
                        throw new IllegalArgumentException("Navigation must be object/collection expression");
                    }

                    //t.getFiltering() // TODO
                    //t.getOrdering() // TODO
                    //t.getWindowingExpression() // TODO
                });

        evaluationNode.getTerminals().entrySet().stream()
                .forEach(t -> {
                    final AttributeRole attributeRole = ((Map.Entry<AttributeRole, Expression>) t).getKey();
                    final Expression expr = ((Map.Entry<AttributeRole, Expression>) t).getValue();
                    log.debug(pad(level) + "   - adding terminal: {}", attributeRole.getAlias());

                    final Expression expressionBase = evaluator.getBase(expr);

                    if (expr instanceof DataConstant) {
                        throw new UnsupportedOperationException("Not supported yet");
//                        final DataConstant dataConstant = (DataConstant) expr;
//                        feature = Constant.builder().value(dataConstant.getValue()).build();
                    } else if (expr instanceof AttributeSelector) {
                        final AttributeSelector attributeSelector = (AttributeSelector) expr;

                        final EAttribute sourceAttribute = (EAttribute) source.getType().getEStructuralFeature(attributeSelector.getAttributeName());
                        if (sourceAttribute.isDerived()) {
                            throw new UnsupportedOperationException("Derived attributes are not supported yet");
                        }

                        // FIXME - use expression base (self transfer objects) to select relevant targets
                        select.getTargets().stream()
                                .filter(target -> target.getType().getEStructuralFeature(attributeRole.getAlias()) != null)
                                .filter(target -> EcoreUtil.equals(target.getBase().getExpression(), expressionBase))
                                .forEach(target -> {
                                    log.debug(pad(level) + "     - checking target {} ...", target);
                                    final EAttribute targetAttribute = (EAttribute) target.getType().getEStructuralFeature(attributeRole.getAlias());

                                    log.debug(pad(level) + "       - target base: {}", target.getBase().getExpression());
                                    log.debug(pad(level) + "       - evaluation base: {}", evaluator.getBase(expr));

                                    if (targetAttribute.isDerived()) {
                                        throw new UnsupportedOperationException("Derived attributes are not supported yet");
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

    }

    private static String pad(int level) {
        return StringUtils.leftPad("", level * 2, " ");
    }
}
