package hu.blackbelt.judo.meta.expression.adapters.psm;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.IntegerConstant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.psm.PsmUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
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

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newStringTypeBuilder;
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
        log.info("Testing: getTypeName(NE): TypeName");

        Optional<TypeName> categoryTypeName = modelAdapter.getTypeName(getEntityTypeByName("Category").get());

        Assert.assertTrue(categoryTypeName.isPresent());
        Assert.assertThat(categoryTypeName.get().getName(), is("Category"));
        Assert.assertThat(categoryTypeName.get().getNamespace(), is("northwind::entities"));
    }

    @Test
    void testGetNamespaceElementByTypeName() {
        log.info("Testing: get(TypeName): NE");
        final TypeName orderTypeName = newTypeNameBuilder()
                .withNamespace("northwind::entities")
                .withName("Order")
                .build();


        final Optional<? extends NamespaceElement> orderNamespaceElement = modelAdapter.get(orderTypeName);
        Assert.assertTrue(orderNamespaceElement.isPresent());
        Assert.assertThat(orderNamespaceElement.get(), instanceOf(EntityType.class));
        Assert.assertThat(orderNamespaceElement.get().getName(), is("Order"));
        Assert.assertThat(getNamespaceFQName(PsmUtils.getNamespaceOfNamespaceElement(orderNamespaceElement.get()).get()), is("northwind::entities"));

        final TypeName negtest_name_TypeName = newTypeNameBuilder()
                .withNamespace("northwind::entities")
                .withName("negtest")
                .build();
        final Optional<? extends NamespaceElement> negtest_name_NamespaceElement = modelAdapter.get(negtest_name_TypeName);
        Assert.assertThat(negtest_name_NamespaceElement.isPresent(), is(Boolean.FALSE));

        //TODO: remove b\c not needed?
        final TypeName negtest_namespace_TypeName = newTypeNameBuilder()
                .withNamespace("northwind::negtest")
                .withName("negtest")
                .build();
        Assert.assertTrue(modelAdapter.get(negtest_namespace_TypeName) == null);
    }

    @Test
    void testGetMeasureByMeasureName() {
        log.info("Testing: get(MeasureName): Opt<Measure>");
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
        log.info("Testing: isObjectType(NamespaceElement): boolean");

        EntityType entityType = newEntityTypeBuilder().withName("EntityType").build();
        StringType stringType = newStringTypeBuilder().withName("String").withMaxLength(-3).build();

        Assert.assertTrue(modelAdapter.isObjectType(entityType));
        Assert.assertFalse(modelAdapter.isObjectType(stringType));
    }

    @Test
    void testGetReference() {
        log.info("Testing: getReference(ET, String): Opt<RefTypEl>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Relation> productsRelation = entityType.get().getRelations().stream().filter(r -> "products".equals(r.getName())).findAny();

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(productsRelation.isPresent());
        Assert.assertThat(modelAdapter.getReference(entityType.get(), "products"), is(productsRelation));

        Assert.assertThat(modelAdapter.getReference(entityType.get(), "store"), is(Optional.empty()));
    }

    @Test
    void testGetTarget() {
        log.debug("Testing: getTarget(RTE): EntityType");
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
        log.debug("Testing: getAttribute(ET, attributeName): Opt<PTE>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Attribute> categoryNameAttribute = entityType.get().getAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(categoryNameAttribute.isPresent());
        Assert.assertThat(modelAdapter.getAttribute(entityType.get(), "categoryName"), is(categoryNameAttribute));
        Assert.assertThat(modelAdapter.getAttribute(entityType.get(), "productName"), is(Optional.empty()));
    }

    @Test
    void testGetAttributeType() {
        log.debug("Testing: getAttributeType(EntityType clazz, String attributeName): Optional<? extends Primitive>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<PrimitiveTypedElement> primitiveTypedElement = getPrimitiveTypedElementByName("categoryName");

        Assert.assertTrue(entityType.isPresent());
        Assert.assertTrue(primitiveTypedElement.isPresent());
        Assert.assertThat(
                modelAdapter.getAttributeType(entityType.get(), "categoryName").get(), is(primitiveTypedElement.get().getDataType())
                //modelAdapter.getAttribute(entityType.get(), "categoryName").get().getDataType(), instanceOf(StringType.class) //TODO: ask what was this
        );
    }

    @Test
    void testGetSuperTypes() {
        log.debug("Testing: getSuperTypes(ET): Coll<? ext ET>");
        Optional<EntityType> childEntity = getEntityTypeByName("Company");
        Optional<EntityType> superEntity = getEntityTypeByName("Customer");

        Assert.assertTrue(superEntity.isPresent());
        Assert.assertTrue(childEntity.isPresent());
        Assert.assertThat(modelAdapter.getSuperTypes(childEntity.get()), contains(superEntity.get()));

        Optional<EntityType> negtestEntity = getEntityTypeByName("Order");
        Assert.assertTrue(negtestEntity.isPresent());
        Assert.assertFalse(modelAdapter.getSuperTypes(childEntity.get()).contains(negtestEntity.get()));
        Assert.assertFalse(modelAdapter.getSuperTypes(negtestEntity.get()).contains(superEntity.get()));
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

    //TODO: [MeasuredDecimal & MeasuredInteger].getMeasure()==null => return Optional.empty()?
    @Test
    void testGetUnit() {
        log.debug("Testing: Opt<Unit> getUnit(NumExpr)...");
        //--------------------------
        TypeName type = modelAdapter.getTypeName(getEntityTypeByName("Product").get()).get();
        Instance instance = newInstanceBuilder().withElementName(type).build();

        Optional<Unit> kilogram = getPsmElement(Unit.class).filter(u -> "kilogram".equals(u.getName())).findAny();
        NumericAttribute numericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("weight")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
                                .withReferenceName("self")
                                .build()
                )
                .build();
        Assert.assertTrue(modelAdapter.getUnit(numericAttribute).isPresent());
        Assert.assertThat(modelAdapter.getUnit(numericAttribute).get(), is(kilogram.get()));

        NumericAttribute nonMeasureNumericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("quantityPerUnit")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
                                .withReferenceName("self")
                                .build()
                )
                .build();
        Assert.assertFalse(modelAdapter.getUnit(nonMeasureNumericAttribute).isPresent());
        Assert.assertThat(modelAdapter.getUnit(nonMeasureNumericAttribute), is(Optional.empty()));

        NumericAttribute notFoundAttributeNumericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("somethingNonExistent")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
                                .withReferenceName("self")
                                .build()
                )
                .build();
        Assert.assertFalse(modelAdapter.getUnit(notFoundAttributeNumericAttribute).isPresent());
        Assert.assertThat(modelAdapter.getUnit(notFoundAttributeNumericAttribute), is(Optional.empty()));
        //--------------------------
        Optional<Unit> inch = getPsmElement(Unit.class).filter(u -> "inch".equals(u.getName())).findAny();
        Assert.assertTrue(inch.isPresent());
        //5cm téll hossz, 5m as dist -> length-e

        MeasuredDecimal inchMeasuredDecimal = newMeasuredDecimalBuilder()
                .withUnitName(
                        "inch"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("northwind::measures").withName("Length").build()
                ).build();
        Assert.assertThat(modelAdapter.getUnit(inchMeasuredDecimal).get(), is(inch.get()));
        //--------------------------
        MeasuredDecimal measuredDecimal = newMeasuredDecimalBuilder()
                .withUnitName("TODO")
                .withMeasure(
                        newMeasureNameBuilder().withName("TODO").build()
                ).build();
        Assert.assertFalse(modelAdapter.getUnit(measuredDecimal).isPresent());
        Assert.assertThat(modelAdapter.getUnit(measuredDecimal), is(Optional.empty()));
        //--------------------------
        //empty: MeasuredInteger -> NumericExpression
        MeasuredInteger integerAttribute = newMeasuredIntegerBuilder()
                .withUnitName("TODO")
                .withMeasure(
                        newMeasureNameBuilder().withNamespace("northwind::measures").withName("TODO").build()
                ).build();
        Assert.assertFalse(modelAdapter.getUnit(integerAttribute).isPresent());
        Assert.assertThat(modelAdapter.getUnit(integerAttribute), is(Optional.empty()));
        //--------------------------
        //valid: MeasuredInteger -> NumericExpression
        MeasuredInteger kilogramMeasuredInteger = newMeasuredIntegerBuilder()
                .withUnitName(
                        "kilogram"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("northwind::measures").withName("Mass").build()
                ).build();


        Assert.assertTrue(kilogram.isPresent());
        Assert.assertThat(modelAdapter.getUnit(kilogramMeasuredInteger).get(), is(kilogram.get()));
        //--------------------------

        IntegerConstant integerConstant = newIntegerConstantBuilder().build();
        Assert.assertThat(modelAdapter.getUnit(integerConstant), is(Optional.empty()));
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

    private Optional<PrimitiveTypedElement> getPrimitiveTypedElementByName(final String pteName) {
        return getPsmElement(PrimitiveTypedElement.class)
                .filter(pte -> Objects.equals(pte.getName(), pteName))
                .findAny();
    }

    <T> Stream<T> getPsmElement(final Class<T> clazz) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    private String getNamespaceFQName(final Namespace namespace) {
        final Optional<Namespace> containerNamespace;
        if (namespace instanceof Package) {
            containerNamespace = PsmUtils.getNamespaceOfPackage((Package) namespace);
        } else {
            containerNamespace = Optional.empty();
        }

        return (containerNamespace.isPresent() ? getNamespaceFQName(containerNamespace.get()) + "::" : "") + namespace.getName();
    }

}