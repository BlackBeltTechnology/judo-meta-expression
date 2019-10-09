package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

public class AsmJqlExpressionBuilder {

    private static final Logger log = LoggerFactory.getLogger(AsmJqlExpressionBuilder.class.getName());

    private static final String SELF_NAME = "self";

    private final ExpressionModelResourceSupport expressionModelResourceSupport;
    private final AsmUtils asmUtils;

    private final EMap<EClass, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());

    private final JqlParser jqlParser = new JqlParser();

    public AsmJqlExpressionBuilder(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final URI uri) {
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
     * @param entityType         entity type
     * @param expressionAsString expression to process
     * @return data expression of attribute
     */
    public DataExpression createAttributeBinding(final EClass entityType, final String expressionAsString) {
        if (log.isTraceEnabled()) {
            log.trace("Creating attribute binding of {}: {}", AsmUtils.getClassifierFQName(entityType), expressionAsString);
        }

        final Instance instance = entityInstances.get(entityType);
        //final Expression expression = transformJqlToExpression(jqlParser.parseString(expressionAsString));
        final Expression expression = transformJqlToExpression(null);

        if (expression == null || (expression instanceof DataExpression)) {
            return (DataExpression) expression;
        } else {
            log.error("Invalid data expression: {}", expressionAsString);
            return null;
        }
    }

    private Expression transformJqlToExpression(hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression) {
        return null;
    }
}
