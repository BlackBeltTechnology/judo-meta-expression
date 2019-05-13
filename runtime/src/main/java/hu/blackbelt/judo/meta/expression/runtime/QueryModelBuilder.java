package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.ImmutableCollection;
import hu.blackbelt.judo.meta.expression.constant.CollectionReference;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.InstanceId;
import hu.blackbelt.judo.meta.expression.constant.InstanceReference;
import hu.blackbelt.judo.meta.expression.runtime.query.Constant;
import hu.blackbelt.judo.meta.expression.runtime.query.IdAttribute;
import hu.blackbelt.judo.meta.expression.runtime.query.Select;
import hu.blackbelt.judo.meta.expression.runtime.query.function.Equals;
import hu.blackbelt.judo.meta.expression.runtime.query.function.LogicalFunction;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Slf4j
public class QueryModelBuilder {

    private ModelAdapter modelAdapter;

    public QueryModelBuilder(final ModelAdapter modelAdapter) {
        this.modelAdapter = modelAdapter;
    }

    public Select createQueryModel(final EvaluationNode evaluationNode) {
        final Select.SelectBuilder builder = Select.builder();

        final Collection<LogicalFunction> filters = new ArrayList<>();

        final IdAttribute base = new IdAttribute();

        final Expression expression = evaluationNode.getExpression();
        if (expression instanceof Instance) {
            final Instance instance = (Instance) expression;
            final EClass type = (EClass) modelAdapter.get(instance.getElementName()).get();

            if (instance.getDefinition() instanceof InstanceId) {
                final InstanceId instanceId = (InstanceId) instance.getDefinition();
                log.debug("Base: instance with ID '{}' of {}", instanceId.getId(), type.getName());

                filters.add(Equals.builder()
                        .left(SingleParameter.builder().feature(base).build())
                        .right(SingleParameter.builder().feature(Constant.<UUID>builder().value(UUID.fromString(instanceId.getId())).build()).build())
                        .build());
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

        final Select queryModel = builder
                .filters(filters)
                .build();
        base.setIdentifiable(queryModel);

        return queryModel;
    }
}
