package hu.blackbelt.judo.meta.expression.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import hu.blackbelt.epsilon.runtime.execution.impl.NioFilesystemnRelativePathURIHandlerImpl;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.expression.runtime.query.Attribute;
import hu.blackbelt.judo.meta.expression.runtime.query.Join;
import hu.blackbelt.judo.meta.expression.runtime.query.Select;
import hu.blackbelt.judo.meta.expression.runtime.query.SubSelect;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;
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

        final Select select = Select.builder()
                .from(order)
                .target(orderInfo)
                .features(ImmutableMap.of(
                        (EAttribute) orderInfo.getEStructuralFeature("orderDate"),
                        Attribute.builder().targetAttribute((EAttribute) order.getEStructuralFeature("orderDate")).build(),

                        (EAttribute) orderInfo.getEStructuralFeature("shipperName"),
                        Attribute.builder().targetAttribute((EAttribute) shipper.getEStructuralFeature("companyName")).build()
                ))
                .joins(ImmutableList.of(
                        Join.builder().joined(shipper).reference((EReference) orderInfo.getEStructuralFeature("shipper")).build()
                ))
                .subSelects(ImmutableMap.of(
                        (EReference) orderInfo.getEStructuralFeature("categories"),
                        SubSelect.builder()
                                .joins(ImmutableList.of(
                                        Join.builder().joined(orderDetails).reference((EReference) order.getEStructuralFeature("orderDetails")).build(),
                                        Join.builder().joined(product).reference((EReference) orderDetails.getEStructuralFeature("product")).build(),
                                        Join.builder().joined(category).reference((EReference) product.getEStructuralFeature("category")).build()
                                ))
                                .select(Select.builder()
                                        .from(category)
                                        .target(categoryInfo)
                                        .features(ImmutableMap.of(
                                                (EAttribute) categoryInfo.getEStructuralFeature("categoryName"),
                                                Attribute.builder().targetAttribute((EAttribute) category.getEStructuralFeature("categoryName")).build()
                                        ))
                                        .build())
                                .build(),

                        (EReference) orderInfo.getEStructuralFeature("items"),
                        SubSelect.builder()
                                .joins(ImmutableList.of(
                                        Join.builder().joined(orderDetails).reference((EReference) order.getEStructuralFeature("orderDetails")).build()
                                ))
                                .select(Select.builder()
                                        .from(orderDetails)
                                        .target(orderItem)
                                        .features(ImmutableMap.of(
                                                (EAttribute) orderItem.getEStructuralFeature("unitPrice"),
                                                Attribute.builder().targetAttribute((EAttribute) orderDetails.getEStructuralFeature("unitPrice")).build(),

                                                (EAttribute) orderItem.getEStructuralFeature("quantity"),
                                                Attribute.builder().targetAttribute((EAttribute) orderDetails.getEStructuralFeature("quantity")).build(),

                                                (EAttribute) orderItem.getEStructuralFeature("discount"),
                                                Attribute.builder().targetAttribute((EAttribute) orderDetails.getEStructuralFeature("discount")).build()
                                        ))
                                        .build())
                                .build()
                ))
                .build();
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
