package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
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
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.LoadArguments.asmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.loadAsmModel;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.LoadArguments.measureLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.loadMeasureModel;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEClassBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEEnumBuilder;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

public class AsmModelAdapterTest {

    private MeasureModel measureModel;
    private AsmModel asmModel;
    private AsmUtils asmUtils;

    private ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EReference, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        measureModel = loadMeasureModel(measureLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/measure.model").getAbsolutePath()))
                .name("measure"));

        asmModel = loadAsmModel(asmLoadArgumentsBuilder()
                .uri(URI.createFileURI(new File("src/test/model/asm.model").getAbsolutePath()))
                .name("asm"));

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

        assertTrue(categoryTypeName.isPresent());
        assertThat(categoryTypeName.get().getName(), is("Category")); //TODO: check, seems kinda silly (+psm)
        assertThat(categoryTypeName.get().getNamespace(), is("demo.entities"));
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
        assertTrue(orderEClassifier.isPresent());
        assertThat(orderEClassifier.get(), instanceOf(EClass.class));
        assertThat(orderEClassifier.get().getName(), is("Order"));
        assertThat(asmUtils.getPackageFQName(orderEClassifier.get().getEPackage()), is("demo.entities"));



        final TypeName negtest_name_TypeName = newTypeNameBuilder()
                .withNamespace("demo::entities")
                .withName("negtest")
                .build();
        final Optional<? extends EClassifier> negtest_name_NamespaceElement = modelAdapter.get(negtest_name_TypeName);
        assertThat(negtest_name_NamespaceElement.isPresent(), is(Boolean.FALSE));

