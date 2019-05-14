package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromCollectionExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.collection.ObjectNavigationFromCollectionExpression;
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
        final List<SubSelect> subSelects = new ArrayList<>();
        final Collection<IdentifiableFeature> backReferences = new ArrayList<>();

        final Expression expression = evaluationNode.getExpression();
        final EClass sourceType;
        if (expression instanceof Instance) {
            final Instance instance = (Instance) expression;
            sourceType =  (EClass) modelAdapter.get(instance.getElementName()).get();

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
        } else if (expression instanceof ImmutableCollection) {
            final ImmutableCollection immutableCollection = (ImmutableCollection) expression;
            sourceType  = (EClass) modelAdapter.get(immutableCollection.getElementName()).get();

            if (immutableCollection.getDefinition() instanceof CollectionReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            } else {
                log.debug("Base: all instances of {}", sourceType);
            }
        } else if (expression instanceof ReferenceExpression) {
            log.info("Base: {} ({})", expression, expression.getClass().getSimpleName());
            sourceType = (EClass) ((ReferenceExpression) expression).getObjectType(modelAdapter);
        } else {
            throw new UnsupportedOperationException("Unsupported base expression");
        }
        processEvaluationNode(evaluationNode, builder, sourceType, targetType, features, joins, filters, subSelects, backReferences);

        final Select select = builder
                .from(sourceType)
                .features(features)
                .joins(joins)
                .filters(filters)
                .subSelects(subSelects)
                .build();
        backReferences.forEach(r -> r.setIdentifiable(select));

        return resolve(select);
    }

    private void processEvaluationNode(final EvaluationNode evaluationNode, final Select.SelectBuilder builder, final EClass sourceType, final EClass targetType,
                                       final Map<EAttribute, Feature> features, final List<Join> joins, final Collection<Function> filters, final Collection<SubSelect> subSelects, final Collection<IdentifiableFeature> backReferences) {
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

                        final EAttribute sourceAttribute = (EAttribute) sourceType.getEStructuralFeature(attributeSelector.getAttributeName());
                        if (sourceAttribute == null) {
                            log.error("Attribute {} of {} not found", attributeSelector.getAttributeName(), sourceType.getName());
                            throw new IllegalStateException("Invalid source attribute");
                        }

                        feature = Attribute.builder()
                                .sourceAttribute(sourceAttribute)
                                .build();
                        backReferences.add((Attribute) feature);
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

                    final EReference sourceReference = (EReference) sourceType.getEStructuralFeature(referenceName);
                    ;
                    if (sourceReference == null) {
                        log.error("Reference {} of {} not found", referenceName, sourceType.getName());
                        throw new IllegalStateException("Invalid source reference");
                    }

                    if (expr instanceof ObjectExpression) {
                        final Collection<IdentifiableFeature> joinedBackReferences = new ArrayList<>();
                        processEvaluationNode(next, builder, joined, targetType, features, joins, filters, subSelects, joinedBackReferences);

                        final Join join = Join.builder()
                                .reference(sourceReference)
                                //.alias(t.getAlias())
                                .build();
                        backReferences.add(join);

                        joinedBackReferences.forEach(j -> j.setIdentifiable(join));

                        joins.add(join); // TODO - include source class name
                    } else if (expr instanceof CollectionExpression) {
                        final EReference targetReference = (EReference) targetType.getEStructuralFeature(t.getAlias());
                        if (targetReference == null) {
                            log.error("Reference {} of {} not found", t.getAlias(), targetReference.getName());
                            throw new IllegalStateException("Invalid target reference");
                        }

                        // FIXME - put all joined types of navigation
                        final Join join = Join.builder()
                                //.alias(t.getAlias())
                                .reference(sourceReference).build(); // set alias of last entry only!
                        final SubSelect subSelect = SubSelect.builder()
                                .select(createQueryModel(next, targetReference.getEReferenceType()))
                                .joins(ImmutableList.of(join))
                                .alias(t.getAlias())
                                .build();
                        backReferences.add(join);

                        subSelects.add(subSelect);
                    } else {
                        throw new IllegalArgumentException("Navigation must be object/collection expression");
                    }

                    //t.getFiltering() // TODO
                    //t.getOrdering() // TODO
                    //t.getWindowingExpression() // TODO
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
                    if (identifiableMap.containsKey(j.getReference().getEReferenceType())) {
                        list = identifiableMap.get(j.getReference().getEReferenceType());
                    } else {
                        list = new ArrayList<>();
                        identifiableMap.put(j.getReference().getEReferenceType(), list);
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
