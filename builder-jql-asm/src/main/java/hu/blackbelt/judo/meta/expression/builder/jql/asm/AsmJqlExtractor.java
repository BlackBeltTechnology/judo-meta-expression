package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.asm.support.AsmModelResourceSupport;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.AttributeBinding;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBinding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExtractor;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AsmJqlExtractor implements JqlExtractor {

    private static final Logger log = LoggerFactory.getLogger(AsmJqlExtractor.class.getName());

    private final AsmUtils asmUtils;
    private final ResourceSet expressionResourceSet;

    private final JqlExpressionBuilder builder;

    public AsmJqlExtractor(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final ResourceSet expressionResourceSet) {
        this.expressionResourceSet = expressionResourceSet;
        asmUtils = new AsmUtils(asmResourceSet != null ? asmResourceSet : AsmModelResourceSupport.createAsmResourceSet());

        final AsmModelAdapter asmModelAdapter = new AsmModelAdapter(asmResourceSet, measureResourceSet != null ? measureResourceSet : MeasureModelResourceSupport.createMeasureResourceSet());
        builder = new JqlExpressionBuilder(asmModelAdapter, expressionResourceSet.getResources().get(0));
    }

    public AsmJqlExtractor(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final URI uri) {
        this(asmResourceSet, measureResourceSet, ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(uri)
                .build().getResourceSet());
    }

    @Override
    public ResourceSet extractExpressions() {
        asmUtils.all(EClass.class)
                .filter(c -> AsmUtils.isEntityType(c))
                .forEach(entityType -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Extracting JQL expressions of entity type {}", AsmUtils.getClassifierFQName(entityType));
                    }

                    final Optional<TypeName> typeName = builder.getTypeName(entityType);

                    if (!typeName.isPresent()) {
                        throw new IllegalStateException("Illegal type name");
                    }

                    entityType.getEAttributes()
                            .forEach(attribute -> {
                                final boolean hasGetterBinding = all(expressionResourceSet, AttributeBinding.class)
                                        .anyMatch(b -> b.getTypeName() != null &&
                                                Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                b.getRole() == AttributeBindingRole.GETTER &&
                                                Objects.equals(b.getAttributeName(), attribute.getName()));
                                final boolean hasSetterBinding = all(expressionResourceSet, AttributeBinding.class)
                                        .anyMatch(b -> b.getTypeName() != null &&
                                                Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                b.getRole() == AttributeBindingRole.SETTER &&
                                                Objects.equals(b.getAttributeName(), attribute.getName()));

                                final JqlExpressionBuilder.BindingContext getterBindingContext = new JqlExpressionBuilder.BindingContext(attribute.getName(), JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.GETTER);
                                final JqlExpressionBuilder.BindingContext setterBindingContext = new JqlExpressionBuilder.BindingContext(attribute.getName(), JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.SETTER);

                                if (!attribute.isDerived()) {
                                    if (log.isTraceEnabled()) {
                                        log.trace("  - extracting JQL expressions of attribute: {}", AsmUtils.getAttributeFQName(attribute));
                                    }

                                    if (!hasGetterBinding || !hasSetterBinding) {
                                        final Expression expression = builder.createExpression(entityType, JqlExpressionBuilder.SELF_NAME + "." + attribute.getName());

                                        if (!hasGetterBinding) {
                                            builder.createBinding(getterBindingContext, entityType, expression);
                                        } else {
                                            log.debug("Getter expression already extracted for attribute: {}", attribute);
                                        }
                                        if (!hasSetterBinding) {
                                            builder.createBinding(setterBindingContext, entityType, expression);
                                        } else {
                                            log.debug("Setter expression already extracted for attribute: {}", attribute);
                                        }
                                    } else {
                                        log.debug("Getter and setter expressions already extracted for attribute: {}", attribute);
                                    }
                                } else {
                                    if (log.isTraceEnabled()) {
                                        log.trace("  - extracting JQL expressions of data property: {}", AsmUtils.getAttributeFQName(attribute));
                                    }

                                    final boolean hasDefaultBinding = all(expressionResourceSet, AttributeBinding.class)
                                            .anyMatch(b -> b.getTypeName() != null &&
                                                    Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                    Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                    b.getRole() == AttributeBindingRole.DEFAULT &&
                                                    Objects.equals(b.getAttributeName(), attribute.getName()));

                                    final JqlExpressionBuilder.BindingContext defaultBindingContext = new JqlExpressionBuilder.BindingContext(attribute.getName(), JqlExpressionBuilder.BindingType.ATTRIBUTE, JqlExpressionBuilder.BindingRole.DEFAULT);

                                    final Optional<String> getterJql = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "getter", false);
                                    final Optional<String> getterDialect = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "getter.dialect", false);
                                    final Optional<String> setterJql = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "setter", false);
                                    final Optional<String> setterDialect = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "setter.dialect", false);
                                    final Optional<String> defaultJql = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "default", false);
                                    final Optional<String> defaultDialect = asmUtils.getExtensionAnnotationCustomValue(attribute, "expression", "default.dialect", false);

                                    if (!hasGetterBinding) {
                                        if (getterJql.isPresent() && JQL_DIALECT.equals(getterDialect.get())) {
                                            if (getterJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, getterJql.get());
                                                builder.createBinding(getterBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Getter attribute JQL not found");
                                            }
                                        } else if (getterDialect.isPresent()) {
                                            log.warn("Dialect {} of getter attribute is not supported", getterDialect.get());
                                        } else if (getterJql.isPresent()) {
                                            log.warn("Dialect of getter attribute is not specified, skipped expression: {}", getterJql.get());
                                        }
                                    } else {
                                        log.debug("Getter expression already extracted for data property: {}", attribute);
                                    }

                                    if (!hasSetterBinding) {
                                        if (setterDialect.isPresent() && JQL_DIALECT.equals(setterDialect.get())) {
                                            if (setterJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, setterJql.get());
                                                builder.createBinding(setterBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Getter attribute JQL not found");
                                            }
                                        } else if (setterDialect.isPresent()) {
                                            log.warn("Dialect {} of setter attribute is not supported", setterDialect.get());
                                        } else if (setterJql.isPresent()) {
                                            log.warn("Dialect of setter attribute is not specified, skipped expression: {}", setterJql.get());
                                        }
                                    } else {
                                        log.debug("Setter expression already extracted for data property: {}", attribute);
                                    }

                                    if (!hasDefaultBinding) {
                                        if (defaultDialect.isPresent() && JQL_DIALECT.equals(defaultDialect.get())) {
                                            if (defaultJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, defaultJql.get());
                                                builder.createBinding(defaultBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Default attribute JQL not found");
                                            }
                                        } else if (defaultDialect.isPresent()) {
                                            log.warn("Dialect {} of default attribute is not supported", defaultDialect.get());
                                        } else if (defaultJql.isPresent()) {
                                            log.warn("Dialect of default attribute is not specified, skipped expression: {}", defaultJql.get());
                                        }
                                    } else {
                                        log.debug("Default expression already extracted for data property: {}", attribute);
                                    }
                                }
                            });

                    entityType.getEReferences()
                            .forEach(reference -> {
                                final boolean hasGetterBinding = all(expressionResourceSet, ReferenceBinding.class)
                                        .anyMatch(b -> b.getTypeName() != null &&
                                                Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                b.getRole() == ReferenceBindingRole.GETTER &&
                                                Objects.equals(b.getReferenceName(), reference.getName()));
                                final boolean hasSetterBinding = all(expressionResourceSet, ReferenceBinding.class)
                                        .anyMatch(b -> b.getTypeName() != null &&
                                                Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                b.getRole() == ReferenceBindingRole.SETTER &&
                                                Objects.equals(b.getReferenceName(), reference.getName()));

                                final JqlExpressionBuilder.BindingContext getterBindingContext = new JqlExpressionBuilder.BindingContext(reference.getName(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.GETTER);
                                final JqlExpressionBuilder.BindingContext setterBindingContext = new JqlExpressionBuilder.BindingContext(reference.getName(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.SETTER);

                                if (!reference.isDerived()) {
                                    if (log.isTraceEnabled()) {
                                        log.trace("  - extracting JQL expressions of relation: {}", AsmUtils.getReferenceFQName(reference));
                                    }

                                    if (!hasGetterBinding || !hasSetterBinding) {
                                        final Expression expression = builder.createExpression(entityType, JqlExpressionBuilder.SELF_NAME + "." + reference.getName());

                                        if (!hasGetterBinding) {
                                            builder.createBinding(getterBindingContext, entityType, expression);
                                        } else {
                                            log.debug("Getter expression already extracted for relation: {}", reference);
                                        }
                                        if (!hasSetterBinding) {
                                            builder.createBinding(setterBindingContext, entityType, expression);
                                        } else {
                                            log.debug("Setter expression already extracted for relation: {}", reference);
                                        }
                                    } else {
                                        log.debug("Getter and setter expressions already extracted for relation: {}", reference);
                                    }
                                } else {
                                    if (log.isTraceEnabled()) {
                                        log.trace("  - extracting JQL expressions of navigation property: {}", AsmUtils.getReferenceFQName(reference));
                                    }

                                    final boolean hasDefaultBinding = all(expressionResourceSet, ReferenceBinding.class)
                                            .anyMatch(b -> b.getTypeName() != null &&
                                                    Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                    Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                    b.getRole() == ReferenceBindingRole.DEFAULT &&
                                                    Objects.equals(b.getReferenceName(), reference.getName()));
                                    final boolean hasRangeBinding = all(expressionResourceSet, ReferenceBinding.class)
                                            .anyMatch(b -> b.getTypeName() != null &&
                                                    Objects.equals(b.getTypeName().getName(), typeName.get().getName()) &&
                                                    Objects.equals(b.getTypeName().getNamespace(), typeName.get().getNamespace()) &&
                                                    b.getRole() == ReferenceBindingRole.RANGE &&
                                                    Objects.equals(b.getReferenceName(), reference.getName()));

                                    final JqlExpressionBuilder.BindingContext defaultBindingContext = new JqlExpressionBuilder.BindingContext(reference.getName(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.DEFAULT);
                                    final JqlExpressionBuilder.BindingContext rangeBindingContext = new JqlExpressionBuilder.BindingContext(reference.getName(), JqlExpressionBuilder.BindingType.RELATION, JqlExpressionBuilder.BindingRole.RANGE);

                                    final Optional<String> getterJql = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "getter", false);
                                    final Optional<String> getterDialect = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "getter.dialect", false);
                                    final Optional<String> setterJql = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "setter", false);
                                    final Optional<String> setterDialect = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "setter.dialect", false);
                                    final Optional<String> defaultJql = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "default", false);
                                    final Optional<String> defaultDialect = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "default.dialect", false);
                                    final Optional<String> rangeJql = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "range", false);
                                    final Optional<String> rangeDialect = asmUtils.getExtensionAnnotationCustomValue(reference, "expression", "range.dialect", false);

                                    if (!hasGetterBinding) {
                                        if (getterJql.isPresent() && JQL_DIALECT.equals(getterDialect.get())) {
                                            if (getterJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, getterJql.get());
                                                builder.createBinding(getterBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Getter reference JQL not found");
                                            }
                                        } else if (getterDialect.isPresent()) {
                                            log.warn("Dialect {} of getter reference is not supported", getterDialect.get());
                                        } else if (getterJql.isPresent()) {
                                            log.warn("Dialect of getter reference is not specified, skipped expression: {}", getterJql.get());
                                        }
                                    } else {
                                        log.debug("Getter expression already extracted for navigation property: {}", reference);
                                    }

                                    if (!hasSetterBinding) {
                                        if (setterDialect.isPresent() && JQL_DIALECT.equals(setterDialect.get())) {
                                            if (setterJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, setterJql.get());
                                                builder.createBinding(setterBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Setter reference JQL not found");
                                            }
                                        } else if (setterDialect.isPresent()) {
                                            log.warn("Dialect {} of setter reference is not supported", setterDialect.get());
                                        } else if (setterJql.isPresent()) {
                                            log.warn("Dialect of setter reference is not specified, skipped expression: {}", setterJql.get());
                                        }
                                    } else {
                                        log.debug("Setter expression already extracted for navigation property: {}", reference);
                                    }

                                    if (!hasDefaultBinding) {
                                        if (defaultDialect.isPresent() && JQL_DIALECT.equals(defaultDialect.get())) {
                                            if (defaultJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, defaultJql.get());
                                                builder.createBinding(defaultBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Default reference JQL not found");
                                            }
                                        } else if (defaultDialect.isPresent()) {
                                            log.warn("Dialect {} of default reference is not supported", defaultDialect.get());
                                        } else if (defaultJql.isPresent()) {
                                            log.warn("Dialect of default reference is not specified, skipped expression: {}", defaultJql.get());
                                        }
                                    } else {
                                        log.debug("Default expression already extracted for navigation property: {}", reference);
                                    }

                                    if (!hasRangeBinding) {
                                        if (rangeDialect.isPresent() && JQL_DIALECT.equals(rangeDialect.get())) {
                                            if (rangeJql.isPresent()) {
                                                final Expression expression = builder.createExpression(entityType, rangeJql.get());
                                                builder.createBinding(rangeBindingContext, entityType, expression);
                                            } else {
                                                throw new IllegalStateException("Range reference JQL not found");
                                            }
                                        } else if (rangeDialect.isPresent()) {
                                            log.warn("Dialect {} of range reference is not supported", rangeDialect.get());
                                        } else if (rangeJql.isPresent()) {
                                            log.warn("Dialect of range reference is not specified, skipped expression: {}", rangeJql.get());
                                        }
                                    } else {
                                        log.debug("Range expression already extracted for navigation property: {}", reference);
                                    }
                                }
                            });
                });
        return expressionResourceSet;
    }

    static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
        final Iterable<Notifier> asmContents = resourceSet::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
