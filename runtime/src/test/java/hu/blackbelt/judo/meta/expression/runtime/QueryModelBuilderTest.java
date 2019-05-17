package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.ObjectNavigationFromCollectionExpression;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.runtime.adapters.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.runtime.query.Select;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
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

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

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

        final TimestampAttribute orderDate = newTimestampAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withAttributeName("orderDate")
                .withAlias("orderDate")
                .build();

        final StringExpression shipperName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(order)
                                .build())
                        .withName("s")
                        .withReferenceName("shipper")
                        .build())
                .withAttributeName("companyName")
                .withAlias("shipperName")
                .build();

        final CollectionNavigationFromObjectExpression orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .build();

        final ObjectVariable od = orderDetails.createIterator("od", "items", modelAdapter);

        final NumericExpression unitPrice = newDecimalAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(od)
                        .build())
                .withAttributeName("unitPrice")
                .withAlias("unitPrice")
                .build();

        final NumericExpression quantity = newIntegerAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(od)
                        .build())
                .withAttributeName("quantity")
                .withAlias("quantity")
                .build();

        final NumericExpression discount = newDecimalAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(od)
                        .build())
                .withAttributeName("discount")
                .withAlias("discount")
                .build();

        final ObjectNavigationExpression product = newObjectNavigationExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(od)
                        .build())
                .withReferenceName("product")
                .build();

        final ObjectVariable p = product.createIterator("p", "product", modelAdapter);

        final StringExpression productName = newStringAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(p)
                        .build())
                .withAttributeName("productName")
                .withAlias("productName")
                .build();

        final ObjectNavigationFromCollectionExpression categories = newObjectNavigationFromCollectionExpressionBuilder()
                .withCollectionExpression(newObjectNavigationFromCollectionExpressionBuilder()
                        .withCollectionExpression(newCollectionNavigationFromObjectExpressionBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(order)
                                        .build())
                                .withReferenceName("orderDetails")
                                .build())
                        .withReferenceName("product")
                        .withName("p")
                        .build())
                .withReferenceName("category")
                .withName("c")
                .build();

        final ObjectVariable category = categories.createIterator("cat", "categories", modelAdapter);

        final StringExpression categoryName = newStringAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(category)
                        .build())
                .withAttributeName("categoryName")
                .withAlias("categoryName")
                .build();

        final Collection<Expression> expressions = ImmutableList.<Expression>builder()
                .add(order)
                .add(orderDate)
                .add(shipperName)
                .add(orderDetails)
                .add(unitPrice)
                .add(quantity)
                .add(product)
                .add(productName)
                .add(discount)
                .add(categories)
                .add(categoryName)
                .build();

        expressionResource.getContents().addAll(ImmutableList.<EObject>builder()
                .add(orderType)
                .addAll(expressions)
                .build());

        try {
            final long startTs = System.currentTimeMillis();
            final ExpressionEvaluator evaluator = new ExpressionEvaluator();
            evaluator.init(expressions);

            final EvaluationNode evaluationNode = evaluator.getEvaluationNode(order);

            final QueryModelBuilder queryModelBuilder = new QueryModelBuilder(modelAdapter, evaluator);

            final Select queryModel = queryModelBuilder.createQueryModel(evaluationNode, (EClass) modelAdapter.get(orderInfoType).get());

            final long endTs = System.currentTimeMillis();
            log.debug("Query model created in {} ms", (endTs - startTs));

            log.info("QUERY MODEL: \n{}", queryModel);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
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
