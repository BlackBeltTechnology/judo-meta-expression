package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.runtime.query.*;
import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class QueryAsmTest {

    private ResourceSet resourceSet;

    private static final String NAMESPACE_SEPARATOR = ".";
    private final Map<String, EPackage> namespaceCache = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        // Set our custom handler
        URIHandler uriHandler = new NioFilesystemnRelativePathURIHandlerImpl("urn", FileSystems.getDefault(), targetDir().getAbsolutePath());

        final File modelFile = new File(srcDir(), "test/models/northwind-asm.model");
        resourceSet = AsmModelLoader.createAsmResourceSet(uriHandler, new AsmModelLoader.LocalAsmPackageRegistration());
        resourceSet.setURIConverter(new ExecutionContextOnAsmTest.ModelFileBasedUriConverter(modelFile));
        AsmModelLoader.loadAsmModel(resourceSet,
                URI.createURI(modelFile.getAbsolutePath()), "test", "1.0.0");

        final Iterable<Notifier> asmContents = resourceSet::getAllContents;
        StreamSupport.stream(asmContents.spliterator(), false)
                .filter(e -> e instanceof EPackage).map(e -> (EPackage) e)
                .filter(e -> e.getESuperPackage() == null)
                .forEach(m -> initNamespace(Collections.emptyList(), m));
    }

    private void initNamespace(final Iterable<EPackage> path, final EPackage namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.join(NAMESPACE_SEPARATOR, StreamSupport.stream(path.spliterator(), false).map(p -> p.getName()).collect(Collectors.toList())));
        if (sb.length() > 0) {
            sb.append(NAMESPACE_SEPARATOR);
        }
        sb.append(namespace.getName());

        final String key = sb.toString();

        namespaceCache.put(key, namespace);
        log.debug("Cached namespace: {}", key);

        final Iterable<EPackage> newPath = Iterables.concat(path, Collections.singleton(namespace));
        namespace.getESubpackages().stream().forEach(p -> initNamespace(newPath, p));
    }

    @AfterEach
    void tearDown() {
        resourceSet = null;
    }

    @Test
    public void testQuery() {
        final EClass order = (EClass) namespaceCache.get("northwind.entities").getEClassifier("Order");
        final EClass orderDetails = (EClass) namespaceCache.get("northwind.entities").getEClassifier("OrderDetail");
        final EClass shipper = (EClass) namespaceCache.get("northwind.entities").getEClassifier("Shipper");
        final EClass product = (EClass) namespaceCache.get("northwind.entities").getEClassifier("Product");
        final EClass category = (EClass) namespaceCache.get("northwind.entities").getEClassifier("Category");

        final EClass orderInfo = (EClass) namespaceCache.get("northwind.services").getEClassifier("OrderInfo");
        final EClass orderItem = (EClass) namespaceCache.get("northwind.services").getEClassifier("OrderItem");
        final EClass categoryInfo = (EClass) namespaceCache.get("northwind.services").getEClassifier("CategoryInfo");

        final UUID orderId = UUID.randomUUID();

        final Target target = Target.builder()
                .type(orderInfo)
                .build();

        final Select select = Select.builder()
                .from(order)
                .targets(ImmutableList.of(target))
                .build();
        select.getJoins().addAll(ImmutableList.of(
                Join.builder().partner(select).reference((EReference) order.getEStructuralFeature("shipper")).build()
        ));
        select.getFilters().addAll(ImmutableSet.of(FunctionSignature.EQUALS.create(ImmutableMap.of(
                FunctionSignature.TwoOperandFunctionParameterName.left.name(), SingleParameter.builder().feature(IdAttribute.builder().target(target).build()).build(),
                FunctionSignature.TwoOperandFunctionParameterName.right.name(), SingleParameter.builder().feature(Constant.<UUID>builder().value(orderId).build()).build())
        )));
        target.getFeatures().addAll(ImmutableList.of(
                Attribute.builder()
                        .target(target)
                        .targetAttribute((EAttribute) orderInfo.getEStructuralFeature("orderDate"))
                        .source(select)
                        .sourceAttribute((EAttribute) order.getEStructuralFeature("orderDate")).build(),
                Attribute.builder()
                        .target(target)
                        .targetAttribute((EAttribute) orderInfo.getEStructuralFeature("shipperName"))
                        .source(select.getJoins().get(0))
                        .sourceAttribute((EAttribute) shipper.getEStructuralFeature("companyName")).build()
        ));

        final Target target1 = Target.builder().type(categoryInfo).build();
        final Select select1 = Select.builder()
                .from(category)
                .targets(ImmutableList.of(target1))
                .build();
        target1.getFeatures().addAll(ImmutableList.of(
                Attribute.builder()
                        .target(target1)
                        .targetAttribute((EAttribute) categoryInfo.getEStructuralFeature("categoryName"))
                        .source(select1)
                        .sourceAttribute((EAttribute) category.getEStructuralFeature("categoryName")).build()
        ));
        final SubSelect subSelect1 = SubSelect.builder()
                .select(select1)
                .alias("categories")
                .build();
        final Join joinOrderDetails = Join.builder().partner(select).reference((EReference) order.getEStructuralFeature("orderDetails")).build();
        final Join joinProduct = Join.builder().partner(joinOrderDetails).reference((EReference) orderDetails.getEStructuralFeature("product")).build();
        final Join joinCategory = Join.builder().partner(joinProduct).reference((EReference) product.getEStructuralFeature("category")).build();

        subSelect1.getJoins().addAll(ImmutableList.of(joinOrderDetails, joinProduct, joinCategory));

        final Target target2 = Target.builder().type(orderItem).build();
        final Select select2 = Select.builder()
                .from(orderDetails)
                .targets(ImmutableList.of(target2))
                .build();
        target2.getFeatures().addAll(
                ImmutableList.of(
                        Attribute.builder()
                                .target(target2)
                                .targetAttribute((EAttribute) orderItem.getEStructuralFeature("unitPrice"))
                                .source(select2)
                                .sourceAttribute((EAttribute) orderDetails.getEStructuralFeature("unitPrice")).build(),
                        Attribute.builder()
                                .target(target2)
                                .targetAttribute((EAttribute) orderItem.getEStructuralFeature("quantity"))
                                .source(select2)
                                .sourceAttribute((EAttribute) orderDetails.getEStructuralFeature("quantity")).build(),
                        Attribute.builder()
                                .target(target2)
                                .targetAttribute((EAttribute) orderItem.getEStructuralFeature("discount"))
                                .source(select2)
                                .sourceAttribute((EAttribute) orderDetails.getEStructuralFeature("discount")).build()
                ));
        final SubSelect subSelect2 = SubSelect.builder()
                .select(select2)
                .alias("items")
                .build();
        subSelect2.getJoins().addAll(ImmutableList.of(
                Join.builder().partner(select).reference((EReference) order.getEStructuralFeature("orderDetails")).build()
        ));
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
