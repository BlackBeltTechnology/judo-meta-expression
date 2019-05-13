package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.constant.CollectionReference;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.InstanceId;
import hu.blackbelt.judo.meta.expression.constant.InstanceReference;
import hu.blackbelt.judo.meta.expression.runtime.query.*;
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
            final EClass type = (EClass) modelAdapter.get(instance.getElementName()).get();

            if (instance.getDefinition() instanceof InstanceId) {
                final InstanceId instanceId = (InstanceId) instance.getDefinition();
                log.debug("Base: instance with ID '{}' of {}", instanceId.getId(), type.getName());

                filters.add(FunctionSignature.EQUALS.create(
                        ImmutableMap.of(
                                FunctionSignature.TwoOperandFunctionParameterName.left.name(), SingleParameter.builder().feature(IdAttribute.builder().type(type).build()).build(),
                                FunctionSignature.TwoOperandFunctionParameterName.right.name(), SingleParameter.builder().feature(Constant.<UUID>builder().value(UUID.fromString(instanceId.getId())).build()).build()
                        )));
            } else if (instance.getDefinition() instanceof InstanceReference) {
                throw new IllegalStateException("Instance references are not supported as base");
            } else {
                throw new IllegalStateException("Missing instance definition");
            }

            builder.from(type);
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
                        final List<Identifiable> list = identifiableMap.get(attribute.eClass());
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
