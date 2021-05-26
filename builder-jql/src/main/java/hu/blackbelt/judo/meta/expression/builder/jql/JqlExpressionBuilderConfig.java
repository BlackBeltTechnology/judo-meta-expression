package hu.blackbelt.judo.meta.expression.builder.jql;

/**
 * This class contains settings for expression building.
 */
public class JqlExpressionBuilderConfig {

    private boolean resolveOnlyCurrentLambdaScope = true;

    public boolean isResolveOnlyCurrentLambdaScope() {
        return resolveOnlyCurrentLambdaScope;
    }

    public void setResolveOnlyCurrentLambdaScope(boolean resolveOnlyCurrentLambdaScope) {
        this.resolveOnlyCurrentLambdaScope = resolveOnlyCurrentLambdaScope;
    }

}
