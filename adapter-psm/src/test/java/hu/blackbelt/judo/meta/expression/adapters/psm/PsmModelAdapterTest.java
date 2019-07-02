package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.psm.data.*;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModelLoader;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;

@Slf4j
public class PsmModelAdapterTest {

    private PsmModel measureModel;
    private PsmModel psmModel;

    private ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, ReferenceTypedElement, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        psmModel = PsmModelLoader.loadPsmModel(
                URI.createURI(new File(srcDir(), "test/models/psm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        measureModel = psmModel;

        modelAdapter = new PsmModelAdapter(psmModel.getResourceSet(), measureModel.getResourceSet());
    }

    @AfterEach
    public void tearDown() {
        modelAdapter = null;
    }

    private File srcDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../src");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    @Test
    public void testGetTypeName() {
        log.info("Testing: getTypeName(Namespace)...");

        Optional<TypeName> categoryTypeName = modelAdapter.getTypeName(getEntityTypeByName("Category").get());

        Assert.assertTrue(categoryTypeName.isPresent());
        Assert.assertThat(categoryTypeName.get().getName(), is("Category"));
        Assert.assertThat(categoryTypeName.get().getNamespace(), is("northwind::entities"));
    }

    @Test
    void testGetNamespaceElementByTypeName() {
        log.info("Testing: get(Typename): NamespaceElement...");
        final TypeName orderTypeName = newTypeNameBuilder()
                .withNamespace("northwind::entities")
                .withName("Order")
                .build();

        final TypeName negtestTypeName = newTypeNameBuilder()
                .withNamespace("northwind::entities")
                .withName("negtest")
                .build();

        final Optional<? extends NamespaceElement> orderNamespaceElement = modelAdapter.get(orderTypeName);
        Assert.assertThat(orderNamespaceElement.isPresent(), is(Boolean.TRUE));
        Assert.assertThat(orderNamespaceElement.get(), instanceOf(EntityType.class));
        Assert.assertThat(orderNamespaceElement.get().getName(), is("Order"));
        Assert.assertThat(getNamespaceFQName(getNamespaceOfNamespaceElement(orderNamespaceElement.get()).get()), is("northwind::entities"));
        //TODO: check copypasted stuff below

        final Optional<? extends NamespaceElement> negtestNamespaceElement = modelAdapter.get(negtestTypeName);
        Assert.assertThat(negtestNamespaceElement.isPresent(), is(Boolean.FALSE));
    }

    @Test
    void testGetMeasureByMeasureName() {
        log.info("Testing: get(MeasureName)...");
        final MeasureName measureName = newMeasureNameBuilder()
                .withNamespace("northwind::measures")
                .withName("Mass")
                .build();
        final Optional<? extends Measure> measure = modelAdapter.get(measureName);

        Assert.assertThat(measure.isPresent(), is(Boolean.TRUE));
        Assert.assertThat(measure.get(), instanceOf(Measure.class));
        Assert.assertThat(measure.get().getName(), is("Mass"));
    }

    @Test
    void testIsObjectType() {
        log.info("Testing: boolean isObjectType(NamespaceElement)...");

        EntityType entityType = newEntityTypeBuilder().withName("EntityType").build();
        StringType stringType = TypeBuilders.newStringTypeBuilder().withName("String").withMaxLength(-3).build();

        Assert.assertTrue(modelAdapter.isObjectType(entityType));
        Assert.assertFalse(modelAdapter.isObjectType(stringType));
    }

    @Test
    void testGetReference() {
        log.info("Testing: Optional(RefTypEl) getReference(ET, String)...");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Relation> productsRelation = entityType.get().getRelations().stream().filter(r -> "products".equals(r.getName())).findAny();

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(productsRelation.isPresent());
        Assert.assertThat(modelAdapter.getReference(entityType.get(), "products"), is(productsRelation));

        Assert.assertThat(modelAdapter.getReference(entityType.get(), "store"), is(Optional.empty()));
    }

    void testIsCollection() {
        log.info("Testing: boolean isCollection(ReferenceExpression)...");

        Optional<EntityType> orderEntity = getEntityTypeByName("Order");

        /*ReferenceExpression refExpr */
        String exprOfRefExprType = orderEntity.get().getNavigationProperties().stream().filter(np -> "categories".equals(np.getName())).findAny()
                .get().getGetterExpression().getExpression();
        //TODO: make sense of life
        //TODO: (self.x.y...)
    }

    @Test
    void testGetTarget() {
        log.debug("Testing: EntityType getTarget(RTE)...");
        Optional<EntityType> containerEntityType = getEntityTypeByName("Category");
        Optional<Relation> productsRelation = containerEntityType.get().getRelations().stream().filter(r -> "products".equals(r.getName())).findAny();
        Optional<EntityType> targetEntityType = getEntityTypeByName("Product");

        Assert.assertTrue(containerEntityType.isPresent());
        Assert.assertTrue(targetEntityType.isPresent());
        Assert.assertTrue(productsRelation.isPresent());
        Assert.assertThat(modelAdapter.getTarget(productsRelation.get()), is(targetEntityType.get()));
    }

    @Test
    void testGetAttribute() {
        log.debug("Testing: Optional<PTE> getAttribute(ET, attributeName)...");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Attribute> categoryNameAttribute = entityType.get().getAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(categoryNameAttribute.isPresent());
        Assert.assertThat(modelAdapter.getAttribute(entityType.get(), "categoryName"), is(categoryNameAttribute));
        Assert.assertThat(modelAdapter.getAttribute(entityType.get(), "productName"), is(Optional.empty()));
    }

    @Test
    void testGetAttributeType() {
        log.debug("Testing: Optional<? extends Primitive> getAttributeType(EntityType clazz, String attributeName)...");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<PrimitiveTypedElement> primitiveTypedElement = getPrimitiveTypedElementByName("categoryName");

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(primitiveTypedElement.isPresent());
        Assert.assertThat(
                modelAdapter.getAttribute(entityType.get(), "categoryName").get().getDataType(), instanceOf(StringType.class)
        );
    }

    @Test
    void testGetSuperTypes() {
        log.debug("Testing: Coll<? ext ET> getSuperTypes(ET)...");

        Optional<EntityType> childEntity = getEntityTypeByName("Company");
        Optional<EntityType> superEntity = getEntityTypeByName("Customer");

        Assert.assertTrue(superEntity.isPresent());
        Assert.assertTrue(childEntity.isPresent());
        //Assert.assertThat(modelAdapter.getSuperTypes(childEntity.get()), hasSize(1)); // Matcher.contains() implicitly includes this case check
        Assert.assertThat(modelAdapter.getSuperTypes(childEntity.get()), contains(superEntity.get()));

        Optional<EntityType> negtestEntity = getEntityTypeByName("Order");
        Assert.assertTrue(negtestEntity.isPresent());
        Assert.assertFalse(modelAdapter.getSuperTypes(childEntity.get()).contains(negtestEntity.get()));
        Assert.assertFalse(modelAdapter.getSuperTypes(negtestEntity.get()).contains(superEntity.get()));
    } //TODO: endcheck

    @Test
    void testPrimitiveTypeChecks() {
        log.debug("Testing: isNumeric, isInteger, isDecimal, etc...");
        //getting all elements of package "types"
        EList<NamespaceElement> primitives = getPsmElement(Package.class).filter(p -> "types".equals(p.getName())).findAny().get().getElements();

        //get NE by name & cast (according to psm.model)
        StringType phone = (StringType) primitives.stream().filter(p -> "Phone".equals(p.getName())).findAny().get();

        Assert.assertTrue(modelAdapter.isString(phone));
        Assert.assertFalse(modelAdapter.isNumeric(phone));

        //TODO: ask if not dumdum
        //then, TODO: isNumeric, isInteger(?), isDecimal, isBoolean, isEnumeration, isDate, isTimestamp, isCustom
        //TODO: ask: isCustom
    }

    //@Test
    void testIsMeasured() {
        log.debug("Testing: boolean isMeasured(NumericExpression)...");
        //TODO: ask why isMeasure(NumExpr) { return false } in PsmEntityModelAdapter;
    }

    @Test
    void testContains() {
        log.debug("Testing: boolean contains(EnumType, memberName)");

        Optional<EnumerationType> countries = getPsmElement(EnumerationType.class).filter(e -> "Countries".equals(e.getName())).findAny();

        Assert.assertTrue(countries.isPresent());
        Assert.assertTrue(modelAdapter.contains(countries.get(), "HU"));
        Assert.assertFalse(modelAdapter.contains(countries.get(), "MS"));

        Optional<EnumerationType> titles = getPsmElement(EnumerationType.class).filter(e -> "Titles".equals(e.getName())).findAny();
        Assert.assertTrue(titles.isPresent());
        Assert.assertFalse(modelAdapter.contains(titles.get(), "HU"));
    }

    @Test
    void testGetDurationMeasure() {
        log.debug("Testing: Opt<Measure> getDurationMeasure()...");
        Optional<Measure> time = getMeasureByName("Time");
        Optional<Unit> day = time.get().getUnits().stream().filter(u -> "day".equals(u.getName())).findAny();

        Assert.assertTrue(time.isPresent());
        Assert.assertTrue(day.isPresent());
        Assert.assertTrue(modelAdapter.getDurationMeasure().get().getUnits().contains(day.get()));
        //TODO: negtest?
        //Optional<Unit> halfDay = time.get().getUnits().stream().filter(u -> "halfDay".equals(u.getName())).findAny();
        //Assert.assertTrue(halfDay.isPresent());
        //Assert.assertFalse(modelAdapter.getDurationMeasure().get().getUnits().contains(halfDay.get())); // is true, should be false
    }

    @Test
    void testIsDurationSupportingAddition() {
        log.debug("Testing: boolean isDurSuppAdd(Unit)...");

        Optional<Measure> time = getMeasureByName("Time");
        Optional<Unit> day = time.get().getUnits().stream().filter(u -> "day".equals(u.getName())).findAny();
        Optional<Unit> halfDay = time.get().getUnits().stream().filter(u -> "halfDay".equals(u.getName())).findAny();

        Assert.assertTrue(time.isPresent());
        Assert.assertTrue(day.isPresent());
        Assert.assertTrue(modelAdapter.isDurationSupportingAddition(day.get()));
        Assert.assertTrue(halfDay.isPresent());
        Assert.assertFalse(modelAdapter.isDurationSupportingAddition(halfDay.get()));
    }

    //@Test
    void testGetMeasure() {
        //TODO
    }










    //@Test
    void testGetUnit() {
        log.debug("Testing: Opt<Unit> getUnit(NumExpr)...");

        //NumericExpression numericExpression = NumericBuilders.new.

        /*
        log.debug("Testing: Opt<Unit> getUnit(Attribute)");
        Optional<Attribute> weight = getPsmElement(Attribute.class).filter(a -> "weight".equals(a.getName())).findAny();
        Assert.assertTrue(weight.isPresent());
        Assert.assertThat(modelAdapter.);
         */
    }












    void testGetDimension() {
        //TODO
    }

    private Optional<EntityType> getEntityTypeByName(final String entityTypeName) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> EntityType.class.isAssignableFrom(e.getClass())).map(e -> (EntityType) e)
                .filter(entityType -> Objects.equals(entityType.getName(), entityTypeName))
                .findAny();
    }