        //TODO: remove b\c not needed?
        final TypeName negtest_namespace_TypeName = newTypeNameBuilder()
                .withNamespace("demo::negtest")
                .withName("negtest")
                .build();
        assertTrue(modelAdapter.get(negtest_namespace_TypeName) == null);
    }

    @Test
    public void testIsObjectType() {
        EClass eClass = newEClassBuilder().withName("EClass").build();
        EEnum eEnum = newEEnumBuilder().withName("EEnum").build();

        assertTrue(modelAdapter.isObjectType(eClass));
        assertFalse(modelAdapter.isObjectType(eEnum));
    }


    @Test
    public void testGetReference() {
        Optional<EClass> containerClass = getEClassByName("Category");
        Optional<EReference> productsReference = containerClass.get().getEReferences().stream().filter(r -> "products".equals(r.getName())).findAny();
        Optional<EClass> targetClass = getEClassByName("Product");

        assertTrue(containerClass.isPresent());
        assertTrue(targetClass.isPresent());
        assertTrue(productsReference.isPresent());
        assertThat(modelAdapter.getTarget(productsReference.get()), is(targetClass.get()));
    }

    @Test
    public void testGetAttribute() {
        Optional<EClass> eClass = getEClassByName("Category");
        Optional<EAttribute> categoryNameEAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        assertTrue(eClass.isPresent());
        assertTrue(categoryNameEAttribute.isPresent());
        assertThat(modelAdapter.getAttribute(eClass.get(), "categoryName"), is(categoryNameEAttribute));
        assertThat(modelAdapter.getAttribute(eClass.get(), "productName"), is(Optional.empty()));
    }

    @Test
    public void testGetAttributeType() {
        Optional<EClass> eClass = getEClassByName("Category");
        Optional<EAttribute> eAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        assertTrue(eClass.isPresent());
        assertTrue(eAttribute.isPresent());
        assertThat(modelAdapter.getAttributeType(eClass.get(), "categoryName").get(),
                is(eAttribute.get().getEAttributeType()));
    }

    @Test
    public void testGetSuperType() {
        Optional<EClass> childClass = getEClassByName("Company");
        Optional<EClass> superClass = getEClassByName("Customer");

        assertTrue(childClass.isPresent());
        assertTrue(superClass.isPresent());
        assertTrue(modelAdapter.getSuperTypes(childClass.get()).contains(superClass.get()));

        Optional<EClass> negtestClass = getEClassByName("Order");
        assertTrue(negtestClass.isPresent());
        assertFalse(modelAdapter.getSuperTypes(childClass.get()).contains(negtestClass.get()));
        assertFalse(modelAdapter.getSuperTypes(negtestClass.get()).contains(superClass.get()));
    }

    @Test
    public void testContains() {
        Optional<EEnum> countries = getAsmElement(EEnum.class).filter(e -> "Countries".equals(e.getName())).findAny();
        assertTrue(countries.isPresent());
        assertTrue(modelAdapter.contains(countries.get(), "HU"));
        assertFalse(modelAdapter.contains(countries.get(), "MS"));

        Optional<EEnum> titles = getAsmElement(EEnum.class).filter(e -> "Titles".equals(e.getName())).findAny();
        assertTrue(titles.isPresent());
        assertFalse(modelAdapter.contains(titles.get(), "HU"));
    }

    @Test
    public void testIsDurationSupportingAddition() {
        Optional<Measure> time = getMeasureByName("Time");
        Optional<Unit> day = time.get().getUnits().stream().filter(u -> "day".equals(u.getName())).findAny();
        Optional<Unit> halfDay = time.get().getUnits().stream().filter(u -> "halfDay".equals(u.getName())).findAny();

        assertTrue(time.isPresent());
        assertTrue(day.isPresent());
        assertTrue(modelAdapter.isDurationSupportingAddition(day.get()));
        assertTrue(halfDay.isPresent());
        assertFalse(modelAdapter.isDurationSupportingAddition(halfDay.get()));
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
        assertTrue(modelAdapter.getUnit(numericAttribute).isPresent());
        assertThat(modelAdapter.getUnit(numericAttribute).get(), is(kilogram.get()));

        NumericAttribute nonMeasureNumericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("quantityPerUnit")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
                                .withReferenceName("self")
                                .build()
                )
                .build();
        assertFalse(modelAdapter.getUnit(nonMeasureNumericAttribute).isPresent());
        assertThat(modelAdapter.getUnit(nonMeasureNumericAttribute), is(Optional.empty()));

        NumericAttribute notFoundAttributeNumericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("somethingNonExistent")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
                                .withReferenceName("self")
                                .build()
                )
                .build();
        assertFalse(modelAdapter.getUnit(notFoundAttributeNumericAttribute).isPresent());
        assertThat(modelAdapter.getUnit(notFoundAttributeNumericAttribute), is(Optional.empty()));
        //--------------------------
        Optional<Unit> inch = getUnitByName("inch");
        assertTrue(inch.isPresent());
        //5cm téll hossz, 5m as dist -> length-e

        MeasuredDecimal inchMeasuredDecimal = newMeasuredDecimalBuilder()
                .withUnitName(
                        "inch"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("Length").build()
                ).build();
        assertThat(modelAdapter.getUnit(inchMeasuredDecimal).get(), is(inch.get()));
        //--------------------------
        MeasuredDecimal measuredDecimal = newMeasuredDecimalBuilder()
                .withUnitName("TODO")
                .withMeasure(
                        newMeasureNameBuilder().withName("TODO").build()
                ).build();
        assertFalse(modelAdapter.getUnit(measuredDecimal).isPresent());
        assertThat(modelAdapter.getUnit(measuredDecimal), is(Optional.empty()));
        //--------------------------
        //empty: MeasuredInteger -> NumericExpression
        MeasuredInteger integerAttribute = newMeasuredIntegerBuilder()
                .withUnitName("TODO")
                .withMeasure(
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("TODO").build()
                ).build();
        assertFalse(modelAdapter.getUnit(integerAttribute).isPresent());
        assertThat(modelAdapter.getUnit(integerAttribute), is(Optional.empty()));
        //--------------------------
        //valid: MeasuredInteger -> NumericExpression
        MeasuredInteger kilogramMeasuredInteger = newMeasuredIntegerBuilder()
                .withUnitName(
                        "kilogram"
                ).withMeasure(
                        newMeasureNameBuilder().withNamespace("demo::measures").withName("Mass").build()
                ).build();


        assertTrue(kilogram.isPresent());
        assertThat(modelAdapter.getUnit(kilogramMeasuredInteger).get(), is(kilogram.get()));
        //--------------------------

        IntegerConstant integerConstant = newIntegerConstantBuilder().build();
        assertThat(modelAdapter.getUnit(integerConstant), is(Optional.empty()));
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