package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

public final class CreateExpressionArguments<C, TO, NE> {
    private final C clazz;
    private final TO inputParameterType;
    private final NE contextNamespacePreset;
    private final JqlExpression jqlExpression;
    private final String jqlExpressionAsString;
    private final ExpressionBuildingVariableResolver context;

    private CreateExpressionArguments(C clazz, TO inputParameterType, NE contextNamespacePreset,
                                      JqlExpression jqlExpression, String jqlExpressionAsString,
                                      ExpressionBuildingVariableResolver context) {
        this.clazz = clazz;
        this.inputParameterType = inputParameterType;
        this.contextNamespacePreset = contextNamespacePreset;
        this.jqlExpression = jqlExpression;
        this.jqlExpressionAsString = jqlExpressionAsString;
        this.context = context;
    }

    public C getClazz() {
        return clazz;
    }

    public TO getInputParameterType() {
        return inputParameterType;
    }

    public NE getContextNamespacePreset() {
        return contextNamespacePreset;
    }

    public JqlExpression getJqlExpression() {
        return jqlExpression;
    }

    public String getJqlExpressionAsString() {
        return jqlExpressionAsString;
    }

    public ExpressionBuildingVariableResolver getContext() {
        return context;
    }

    public static <C, TO, NE> CreateExpressionArgumentsBuilder<C, TO, NE> builder() {
        return new CreateExpressionArgumentsBuilder<>();
    }

    public static class CreateExpressionArgumentsBuilder<C, TO, NE> {
        private C clazz;
        private TO inputParameterType;
        private NE contextNamespacePreset;
        private String jqlExpressionAsString;
        private ExpressionBuildingVariableResolver context;
        private JqlExpression jqlExpression;

        public CreateExpressionArguments<C, TO, NE> build() {
            return new CreateExpressionArguments<>(clazz, inputParameterType, contextNamespacePreset, jqlExpression,
                                                   jqlExpressionAsString, context);
        }

        public C getClazz() {
            return clazz;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withClazz(C clazz) {
            this.clazz = clazz;
            return this;
        }

        public TO getInputParameterType() {
            return inputParameterType;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withInputParameterType(TO inputParameterType) {
            this.inputParameterType = inputParameterType;
            return this;
        }

        public NE getContextNamespacePreset() {
            return contextNamespacePreset;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withContextNamespacePreset(NE contextNamespacePreset) {
            this.contextNamespacePreset = contextNamespacePreset;
            return this;
        }

        public JqlExpression getJqlExpression() {
            return jqlExpression;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withJqlExpression(JqlExpression jqlExpression) {
            this.jqlExpression = jqlExpression;
            return this;
        }

        public String getJqlExpressionAsString() {
            return jqlExpressionAsString;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withJqlExpressionAsString(String jqlExpressionAsString) {
            this.jqlExpressionAsString = jqlExpressionAsString;
            return this;
        }

        public ExpressionBuildingVariableResolver getContext() {
            return context;
        }

        public CreateExpressionArgumentsBuilder<C, TO, NE> withContext(ExpressionBuildingVariableResolver context) {
            this.context = context;
            return this;
        }
    }
}