    private Optional<Measure> getMeasureByName(final String measureName) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(m -> Measure.class.isAssignableFrom(m.getClass())).map(m -> (Measure) m)
                .filter(measure -> Objects.equals(measure.getName(), measureName))
                .findAny();
    }

    private Optional<ReferenceTypedElement> getReferenceTypedElementByName(final String referenceTypedElementName) {
        return getPsmElement(ReferenceTypedElement.class)
                .filter(rte -> Objects.equals(rte.getName(), referenceTypedElementName))
                .findAny();
    }

    private Optional<PrimitiveTypedElement> getPrimitiveTypedElementByName(final String pteName) {
        return getPsmElement(PrimitiveTypedElement.class)
                .filter(pte -> Objects.equals(pte.getName(), pteName))
                .findAny();
    }

    //TODO: note: copypasterino from PsmModelAdapter; ask: should be made public
    <T> Stream<T> getPsmElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    private Optional<Namespace> getNamespaceOfNamespaceElement(final NamespaceElement namespaceElement) {
        return getPsmElement(Namespace.class)
                .filter(ns -> ns.getElements().contains(namespaceElement))
                .findAny();
    }

    private String getNamespaceFQName(final Namespace namespace) {
        final Optional<Namespace> containerNamespace;
        if (namespace instanceof Package) {
            containerNamespace = getNamespaceOfPackage((Package) namespace);
        } else {
            containerNamespace = Optional.empty();
        }

        return (containerNamespace.isPresent() ? getNamespaceFQName(containerNamespace.get()) + "::" : "") + namespace.getName();
    }

    private Optional<Namespace> getNamespaceOfPackage(final Package pkg) {
        return getPsmElement(Namespace.class)
                .filter(ns -> ns.getPackages().contains(pkg))
                .findAny();
    }
    //...end of copypasterino for testGetNamespaceElementByTypeName() ~ namespace check


}
