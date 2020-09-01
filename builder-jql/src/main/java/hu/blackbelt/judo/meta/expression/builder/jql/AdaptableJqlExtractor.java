package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.*;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AdaptableJqlExtractor<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> implements JqlExtractor {

    private static final Logger log = LoggerFactory.getLogger(AdaptableJqlExtractor.class.getName());
    
    private final ResourceSet expressionResourceSet;
    private final ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter;
    private final JqlExpressionBuilder<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> builder;

    public AdaptableJqlExtractor(ResourceSet asmResourceSet, ResourceSet measureResourceSet, ResourceSet expressionResourceSet, ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter) {
        this.expressionResourceSet = expressionResourceSet;
        this.modelAdapter = modelAdapter; 
        this.builder = new JqlExpressionBuilder(modelAdapter, expressionResourceSet.getResources().get(0));
    }

    @Override
    public ResourceSet extractExpressions() {
        modelAdapter.getAllEntityTypes().forEach(this::extractFromEntityType);
        modelAdapter.getAllMappedTransferObjectTypes().forEach(this::extractFromMappedTransferObjectType);
        modelAdapter.getAllUnmappedTransferObjectTypes().forEach(this::extractFromUnmappedTransferObjectType);
        return expressionResourceSet;
    }

    private void extractFromUnmappedTransferObjectType(TO unmappedTransferObjectType) {
        if (log.isTraceEnabled()) {
            log.trace("Extracting JQL expressions of unmapped transfer object type {}", modelAdapter.getFqName(unmappedTransferObjectType));
        }
        TypeName typeName = builder.buildTypeName(unmappedTransferObjectType).orElseThrow(() -> new IllegalStateException("Illegal type name"));

        modelAdapter.getTransferRelations(unmappedTransferObjectType).stream()
                .filter(ref -> modelAdapter.isDerivedTransferRelation(ref))
                .forEach(reference -> {
                    if (log.isTraceEnabled()) {
                        log.trace("  - extracting JQL expressions of navigation property: {}", modelAdapter.getFqName(reference));
                    }

                    if (!hasReferenceBinding(typeName, modelAdapter.getName(reference).get(), ReferenceBindingRole.GETTER)) {
                        JqlExpressionBuilder.BindingContext getterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(reference).get(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER);
                        modelAdapter.getTransferRelationGetter(reference).ifPresent(jql -> buildAndBind(null, unmappedTransferObjectType, getterBindingContext, jql));
                    } else {
                        log.debug("Getter expression already extracted for navigation property: {}", modelAdapter.getFqName(reference));
                    }

                    if (!hasReferenceBinding(typeName, modelAdapter.getName(reference).get(), ReferenceBindingRole.SETTER)) {
                        JqlExpressionBuilder.BindingContext setterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(reference).get(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.SETTER);
                        modelAdapter.getTransferRelationSetter(reference).ifPresent(jql -> buildAndBind(null, unmappedTransferObjectType, setterBindingContext, jql));
                    } else {
                        log.debug("Setter expression already extracted for navigation property: {}", modelAdapter.getFqName(reference));
                    }
                });
    }

    private boolean hasReferenceBinding(TypeName typeName, String featureName, ReferenceBindingRole bindingRole) {
        return all(expressionResourceSet, ReferenceBinding.class)
                .anyMatch(b -> b.getTypeName() != null &&
                        Objects.equals(b.getTypeName().getName(), typeName.getName()) &&
                        Objects.equals(b.getTypeName().getNamespace(), typeName.getNamespace()) &&
                        b.getRole() == bindingRole &&
                        Objects.equals(b.getReferenceName(), featureName));
    }

    private boolean hasAttributeBinding(TypeName typeName, String featureName, AttributeBindingRole bindingRole) {
        return all(expressionResourceSet, AttributeBinding.class)
                .anyMatch(b -> b.getTypeName() != null &&
                        Objects.equals(b.getTypeName().getName(), typeName.getName()) &&
                        Objects.equals(b.getTypeName().getNamespace(), typeName.getNamespace()) &&
                        b.getRole() == bindingRole &&
                        Objects.equals(b.getAttributeName(), featureName));
    }

    private void extractFromMappedTransferObjectType(TO mappedTransferObjectType) {
        if (log.isTraceEnabled()) {
            log.trace("Extracting JQL expressions of mapped transfer object type {}", modelAdapter.getFqName(mappedTransferObjectType));
        }
        modelAdapter.getFilter(mappedTransferObjectType).ifPresent(filterJql -> {
            TypeName typeName = builder.buildTypeName(mappedTransferObjectType).orElseThrow(() -> new IllegalStateException("Illegal type name"));

            if (!hasBinding(typeName, FilterBinding.class)) {
                JqlExpressionBuilder.BindingContext filterBindingContext = new JqlExpressionBuilder.BindingContext(null, JqlExpressionBuilder.BindingType.FILTER, JqlExpressionBuilder.BindingRole.GETTER);
                Expression expression = builder.createExpression(modelAdapter.getMappedEntityType(mappedTransferObjectType).get(), filterJql);
                builder.createBinding(filterBindingContext, null, mappedTransferObjectType, expression);
            } else {
                log.debug("Filter expression already extracted for mapped transfer object type: {}", modelAdapter.getFqName(mappedTransferObjectType));
            }
        });
    }

    private boolean hasBinding(TypeName typeName, Class<? extends Binding> bindingClass) {
        return all(expressionResourceSet, bindingClass)
                .anyMatch(b -> b.getTypeName() != null &&
                        Objects.equals(b.getTypeName().getName(), typeName.getName()) &&
                        Objects.equals(b.getTypeName().getNamespace(), typeName.getNamespace()));
    }

    private void extractFromEntityType(C entityType) {
        if (log.isTraceEnabled()) {
            log.trace("Extracting JQL expressions of entity type {}", modelAdapter.getFqName(entityType));
        }
        TypeName typeName = builder.buildTypeName(entityType).orElseThrow(() -> new IllegalStateException("Invalid type"));

        modelAdapter.getAttributes(entityType)
                .forEach(attribute -> {
                    JqlExpressionBuilder.BindingContext getterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(attribute).get(), JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER);
                    JqlExpressionBuilder.BindingContext setterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(attribute).get(), JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.SETTER);
                    boolean hasGetterBinding = hasAttributeBinding(typeName, modelAdapter.getName(attribute).get(), AttributeBindingRole.GETTER);
                    boolean hasSetterBinding = hasAttributeBinding(typeName, modelAdapter.getName(attribute).get(), AttributeBindingRole.SETTER);

                    if (!modelAdapter.isDerivedAttribute(attribute)) {
                        if (log.isTraceEnabled()) {
                            log.trace("  - extracting JQL expressions of attribute: {}", modelAdapter.getFqName(attribute));
                        }

                        if (!hasGetterBinding) {
                            buildAndBind(entityType, null, getterBindingContext, JqlExpressionBuilder.SELF_NAME + "." + modelAdapter.getName(attribute).get());
                        } else {
                            log.debug("Getter expression already extracted for attribute: {}", attribute);
                        }
                        if (!hasSetterBinding) {
                            buildAndBind(entityType, null, setterBindingContext, JqlExpressionBuilder.SELF_NAME + "." + modelAdapter.getName(attribute).get());
                        } else {
                            log.debug("Setter expression already extracted for attribute: {}", attribute);
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("  - extracting JQL expressions of data property: {}", modelAdapter.getFqName(attribute));
                        }
                        Optional<String> getterJql = modelAdapter.getAttributeGetter(attribute);
                        Optional<String> setterJql = modelAdapter.getAttributeSetter(attribute);

                        if (!hasGetterBinding) {
                            getterJql.ifPresent(jql -> {
                                buildAndBind(entityType, null, getterBindingContext, jql);
                            });
                        } else {
                            log.debug("Getter expression already extracted for data property: {}", attribute);
                        }
                        if (!hasSetterBinding) {
                            setterJql.ifPresent(jql -> {
                                buildAndBind(entityType, null, setterBindingContext, jql);
                            });
                        } else {
                            log.debug("Setter expression already extracted for data property: {}", attribute);
                        }
                    }
                });

        modelAdapter.getReferences(entityType)
                .forEach(reference -> {
                    boolean hasGetterBinding = hasReferenceBinding(typeName, modelAdapter.getName(reference).get(), ReferenceBindingRole.GETTER);
                    boolean hasSetterBinding = hasReferenceBinding(typeName, modelAdapter.getName(reference).get(), ReferenceBindingRole.SETTER);

                    JqlExpressionBuilder.BindingContext getterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(reference).get(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER);
                    JqlExpressionBuilder.BindingContext setterBindingContext = new JqlExpressionBuilder.BindingContext(modelAdapter.getName(reference).get(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.SETTER);

                    if (!modelAdapter.isDerivedReference(reference)) {
                        if (log.isTraceEnabled()) {
                            log.trace("  - extracting JQL expressions of relation: {}", modelAdapter.getFqName(reference));
                        }
                        if (!hasGetterBinding) {
                            buildAndBind(entityType, null, getterBindingContext, JqlExpressionBuilder.SELF_NAME + "." + modelAdapter.getName(reference).get());
                        } else {
                            log.debug("Getter expression already extracted for relation: {}", reference);
                        }
                        if (!hasSetterBinding) {
                            buildAndBind(entityType, null, setterBindingContext, JqlExpressionBuilder.SELF_NAME + "." + modelAdapter.getName(reference).get());
                        } else {
                            log.debug("Setter expression already extracted for relation: {}", reference);
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("  - extracting JQL expressions of navigation property: {}", modelAdapter.getFqName(reference));
                        }

                        Optional<String> getterJql = modelAdapter.getReferenceGetter(reference);
                        Optional<String> setterJql = modelAdapter.getReferenceSetter(reference);

                        if (!hasGetterBinding) {
                            getterJql.ifPresent(jql -> buildAndBind(entityType, null, getterBindingContext, jql));
                        } else {
                            log.debug("Getter expression already extracted for navigation property: {}", reference);
                        }

                        if (!hasSetterBinding) {
                            setterJql.ifPresent(jql -> buildAndBind(entityType, null, setterBindingContext, jql));
                        } else {
                            log.debug("Setter expression already extracted for navigation property: {}", reference);
                        }

                    }
                });
    }

    private void buildAndBind(C entityType, TO transferObjectType, JqlExpressionBuilder.BindingContext bindingContext, String jql) {
        Expression expression = builder.createExpression(entityType, jql);
        builder.createBinding(bindingContext, entityType, transferObjectType, expression);
    }

    static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
        final Iterable<Notifier> resourceContents = resourceSet::getAllContents;
        return StreamSupport.stream(resourceContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

}
