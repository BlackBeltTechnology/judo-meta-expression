package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmModelLoader;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.IntegerConstant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModelLoader;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
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
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEClassBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEEnumBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;

@Slf4j
public class AsmModelAdapterTest {

    private MeasureModel measureModel;
    private AsmModel asmModel;
    private AsmUtils asmUtils;

    private ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = MeasureModelLoader.loadMeasureModel(
                URI.createURI(new File(srcDir(), "test/models/measure.model").getAbsolutePath()),
                "test",
                "1.0.0");
        asmModel = AsmModelLoader.loadAsmModel(
                AsmModelLoader.createAsmResourceSet(),
                URI.createURI(new File(srcDir(), "test/models/asm.model").getAbsolutePath()),
                "test",
                "1.0.0");
        modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
        asmUtils = new AsmUtils(asmModel.getResourceSet());
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
        Optional<TypeName> categoryTypeName = modelAdapter.getTypeName(getEClassByName("Category").get());

        Assert.assertTrue(categoryTypeName.isPresent());
        Assert.assertThat(categoryTypeName.get().getName(), is("Category")); //TODO: check, seems kinda silly (+psm)
        Assert.assertThat(categoryTypeName.get().getNamespace(), is("demo.entities"));
        //TODO: negtest maaaaybe? (+psm)
    }

    //@Test
    public void testGet() {
        //TODO: check if needed
        final TypeName orderTypeName = newTypeNameBuilder()
                .withNamespace("demo.entities")
                .withName("Order")
                .build();


        final Optional<? extends EClassifier> orderEClassifier = modelAdapter.get(orderTypeName);
        Assert.assertTrue(orderEClassifier.isPresent());
        Assert.assertThat(orderEClassifier.get(), instanceOf(EClass.class));
        Assert.assertThat(orderEClassifier.get().getName(), is("Order"));
        Assert.assertThat(asmUtils.getPackageFQName(orderEClassifier.get().getEPackage()), is("demo.entities"));



        final TypeName negtest_name_TypeName = newTypeNameBuilder()
                .withNamespace("demo::entities")
                .withName("negtest")
                .build();
        final Optional<? extends EClassifier> negtest_name_NamespaceElement = modelAdapter.get(negtest_name_TypeName);
        Assert.assertThat(negtest_name_NamespaceElement.isPresent(), is(Boolean.FALSE));

        //TODO: remove b\c not needed?
        final TypeName negtest_namespace_TypeName = newTypeNameBuilder()
                .withNamespace("demo::negtest")
                .withName("negtest")
                .build();
        Assert.assertTrue(modelAdapter.get(negtest_namespace_TypeName) == null);
    }

    @Test
    public void testIsObjectType() {
        EClass eClass = newEClassBuilder().withName("EClass").build();
        EEnum eEnum = newEEnumBuilder().withName("EEnum").build();

        Assert.assertTrue(modelAdapter.isObjectType(eClass));
        Assert.assertFalse(modelAdapter.isObjectType(eEnum));
    }


    @Test
    public void testGetReference() {
        Optional<EClass> containerClass = getEClassByName("Category");
        Optional<EReference> productsReference = containerClass.get().getEReferences().stream().filter(r -> "products".equals(r.getName())).findAny();
        Optional<EClass> targetClass = getEClassByName("Product");

        Assert.assertTrue(containerClass.isPresent());
        Assert.assertTrue(targetClass.isPresent());
        Assert.assertTrue(productsReference.isPresent());
        Assert.assertThat(modelAdapter.getTarget(productsReference.get()), is(targetClass.get()));
    }

    @Test
    public void testGetAttribute() {
        Optional<EClass> eClass = getEClassByName("Category");
        Optional<EAttribute> categoryNameEAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        Assert.assertTrue(eClass.isPresent());
        Assert.assertTrue(categoryNameEAttribute.isPresent());
        Assert.assertThat(modelAdapter.getAttribute(eClass.get(), "categoryName"), is(categoryNameEAttribute));
        Assert.assertThat(modelAdapter.getAttribute(eClass.get(), "productName"), is(Optional.empty()));
    }

    @Test
    public void testGetAttributeType() {
        Optional<EClass> eClass = getEClassByName("Category");
        Optional<EAttribute> eAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        Assert.assertTrue(eClass.isPresent());
        Assert.assertTrue(eAttribute.isPresent());
        Assert.assertThat(modelAdapter.getAttributeType(eClass.get(), "categoryName").get(),
                is(eAttribute.get().getEAttributeType()));
    }

    @Test
    public void testGetSuperType() {
        Optional<EClass> childClass = getEClassByName("Company");
        Optional<EClass> superClass = getEClassByName("Customer");

        Assert.assertTrue(childClass.isPresent());
        Assert.assertTrue(superClass.isPresent());
        Assert.assertTrue(modelAdapter.getSuperTypes(childClass.get()).contains(superClass.get()));

        Optional<EClass> negtestClass = getEClassByName("Order");
        Assert.assertTrue(negtestClass.isPresent());
        Assert.assertFalse(modelAdapter.getSuperTypes(childClass.get()).contains(negtestClass.get()));
        Assert.assertFalse(modelAdapter.getSuperTypes(negtestClass.get()).contains(superClass.get()));
    }

    @Test
    public void testContains() {
        Optional<EEnum> countries = getAsmElement(EEnum.class).filter(e -> "Countries".equals(e.getName())).findAny();
        Assert.assertTrue(countries.isPresent());
        Assert.assertTrue(modelAdapter.contains(countries.get(), "HU"));
        Assert.assertFalse(modelAdapter.contains(countries.get(), "MS"));

        Optional<EEnum> titles = getAsmElement(EEnum.class).filter(e -> "Titles".equals(e.getName())).findAny();
        Assert.assertTrue(titles.isPresent());
        Assert.assertFalse(modelAdapter.contains(titles.get(), "HU"));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        Optional<Measure> time = getMeasureByName("Time");
        Optional<Unit> day = time.get().getUnits().stream().filter(u -> "day".equals(u.getName())).findAny();
        Optional<Unit> halfDay = time.get().getUnits().stream().filter(u -> "halfDay".equals(u.getName())).findAny();

        Assert.assertTrue(time.isPresent());
        Assert.assertTrue(day.isPresent());
        Assert.assertTrue(modelAdapter.isDurationSupportingAddition(day.get()));
        Assert.assertTrue(halfDay.isPresent());
        Assert.assertFalse(modelAdapter.isDurationSupportingAddition(halfDay.get()));
    }

    //TODO
    //@Test
    void testGetUnit() {
        TypeName type = modelAdapter.getTypeName(getEClassByName("Product").get()).get();
        Instance instance = newInstanceBuilder().withElementName(type).build();
        //EClass eClass = newEClassBuilder().withName("Product")..build();

        Optional<Unit> kilogram = getUnitByName("kilogram");
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
        Optional<Unit> inch = getUnitByName("inch");
        Assert.assertTrue(inch.isPresent());
        //5cm téll hossz, 5m as dist -> length-e

        MeasuredDecimal inchMeasuredDecimal = newMeasuredDecimalBuilder()
                .withUnitName(
                        "inch"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("Length").build()
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
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("TODO").build()
                ).build();
        Assert.assertFalse(modelAdapter.getUnit(integerAttribute).isPresent());
        Assert.assertThat(modelAdapter.getUnit(integerAttribute), is(Optional.empty()));
        //--------------------------
        //valid: MeasuredInteger -> NumericExpression
        MeasuredInteger kilogramMeasuredInteger = newMeasuredIntegerBuilder()
                .withUnitName(
                        "kilogram"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("Mass").build()
                ).build();


        Assert.assertTrue(kilogram.isPresent());
        Assert.assertThat(modelAdapter.getUnit(kilogramMeasuredInteger).get(), is(kilogram.get()));
        //--------------------------

        IntegerConstant integerConstant = newIntegerConstantBuilder().build();
        Assert.assertThat(modelAdapter.getUnit(integerConstant), is(Optional.empty()));
    }

    <T> Stream<T> getAsmElement(final Class<T> clazz) {
        final Iterable<Notifier> asmContents = asmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    public Optional<EClass> getEClassByName(final String eClassTypeName) {
        final Iterable<Notifier> asmContents = asmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> EClass.class.isAssignableFrom(e.getClass())).map(e -> (EClass) e)
                .filter(eClass -> Objects.equals(eClass.getName(), eClassTypeName))
                .findAny();
    }

    private Optional<Measure> getMeasureByName(final String measureName) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> Measure.class.isAssignableFrom(e.getClass())).map(e -> (Measure) e)
                .filter(m -> measureName.equals(m.getName()))
                .findAny();
    }

    private Optional<Unit> getUnitByName(final String unitName) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
                .filter(e -> Unit.class.isAssignableFrom(e.getClass())).map(e -> (Unit) e)
                .filter(u -> unitName.equals(u.getName()))
                .findAny();
    }
}
