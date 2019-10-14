package hu.blackbelt.judo.meta.expression.builder.jql;

import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Extract all JQL expressions from a given model and create expression resource set.
 */
public interface JqlExtractor {

    String JQL_DIALECT = "JQL";

    /**
     * Extract all JQL expressions that have no binding yet to a given resource set (created by constructor).
     *
     * @return expression resource set
     */
    ResourceSet extractExpressions();
}
