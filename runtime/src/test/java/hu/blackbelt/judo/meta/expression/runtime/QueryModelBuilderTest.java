package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.runtime.adapters.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.runtime.query.Select;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.UUID;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceIdBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

@Slf4j
public class QueryModelBuilderTest {

    private ResourceSet asmResourceSet;
    private ResourceSet measureResourceSet;

    private ResourceSet expressionResourceSet;
    private Resource expressionResource;

    private ModelAdapter modelAdapter;

    @BeforeEach
    void setUp() throws Exception {
        // Set our custom handler
        URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), targetDir().getAbsolutePath());

        final File asmModelFile = new File(srcDir(), "test/models/northwind-asm.model");
        asmResourceSet = AsmModelLoader.createAsmResourceSet(uriHandler, new AsmModelLoader.LocalAsmPackageRegistration());
        asmResourceSet.setURIConverter(new ExecutionContextOnAsmTest.ModelFileBasedUriConverter(asmModelFile));
        AsmModelLoader.loadAsmModel(asmResourceSet,
                URI.createURI(asmModelFile.getAbsolutePath()), "test", "1.0.0");

        final File measureModelFile = new File(srcDir(), "test/models/measure.model");
        measureResourceSet = MeasureModelLoader.createMeasureResourceSet(uriHandler);
        measureResourceSet.setURIConverter(new ExecutionContextOnAsmTest.ModelFileBasedUriConverter(measureModelFile));
        MeasureModelLoader.loadMeasureModel(measureResourceSet,
                URI.createURI(measureModelFile.getAbsolutePath()), "test", "1.0.0");

        modelAdapter = new AsmModelAdapter(asmResourceSet, measureResourceSet);

        expressionResourceSet = ExpressionModelLoader.createExpressionResourceSet();

        final String createdSourceModelName = "urn:expression.judo-meta-expression";
        expressionResource = expressionResourceSet.createResource(
                URI.createURI(createdSourceModelName));
    }

    @AfterEach
    void tearDown() {
        modelAdapter = null;
        asmResourceSet = null;
        measureResourceSet = null;
        expressionResourceSet = null;
    }

    @Test
    public void testSimpleQuery() {
        final TypeName orderType = newTypeNameBuilder().withNamespace("northwind.entities").withName("Order").build();
        final TypeName orderInfoType = newTypeNameBuilder().withNamespace("northwind.services").withName("OrderInfo").build();

        final UUID orderId = UUID.randomUUID();

        final Instance order = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .withDefinition(newInstanceIdBuilder().withId(orderId.toString()).build())
                .build();

        final Collection<Expression> expressions = ImmutableList.<Expression>builder()
                .add(order)
                .build();

        expressionResource.getContents().addAll(ImmutableList.<EObject>builder()
                .add(orderType)
                .addAll(expressions)
                .build());

        final ExpressionEvaluator evaluator = new ExpressionEvaluator();
        evaluator.init(expressions);

        final EvaluationNode evaluationNode = evaluator.getEvaluationNode(order);

        final QueryModelBuilder queryModelBuilder = new QueryModelBuilder(modelAdapter);
        final Select queryModel = queryModelBuilder.createQueryModel(evaluationNode, (EClass) modelAdapter.get(orderInfoType).get());

        log.debug("QUERY MODEL: \n{}", queryModel);
    }

    protected File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    public File targetDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }
}
