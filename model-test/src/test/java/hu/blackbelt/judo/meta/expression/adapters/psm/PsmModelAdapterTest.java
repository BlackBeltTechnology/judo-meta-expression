package hu.blackbelt.judo.meta.expression.adapters.psm;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredIntegerBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newAttributeBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.newEntityTypeBuilder;
import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.*;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.*;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasureDefinitionTermBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newMeasuredTypeBuilder;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.newUnitBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.useModel;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.usePackage;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newNumericTypeBuilder;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.notify.Notifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.IntegerConstant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.psm.PsmEpsilonValidator;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.data.ReferenceTypedElement;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.meta.psm.data.Sequence;
import hu.blackbelt.judo.meta.psm.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.psm.measure.DurationType;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.meta.psm.type.StringType;

public class PsmModelAdapterTest {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PsmModelAdapterTest.class);
    private PsmModel measureModel;
    private PsmModel psmModel;

    private ModelAdapter<NamespaceElement, Primitive, PrimitiveTypedElement, EnumerationType, EntityType, TransferObjectType, ReferenceTypedElement, Sequence, Measure, Unit> modelAdapter;

    private static final Logger logger = LoggerFactory.getLogger(PsmModelAdapterTest.class);
    
    @BeforeEach
    public void setUp() throws Exception {

    	measureModel = buildPsmModel().name("test").build();
        populateMeasureModel();

        psmModel = buildPsmModel().name("test").build();
        
        Model model = newModelBuilder().withName("demo").build();
        
        final Primitive stringType = newStringTypeBuilder()
        		.withMaxLength(256)
                .withName("String")
                .build();

        Primitive doubleType = newNumericTypeBuilder()
                .withName("Integer")
                .withScale(7)
                .withPrecision(18)
                .build();

        Primitive kgType = newMeasuredTypeBuilder()
                .withName("kg")
                .withPrecision(18)
                .withScale(7)
                .withStoreUnit(getMeasureElement(Unit.class).filter(u -> "kg".equals(u.getSymbol())).findAny().get())
                .build();

        Primitive cmType = newMeasuredTypeBuilder()
                .withName("cm")
                .withPrecision(18)
                .withScale(7)
                .withStoreUnit(getMeasureElement(Unit.class).filter(u -> "cm".equals(u.getSymbol())).findAny().get())
                .build();

        EntityType product = newEntityTypeBuilder()
                .withName("Product")
                .withAttributes(ImmutableList.of(
                        newAttributeBuilder().withName("discount").withDataType(doubleType).build(),
                        newAttributeBuilder().withName("url").withDataType(stringType).build(),
                        newAttributeBuilder().withName("unitPrice").withDataType(doubleType).build(),
                        newAttributeBuilder().withName("weight").withDataType(kgType).build(),
                        newAttributeBuilder().withName("height").withDataType(cmType).build()
                )).build();
        
        EntityType customer = newEntityTypeBuilder()
        		.withName("Customer")
        		.build();
        
        EntityType company = newEntityTypeBuilder()
        		.withName("Company")
        		.withSuperEntityTypes(customer)
        		.build();
        
        EntityType order = newEntityTypeBuilder()
        		.withName("Order")
        		.build();
        EntityType category = newEntityTypeBuilder()
        		.withName("Category")
        		.withAttributes(newAttributeBuilder().withName("categoryName").withDataType(stringType).build())
        		.build();
        
        AssociationEnd products = newAssociationEndBuilder().withName("products").withTarget(product)
				.withCardinality(newCardinalityBuilder().withLower(0).withUpper(-1).build()).build();
        AssociationEnd categoryRef = newAssociationEndBuilder().withName("category").withTarget(category)
				.withCardinality(newCardinalityBuilder().withLower(1).withUpper(1).build()).build();
		
        useAssociationEnd(products).withPartner(categoryRef).build();
        useAssociationEnd(categoryRef).withPartner(products).build();
        
        useEntityType(category).withRelations(products).build();
        useEntityType(product).withRelations(categoryRef).build();
        
        EnumerationType countries = newEnumerationTypeBuilder().withName("Countries").withMembers(
        		newEnumerationMemberBuilder().withName("HU").withOrdinal(0).build(),
        		newEnumerationMemberBuilder().withName("AT").withOrdinal(1).build(),
        		newEnumerationMemberBuilder().withName("RO").withOrdinal(2).build(),
        		newEnumerationMemberBuilder().withName("SK").withOrdinal(3).build()
        		).build();
        EnumerationType titles = newEnumerationTypeBuilder().withName("Titles").withMembers(
        		newEnumerationMemberBuilder().withName("MS").withOrdinal(0).build(),
        		newEnumerationMemberBuilder().withName("MRS").withOrdinal(1).build(),
        		newEnumerationMemberBuilder().withName("MR").withOrdinal(2).build(),
        		newEnumerationMemberBuilder().withName("DR").withOrdinal(3).build()
        		).build();
        
        Package entities = newPackageBuilder().withName("entities").withElements(category,order,customer,company,product).build();
        
        useModel(model).withElements(stringType,doubleType,kgType,cmType,countries,titles)
        	.withPackages(entities).build();
        psmModel.addContent(model);

        log.info(psmModel.getDiagnosticsAsString());
  		log.info(measureModel.getDiagnosticsAsString());
    	assertTrue(psmModel.isValid());
    	assertTrue(measureModel.isValid());
    	runEpsilon(psmModel);
    	runEpsilon(measureModel);
        
        modelAdapter = new PsmModelAdapter(psmModel.getResourceSet(), measureModel.getResourceSet());
    }
    
    private void populateMeasureModel() {  	  
  	  
    	Model demo = newModelBuilder().withName("demo").build();
    	Package measures = newPackageBuilder().withName("measures").build();
    	useModel(demo).withPackages(measures).build();
    	
        Measure time = newMeasureBuilder().withName("Time").withUnits(
        		newDurationUnitBuilder().withName("nanosecond").withSymbol("ns").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0E+9)).withUnitType(DurationType.NANOSECOND).build(),
        		newDurationUnitBuilder().withName("microsecond").withSymbol("μs").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1000000.0)).withUnitType(DurationType.MICROSECOND).build(),
    			newDurationUnitBuilder().withName("millisecond").withSymbol("ms").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.MILLISECOND).build(),
    			newDurationUnitBuilder().withName("second").withSymbol("s").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.SECOND).build(),
    			newDurationUnitBuilder().withName("minute").withSymbol("min").withRateDividend(new Double(60.0)).withRateDivisor(new Double(1.0)).build(),
    			newDurationUnitBuilder().withName("hour").withSymbol("h").withRateDividend(new Double(3600.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.HOUR).build(),
    			newDurationUnitBuilder().withName("day").withSymbol("").withRateDividend(new Double(86400.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.DAY).build(),
    			newDurationUnitBuilder().withName("week").withSymbol("").withRateDividend(new Double(604800.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.WEEK).build(),
    			newDurationUnitBuilder().withName("halfDay").withSymbol("").withRateDividend(new Double(43200.0)).withRateDivisor(new Double(1.0)).build())
        	.build();
        
        Measure monthBasedTime = newMeasureBuilder().withName("MonthBasedTime").withUnits(
    			newDurationUnitBuilder().withName("month").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.MONTH).build(),
    			newDurationUnitBuilder().withName("year").withRateDividend(new Double(12.0)).withRateDivisor(new Double(1.0)).withUnitType(DurationType.YEAR).build())
        	.build();
        
        Measure mass = newMeasureBuilder().withName("Mass").withUnits(
        		newUnitBuilder().withName("milligram").withSymbol("mg").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("gram").withSymbol("g").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("dekagram").withSymbol("dkg").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("kilogram").withSymbol("kg").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("quintal").withSymbol("q").withRateDividend(new Double(100.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("tonne").withSymbol("t").withRateDividend(new Double(1000.0)).withRateDivisor(new Double(1.0)).build())
    		.build();
        
        Measure length = newMeasureBuilder().withName("Length").withUnits(
        		newUnitBuilder().withName("nanometre").withSymbol("nm").withRateDividend(new Double(1.0E-9)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("micrometre").withSymbol("μm").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("millimetre").withSymbol("mm").withRateDividend(new Double(0.001)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("centimetre").withSymbol("cm").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("decimetre").withSymbol("dm").withRateDividend(new Double(0.1)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("metre").withSymbol("m").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("kilometre").withSymbol("km").withRateDividend(new Double(1000.0)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("inch").withSymbol("&quot;").withRateDividend(new Double(0.0254)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("foot").withSymbol("ft").withRateDividend(new Double(0.3048)).withRateDivisor(new Double(1.0)).build(),
        		newUnitBuilder().withName("mile").withSymbol("mi").withRateDividend(new Double(1609.344)).withRateDivisor(new Double(1.0)).build())
    		.build();
        
        DerivedMeasure velocity = newDerivedMeasureBuilder().withName("Velocity").withUnits(
        		newUnitBuilder().withName("kilometrePerHour").withSymbol("km/h").withRateDividend(new Double(1.0)).withRateDivisor(new Double(3.6)).build(),
        		newUnitBuilder().withName("metrePerSecond").withSymbol("m/s").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(length.getUnits().get(6)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(-1).withUnit(time.getUnits().get(5)).build())
    		.build();
        
    	DerivedMeasure area = newDerivedMeasureBuilder().withName("Area").withUnits(
      			newUnitBuilder().withName("squareMillimetre").withSymbol("mm²").withRateDividend(new Double(0.0000010)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareCentimetre").withSymbol("cm²").withRateDividend(new Double(0.00010)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareDecimetre").withSymbol("dm²").withRateDividend(new Double(0.01)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareMetre").withSymbol("m²").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("hectare").withSymbol("ha").withRateDividend(new Double(10000.0)).withRateDivisor(new Double(1.0)).build(),
      			newUnitBuilder().withName("squareKilometre").withSymbol("km²").withRateDividend(new Double(1000000.0)).withRateDivisor(new Double(1.0)).build())
    			.withTerms(newMeasureDefinitionTermBuilder().withExponent(2).withUnit(length.getUnits().get(0)).build())
    	.build();
    	
        DerivedMeasure force = newDerivedMeasureBuilder().withName("Force").withUnits(
        		newUnitBuilder().withName("newton").withSymbol("N").withRateDividend(new Double(1.0)).withRateDivisor(new Double(1.0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(-2).withUnit(time.getUnits().get(0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(mass.getUnits().get(0)).build())
        		.withTerms(newMeasureDefinitionTermBuilder().withExponent(1).withUnit(length.getUnits().get(0)).build())
    		.build();
        
        usePackage(measures).withElements(time,mass,length,velocity,area,force,monthBasedTime).build();
        
        measureModel.addContent(demo);
    }
    
    private void runEpsilon(PsmModel model) throws Exception {
        try {
            PsmEpsilonValidator.validatePsm(new Slf4jLog(),
            		model,
            		PsmEpsilonValidator.calculatePsmValidationScriptURI(),
            		Collections.emptyList(),
            		Collections.emptyList());
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            throw ex;
        }
    }
    
    @AfterEach
    public void tearDown() {
        modelAdapter = null;
    }

    @Test
    public void testGetTypeName() {
        log.info("Testing: getTypeName(NE): TypeName");

        Optional<TypeName> categoryTypeName = modelAdapter.buildTypeName(getEntityTypeByName("Category").get());

        assertTrue(categoryTypeName.isPresent());
        assertThat(categoryTypeName.get().getName(), is("Category"));
        assertThat(categoryTypeName.get().getNamespace(), is("demo::entities"));
    }

    @Test
    void testGetNamespaceElementByTypeName() {
        log.info("Testing: get(TypeName): NE");
        final TypeName orderTypeName = newTypeNameBuilder()
                .withNamespace("demo::entities")
                .withName("Order")
                .build();


        final Optional<? extends NamespaceElement> orderNamespaceElement = modelAdapter.get(orderTypeName);
        assertTrue(orderNamespaceElement.isPresent());
        assertThat(orderNamespaceElement.get(), instanceOf(EntityType.class));
        assertThat(orderNamespaceElement.get().getName(), is("Order"));
        assertThat(getNamespaceFQName(PsmUtils.getNamespaceOfNamespaceElement(orderNamespaceElement.get()).get()), is("demo::entities"));

        final TypeName negtest_name_TypeName = newTypeNameBuilder()
                .withNamespace("demo::entities")
                .withName("negtest")
                .build();
        final Optional<? extends NamespaceElement> negtest_name_NamespaceElement = modelAdapter.get(negtest_name_TypeName);
        assertThat(negtest_name_NamespaceElement.isPresent(), is(Boolean.FALSE));

        //TODO: remove b\c not needed?
        final TypeName negtest_namespace_TypeName = newTypeNameBuilder()
                .withNamespace("demo::negtest")
                .withName("negtest")
                .build();
        assertTrue(modelAdapter.get(negtest_namespace_TypeName) == null);
    }

    @Test
    void testGetMeasureByMeasureName() {
        log.info("Testing: get(MeasureName): Opt<Measure>");
        final MeasureName measureName = newMeasureNameBuilder()
                .withNamespace("demo::measures")
                .withName("Mass")
                .build();
        final Optional<? extends Measure> measure = modelAdapter.get(measureName);

        assertThat(measure.isPresent(), is(Boolean.TRUE));
        assertThat(measure.get(), instanceOf(Measure.class));
        assertThat(measure.get().getName(), is("Mass"));
    }

    @Test
    void testIsObjectType() {
        log.info("Testing: isObjectType(NamespaceElement): boolean");

        EntityType entityType = newEntityTypeBuilder().withName("EntityType").build();
        StringType stringType = newStringTypeBuilder().withName("String").withMaxLength(-3).build();

        assertTrue(modelAdapter.isObjectType(entityType));
        assertFalse(modelAdapter.isObjectType(stringType));
    }


    @Test
    void testGetReference() {
        log.info("Testing: getReference(ET, String): Opt<RefTypEl>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Relation> productsRelation = entityType.get().getRelations().stream().filter(r -> "products".equals(r.getName())).findAny();

        assertTrue(entityType.isPresent());
        assertTrue(productsRelation.isPresent());
        assertThat(modelAdapter.getReference(entityType.get(), "products"), is(productsRelation));

        assertThat(modelAdapter.getReference(entityType.get(), "store"), is(Optional.empty()));
    }

    @Test
    void testGetTarget() {
        log.debug("Testing: getTarget(RTE): EntityType");
        Optional<EntityType> containerEntityType = getEntityTypeByName("Category");
        Optional<Relation> productsRelation = containerEntityType.get().getRelations().stream().filter(r -> "products".equals(r.getName())).findAny();
        Optional<EntityType> targetEntityType = getEntityTypeByName("Product");

        assertTrue(containerEntityType.isPresent());
        assertTrue(targetEntityType.isPresent());
        assertTrue(productsRelation.isPresent());
        assertThat(modelAdapter.getTarget(productsRelation.get()), is(targetEntityType.get()));
    }

    @Test
    void testGetAttribute() {
        log.debug("Testing: getAttribute(ET, attributeName): Opt<PTE>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<Attribute> categoryNameAttribute = entityType.get().getAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        assertTrue(entityType.isPresent());
        assertTrue(categoryNameAttribute.isPresent());
        assertThat(modelAdapter.getAttribute(entityType.get(), "categoryName"), is(categoryNameAttribute));
        assertThat(modelAdapter.getAttribute(entityType.get(), "productName"), is(Optional.empty()));
    }

    @Test
    void testGetAttributeType() {
        log.debug("Testing: getAttributeType(EntityType clazz, String attributeName): Optional<? extends Primitive>");

        Optional<EntityType> entityType = getEntityTypeByName("Category");
        Optional<PrimitiveTypedElement> primitiveTypedElement = getPrimitiveTypedElementByName("categoryName");

        assertTrue(entityType.isPresent());
        assertTrue(primitiveTypedElement.isPresent());
        assertThat(
                modelAdapter.getAttributeType(entityType.get(), "categoryName").get(), is(primitiveTypedElement.get().getDataType())
                //modelAdapter.getAttribute(entityType.get(), "categoryName").get().getDataType(), instanceOf(StringType.class) //TODO: ask what was this
        );
    }

    @Test
    void testGetSuperTypes() {
        log.debug("Testing: getSuperTypes(ET): Coll<? ext ET>");
        Optional<EntityType> childEntity = getEntityTypeByName("Company");
        Optional<EntityType> superEntity = getEntityTypeByName("Customer");

        assertTrue(superEntity.isPresent());
        assertTrue(childEntity.isPresent());
        assertTrue(modelAdapter.getSuperTypes(childEntity.get()).contains(superEntity.get()));

        Optional<EntityType> negtestEntity = getEntityTypeByName("Order");
        assertTrue(negtestEntity.isPresent());
        assertFalse(modelAdapter.getSuperTypes(childEntity.get()).contains(negtestEntity.get()));
        assertFalse(modelAdapter.getSuperTypes(negtestEntity.get()).contains(superEntity.get()));
    }

    @Test
    void testContains() {
        log.debug("Testing: boolean contains(EnumType, memberName)");

        Optional<EnumerationType> countries = getPsmElement(EnumerationType.class).filter(e -> "Countries".equals(e.getName())).findAny();
        assertTrue(countries.isPresent());
        assertTrue(modelAdapter.contains(countries.get(), "HU"));
        assertFalse(modelAdapter.contains(countries.get(), "MS"));

        Optional<EnumerationType> titles = getPsmElement(EnumerationType.class).filter(e -> "Titles".equals(e.getName())).findAny();
        assertTrue(titles.isPresent());
        assertFalse(modelAdapter.contains(titles.get(), "HU"));
    }

    @Test
    void testIsDurationSupportingAddition() {
        log.debug("Testing: boolean isDurSuppAdd(Unit)...");

        Optional<Measure> time = getMeasureByName("Time");
        Optional<Unit> day = time.get().getUnits().stream().filter(u -> "day".equals(u.getName())).findAny();
        Optional<Unit> microsecond = time.get().getUnits().stream().filter(u -> "microsecond".equals(u.getName())).findAny();

        assertTrue(time.isPresent());
        assertTrue(day.isPresent());
        assertTrue(modelAdapter.isDurationSupportingAddition(day.get()));
        assertTrue(microsecond.isPresent());
        assertFalse(modelAdapter.isDurationSupportingAddition(microsecond.get()));
    }

    //TODO: [MeasuredDecimal & MeasuredInteger].getMeasure()==null => return Optional.empty()?
    @Test
    void testGetUnit() {
        log.debug("Testing: Opt<Unit> getUnit(NumExpr)...");
        //--------------------------
        TypeName type = modelAdapter.buildTypeName(getEntityTypeByName("Product").get()).get();
        Instance instance = newInstanceBuilder().withElementName(type).build();

        Optional<Unit> kilogram = getMeasureElement(Unit.class).filter(u -> "kilogram".equals(u.getName())).findAny();
        NumericAttribute numericAttribute = newIntegerAttributeBuilder()
                .withAttributeName("weight")
                .withObjectExpression(
                        newObjectVariableReferenceBuilder()
                                .withVariable(instance)
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
                                .build()
                )
                .build();
        assertFalse(modelAdapter.getUnit(notFoundAttributeNumericAttribute).isPresent());
        assertThat(modelAdapter.getUnit(notFoundAttributeNumericAttribute), is(Optional.empty()));
        //--------------------------
        Optional<Unit> inch = getMeasureElement(Unit.class).filter(u -> "inch".equals(u.getName())).findAny();
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

    private Optional<EntityType> getEntityTypeByName(final String entityTypeName) {
        final Iterable<Notifier> psmContents = psmModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(psmContents.spliterator(), true)
                .filter(e -> EntityType.class.isAssignableFrom(e.getClass())).map(e -> (EntityType) e)
                .filter(entityType -> Objects.equals(entityType.getName(), entityTypeName))
                .findAny();
    }

    private Optional<Measure> getMeasureByName(final String measureName) {
        final Iterable<Notifier> psmContents = measureModel.getResourceSet()::getAllContents;
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
    
    <T> Stream<T> getMeasureElement(final Class<T> clazz) {
        final Iterable<Notifier> measureContents = measureModel.getResourceSet()::getAllContents;
        return StreamSupport.stream(measureContents.spliterator(), true)
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