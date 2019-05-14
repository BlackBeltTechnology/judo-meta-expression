package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.constant.*;
import hu.blackbelt.judo.meta.expression.runtime.query.*;
import hu.blackbelt.judo.meta.expression.runtime.query.Constant;
import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class QueryModelBuilder {

    private ModelAdapter modelAdapter;

    private static final String TABLE_ALIAS_FORMAT = "_t{0}";

    public QueryModelBuilder(final ModelAdapter modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    public Select createQueryModel(final EvaluationNode evaluationNode, final EClass targetType) {
        final Select.SelectBuilder builder = Select.builder()
                .target(targetType);

        final Map<EAttribute, Feature> features = new HashMap<>();
        final List<Join> joins = new ArrayList<>();
        final Collection<Function> filters = new ArrayList<>();
        final Map<EReference, SubSelect> subSelects = new HashMap<>();

        final Expression expression = evaluationNode.getExpression();
        if (expression instanceof Instance) {
            final Instance instance = (Instance) expression;
            final EClass sourceType = (EClass) modelAdapter.get(instance.getElementName()).get();

            if (instance.getDefinition() instanceof InstanceId) {
                final InstanceId instanceId = (InstanceId) instance.getDefinition();
                log.debug("Base: instance with ID '{}' of {}", instanceId.getId(), sourceType.getName());

                filters.add(FunctionSignature.EQUALS.create(
                        ImmutableMap.of(
                                FunctionSignature.TwoOperandFunctionParameterName.left.name(), SingleParameter.builder().feature(IdAttribute.builder().type(sourceType).build()).build(),
                                FunctionSignature.TwoOperandFunctionParameterName.right.name(), SingleParameter.builder().feature(Constant.<UUID>builder().value(UUID.fromString(instanceId.getId())).build()).build()
                        )));
            } else if (instance.getDefinition() instanceof InstanceReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            } else {
                throw new IllegalStateException("Missing instance definition");
            }

            processEvaluationNode(evaluationNode, builder, sourceType, targetType, features, joins, filters, subSelects);

            builder.from(sourceType);
        } else if (expression instanceof ImmutableCollection) {
            final ImmutableCollection immutableCollection = (ImmutableCollection) expression;
            final EClass type = (EClass) modelAdapter.get(immutableCollection.getElementName()).get();

            if (immutableCollection.getDefinition() instanceof CollectionReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            } else {
                log.debug("Base: all instances of {}", type);
            }

            builder.from(type);
        } else {
            throw new UnsupportedOperationException("Unsupported base expression");
        }

        return resolve(builder
                .features(features)
                .joins(joins)
                .filters(filters)
                .subSelects(subSelects)
                .build());
    }

    private void processEvaluationNode(final EvaluationNode evaluationNode, final Select.SelectBuilder builder, final EClass sourceType, final EClass targetType,
                                       final Map<EAttribute, Feature> features, final List<Join> joins, final Collection<Function> filters, final Map<EReference, SubSelect> subSelects) {
        evaluationNode.getTerminals().entrySet().stream()
                .forEach(t -> {
                    final AttributeRole attributeRole = ((Map.Entry<AttributeRole, Expression>) t).getKey();
                    final Expression expr = ((Map.Entry<AttributeRole, Expression>) t).getValue();

                    final Feature feature;
                    if (expr instanceof DataConstant) {
                        final DataConstant dataConstant = (DataConstant) expr;
                        feature = Constant.builder().value(dataConstant.getValue()).build();
                    } else if (expr instanceof AttributeSelector) {
                        final AttributeSelector attributeSelector = (AttributeSelector) expr;

                        //final String alias = attributeSelector.getAlias() != null ? attributeSelector.getAlias() : attributeSelector.getAttributeName();

                        final EAttribute sourceAttribute = (EAttribute) sourceType.getEStructuralFeature(attributeSelector.getAttributeName());
                        if (sourceAttribute == null) {
                            log.error("Attribute {} of {} not found", attributeSelector.getAttributeName(), sourceType.getName());
                            throw new IllegalStateException("Invalid source attribute");
                        }

                        feature = Attribute.builder()
                                // .identifiable() // TODO
                                .sourceAttribute(sourceAttribute)
                                .build();
                    } else {
                        throw new UnsupportedOperationException("Not supported yet");
                    }

                    final EAttribute targetAttribute = (EAttribute) targetType.getEStructuralFeature(attributeRole.getAlias());
                    if (targetAttribute == null) {
                        log.error("Attribute {} of {} not found", attributeRole.getAlias(), targetType.getName());
                        throw new IllegalStateException("Invalid target attribute");
                    }

                    features.put(targetAttribute, feature);

                });

        evaluationNode.getNavigations().entrySet().stream()
                .forEach(n -> {
                    final SetTransformation t = ((Map.Entry<SetTransformation, ?>) n).getKey();
                    final EvaluationNode next = ((Map.Entry<SetTransformation, EvaluationNode>) n).getValue();

                    //t.getFiltering() // TODO
                    //t.getOrdering() // TODO
                    //t.getWindowingExpression() // TODO

                    final EClass joined;

                    if (next.getExpression() instanceof ReferenceExpression) {
                        joined = (EClass) ((ReferenceExpression) next.getExpression()).getObjectType(modelAdapter);
                    } else {
                        throw new IllegalStateException("Expression must be reference");
                    }

                    processEvaluationNode(next, builder, joined, targetType, features, joins, filters, subSelects);

                    final EReference ref = (EReference) sourceType.getEStructuralFeature(t.getAlias());
                    if (ref == null) {
                        log.error("Reference {} of {} not found", t.getAlias(), sourceType.getName());
                        throw new IllegalStateException("Invalid reference");
                    }

                    joins.add(
                            Join.builder()
                                    .reference(ref)
                                    .joined(joined)
                                    .alias(t.getAlias()) // TODO - include source class name
                                    .build()
                    );
                });
    }

    private Select resolve(final Select select) {
        return resolve(select, 0);
    }

    private Select resolve(final Select select, final int identifiableId) {
        final AtomicInteger nextIdentifiable = new AtomicInteger(identifiableId);

        final Map<EClass, List<Identifiable>> identifiableMap = new HashMap<>();
        final List<Identifiable> selectType = new ArrayList<>();
        selectType.add(select);
        identifiableMap.put(select.getFrom(), selectType);

        if (select.getAlias() == null) {
            select.setAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextIdentifiable.getAndIncrement()));
        }

        select.getJoins().stream()
                .forEach(j -> {
                    final List<Identifiable> list;
                    if (identifiableMap.containsKey(j.getJoined())) {
                        list = identifiableMap.get(j.getJoined());
                    } else {
                        list = new ArrayList<>();
                        identifiableMap.put(j.getJoined(), list);
                    }
                    list.add(j);
                });

        select.getJoins().stream()
                .filter(j -> j.getAlias() == null)
                .forEach(j -> j.setAlias(MessageFormat.format(TABLE_ALIAS_FORMAT, nextIdentifiable.getAndIncrement())));

        select.getIdentifiableFeatures()
                .filter(f -> f.getIdentifiable() == null)
                .forEach(identifiableFeature -> {
                    if (identifiableFeature instanceof Attribute) {
                        final EAttribute attribute = ((Attribute) identifiableFeature).getSourceAttribute();
                        final List<Identifiable> list = identifiableMap.get(attribute.getEContainingClass());
                        if (list == null) {
                            log.error("LIST NOT FOUND: {}, {}", attribute.getEContainingClass(), attribute);
                        }
                        if (list.size() == 1) {
                            identifiableFeature.setIdentifiable(list.get(0));
                        } else {
                            throw new IllegalArgumentException("Unknown/multiple sources of attribute");
                        }
                    } else if (identifiableFeature instanceof IdAttribute) {
                        final List<Identifiable> list = identifiableMap.get(((IdAttribute) identifiableFeature).getType());
                        if (list.size() == 1) {
                            identifiableFeature.setIdentifiable(list.get(0));
                        } else {
                            throw new IllegalArgumentException("Unknown/multiple sources of ID attribute");
                        }
                    }
                });

        return select;
    }
}
