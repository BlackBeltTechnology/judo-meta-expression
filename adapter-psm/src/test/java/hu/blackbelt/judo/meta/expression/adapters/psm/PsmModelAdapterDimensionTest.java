package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class PsmModelAdapterDimensionTest {

    private MeasureAdapter<Measure, Unit, EntityType> measureAdapter;
    private MeasureProvider<Measure, Unit> measureProvider;
    private Resource resource;

    @BeforeEach
    public void setUp() {
        final ResourceSet resourceSet = PsmModelLoader.createPsmResourceSet();
        resource = resourceSet.createResource(URI.createURI("urn:psm.judo-meta-psm"));
        measureProvider = new PsmMeasureProvider(resourceSet);

        final ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> modelAdapter = Mockito.mock(ModelAdapter.class);

        Mockito.doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            if (args[0] instanceof MeasuredDecimal) {
                final MeasuredDecimal measuredDecimal = (MeasuredDecimal) args[0];
                return measureAdapter.getUnit(measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getNamespace()) : Optional.empty(),
                        measuredDecimal.getMeasure() != null ? Optional.ofNullable(measuredDecimal.getMeasure().getName()) : Optional.empty(),
                        measuredDecimal.getUnitName());
            } else {
                throw new IllegalStateException("Not supported by mock");
            }
        }).when(modelAdapter).getUnit(any(NumericExpression.class));

        measureAdapter = new MeasureAdapter<>(measureProvider, modelAdapter);
    }

    @AfterEach
    public void tearDown() {
        resource = null;
        measureAdapter = null;
    }
}
