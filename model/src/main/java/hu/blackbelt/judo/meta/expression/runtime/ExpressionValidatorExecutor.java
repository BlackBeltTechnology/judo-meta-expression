package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;

import java.util.Collection;

public interface ExpressionValidatorExecutor {

    void setSource(Logger log, ExpressionModel expressionModel, ModelAdapter modelAdapter, String adaptedName, Resource adapted, String measuresName, Resource measure);
    void execute(Collection<String> expectedErrors, Collection<String> expectedWarnings) throws ExpressionValidationException;
}
