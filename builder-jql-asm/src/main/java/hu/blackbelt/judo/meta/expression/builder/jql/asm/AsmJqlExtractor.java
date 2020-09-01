package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.AdaptableJqlExtractor;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.support.MeasureModelResourceSupport;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;

public class AsmJqlExtractor extends AdaptableJqlExtractor {

    public AsmJqlExtractor(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final ResourceSet expressionResourceSet) {
        super(asmResourceSet, measureResourceSet, expressionResourceSet, new AsmModelAdapter(asmResourceSet, measureResourceSet != null ? measureResourceSet : MeasureModelResourceSupport.createMeasureResourceSet()));
    }

    public AsmJqlExtractor(final ResourceSet asmResourceSet, final ResourceSet measureResourceSet, final URI uri) {
        this(asmResourceSet, measureResourceSet, ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(uri)
                .build().getResourceSet());
    }

}
