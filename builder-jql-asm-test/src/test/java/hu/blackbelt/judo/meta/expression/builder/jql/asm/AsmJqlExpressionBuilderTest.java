package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EvlExecutionContext.evlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.buildAsmModel;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.ATTRIBUTE;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.RELATION;
import static hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport.SaveArguments.expressionSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.buildMeasureModel;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureTermBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEAttributeBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEClassBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEDataTypeBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEEnumBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEEnumLiteralBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEPackageBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.newEReferenceBuilder;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.useEPackage;
import static org.eclipse.emf.ecore.util.builder.EcoreBuilders.useEReference;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.exceptions.EvlScriptExecutionException;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmEpsilonValidator;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.AsmModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildException;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionSwitchExpression;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.constant.DateConstant;
import hu.blackbelt.judo.meta.expression.numeric.DecimalSwitchExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectFilterExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectSelectorExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectSwitchExpression;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.temporal.TimestampDifferenceExpression;
import hu.blackbelt.judo.meta.measure.BaseMeasure;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.DurationType;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureEpsilonValidator;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsmJqlExpressionBuilderTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private JqlExpressionBuilder<EClassifier, EDataType, EAttribute, EEnum, EClass, EClass, EReference, EClassifier, Measure, Unit> expressionBuilder;

    private AsmModelAdapter modelAdapter;
    private AsmUtils asmUtils;
    private ExpressionModelResourceSupport expressionModelResourceSupport;
    
    private AsmModel asmModel;
    private MeasureModel measureModel;
    private final String createdSourceModelName = "urn:asm.judo-meta-asm";
    
    private static final Logger logger = LoggerFactory.getLogger(AsmJqlExpressionBuilderTest.class);

    @BeforeEach
    void setUp() throws Exception {
    	
    	asmModel = buildAsmModel()
    			.uri(URI.createURI(createdSourceModelName))
                .name("demo")
                .build();
    	asmUtils = new AsmUtils(asmModel.getResourceSet());
    	populateAsmModel();
    	
    	expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();
    	
    	measureModel = buildMeasureModel()
                .name(asmModel.getName())
                .build();
    	populateMeasureModel();
        
  		log.info(asmModel.getDiagnosticsAsString());
  		log.info(measureModel.getDiagnosticsAsString());
    	assertTrue(asmModel.isValid());
    	assertTrue(measureModel.isValid());
    	runEpsilonOnMeasure();
    	runEpsilonOnAsm();
    	
    	modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
        expressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
    }

    @AfterEach
    void tearDown(final TestInfo testInfo) throws Exception {
        Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        expressionModelResourceSupport.saveExpression(expressionSaveArgumentsBuilder()
                .file(new File(TARGET_TEST_CLASSES, testInfo.getDisplayName().replace("(", "").replace(")", "") + "-expression.model"))
                .validateModel(false)
                .build());
        validateExpressions(expressionModelResourceSupport.getResource());

        modelAdapter = null;
        asmUtils = null;
        expressionModelResourceSupport = null;
    }
    
    private void populateAsmModel() {
    	//enum
    	EEnum countriesEnum = newEEnumBuilder().withName("Countries").withELiterals(newEEnumLiteralBuilder().withLiteral("HU").withName("HU").withValue(0).build(),
    			newEEnumLiteralBuilder().withLiteral("AT").withName("AT").withValue(1).build(),
    			newEEnumLiteralBuilder().withLiteral("RO").withName("RO").withValue(2).build(),
    			newEEnumLiteralBuilder().withLiteral("SK").withName("SK").withValue(3).build()).build();
    	
    	//types
    	EDataType timestamp = newEDataTypeBuilder().withName("Timestamp").withInstanceClassName("java.time.OffsetDateTime").build();
    	EDataType stringType = newEDataTypeBuilder().withName("String").withInstanceClassName("java.lang.String").build();
    	EDataType doubleType = newEDataTypeBuilder().withName("Double").withInstanceClassName("java.lang.Double").build();
    	EDataType integerType = newEDataTypeBuilder().withName("Integer").withInstanceClassName("java.lang.Integer").build();
    	EDataType binary = newEDataTypeBuilder().withName("Binary").withInstanceClassName("java.lang.Object").build();
    	EDataType timeStoredInMonths = newEDataTypeBuilder().withName("TimeStoredInMonths").withInstanceClassName("java.lang.Integer").build();
    	EDataType timeStoredInSeconds = newEDataTypeBuilder().withName("TimeStoredInSeconds").withInstanceClassName("java.lang.Double").build();
    	EDataType dateType = newEDataTypeBuilder().withName("Date").withInstanceClassName("java.time.LocalDate").build();
    	EDataType phoneType = newEDataTypeBuilder().withName("Phone").withInstanceClassName("java.lang.String").build();
    	EDataType booleanType = newEDataTypeBuilder().withName("Boolean").withInstanceClassName("java.lang.Boolean").build();
    	EDataType massStoredInKilograms = newEDataTypeBuilder().withName("MassStoredInKilograms").withInstanceClassName("java.lang.Double").build();
    	
    	//attributes
    	EAttribute orderDate = newEAttributeBuilder().withName("orderDate").withEType(timestamp).build();
    	EAttribute companyName = newEAttributeBuilder().withName("companyName").withEType(stringType).build();
    	EAttribute exciseTax = newEAttributeBuilder().withName("exciseTax").withEType(doubleType).build();
    	EAttribute customsDescription = newEAttributeBuilder().withName("customsDescription").withEType(stringType).build();
    	EAttribute productName = newEAttributeBuilder().withName("productName").withEType(stringType).build();
    	EAttribute unitPrice = newEAttributeBuilder().withName("unitPrice").withEType(doubleType).build();
    	EAttribute categoryName = newEAttributeBuilder().withName("categoryName").withEType(stringType).build();
    	EAttribute unitPriceOrderDetail = newEAttributeBuilder().withName("unitPrice").withEType(doubleType).build();
    	EAttribute quantity = newEAttributeBuilder().withName("quantity").withEType(integerType).build();
    	EAttribute discount = newEAttributeBuilder().withName("discount").withEType(doubleType).build();
    	EAttribute country = newEAttributeBuilder().withName("country").withEType(countriesEnum).build();
    	EAttribute picture = newEAttributeBuilder().withName("picture").withEType(binary).build();
    	EAttribute quantityPerUnit = newEAttributeBuilder().withName("quantityPerUnit").withEType(integerType).build();
    	EAttribute firstName = newEAttributeBuilder().withName("firstName").withEType(stringType).build();
    	EAttribute firstNameEmployee = newEAttributeBuilder().withName("firstName").withEType(stringType).build();
    	EAttribute lastNameEmployee = newEAttributeBuilder().withName("lastName").withEType(stringType).build();
    	EAttribute phone = newEAttributeBuilder().withName("phone").withEType(phoneType).build();
    	EAttribute discounted = newEAttributeBuilder().withName("discounted").withEType(booleanType).build();
    	EAttribute weight = newEAttributeBuilder().withName("weight").withEType(massStoredInKilograms).build();
    	EAttribute freight = newEAttributeBuilder().withName("freight").withEType(doubleType).build();
    	EAttribute postalCode = newEAttributeBuilder().withName("postalCode").withEType(phoneType).build();
    	
    	//relations
    	EReference orderDetails = newEReferenceBuilder().withName("orderDetails").withContainment(true).withLowerBound(0).withUpperBound(-1).build();
    	EReference productRef = newEReferenceBuilder().withName("product").withLowerBound(1).withUpperBound(1).build();
    	EReference categoryRef = newEReferenceBuilder().withName("category").withLowerBound(1).withUpperBound(1).build();
    	EReference productsRef = newEReferenceBuilder().withName("products").withLowerBound(0).withUpperBound(-1).build();
    	EReference categories = newEReferenceBuilder().withName("categories").withLowerBound(0).withUpperBound(-1).build();
    	EReference ordersRef = newEReferenceBuilder().withName("orders").withLowerBound(0).withUpperBound(-1).build();
    	EReference employeeRef = newEReferenceBuilder().withName("employee").withLowerBound(0).withUpperBound(1).build();
    	EReference shipperOrdersRef = newEReferenceBuilder().withName("shipperOrders").withLowerBound(0).withUpperBound(-1).build();
    	EReference shipperRef = newEReferenceBuilder().withName("shipper").withLowerBound(0).withUpperBound(1).build();
    	EReference ordersCustomer = newEReferenceBuilder().withName("orders").withLowerBound(0).withUpperBound(-1).build();
    	EReference addressesCustomer = newEReferenceBuilder().withName("addresses").withLowerBound(0).withUpperBound(-1)
    			.withContainment(true).build();
    	EReference customerOrder = newEReferenceBuilder().withName("customer").withLowerBound(0).withUpperBound(1).build();
    	EReference owner = newEReferenceBuilder().withName("owner").withLowerBound(0).withUpperBound(1).build();
    	EReference categoryEmployee = newEReferenceBuilder().withName("category").withLowerBound(0).withUpperBound(-1).build();
    	EReference territoryRef = newEReferenceBuilder().withName("territory").withLowerBound(0).withUpperBound(1).build();
    	EReference shipperTerritory = newEReferenceBuilder().withName("shipper").withLowerBound(0).withUpperBound(1).build();
    	EReference shipAddress = newEReferenceBuilder().withName("shipAddress").withLowerBound(0).withUpperBound(1).build();
    	
    	//classes
    	EClass order = newEClassBuilder().withName("Order").withEStructuralFeatures(orderDate,orderDetails,categories,employeeRef,shipperRef,customerOrder,shipAddress).build();
    	EClass orderDetail = newEClassBuilder().withName("OrderDetail").withEStructuralFeatures(productRef,unitPriceOrderDetail,quantity,discount).build();
    	EClass product = newEClassBuilder().withName("Product").withEStructuralFeatures(categoryRef,productName,unitPrice,quantityPerUnit,discounted,weight).build();
    	EClass category = newEClassBuilder().withName("Category").withEStructuralFeatures(productsRef,categoryName,picture,owner).build();
    	EClass employee = newEClassBuilder().withName("Employee").withEStructuralFeatures(ordersRef,categoryEmployee,firstNameEmployee,lastNameEmployee).build();
    	EClass internationalOrder = newEClassBuilder().withName("InternationalOrder").withEStructuralFeatures(exciseTax,customsDescription,freight)
    			.withESuperTypes(order).build();
    	EClass customer = newEClassBuilder().withName("Customer").withEStructuralFeatures(ordersCustomer,addressesCustomer).build();
    	EClass address = newEClassBuilder().withName("Address").withEStructuralFeatures(postalCode).build();
    	EClass internationalAddress = newEClassBuilder().withName("InternationalAddress")
    			.withESuperTypes(address).withEStructuralFeatures(country).build();
    	EClass company = newEClassBuilder().withName("Company").withESuperTypes(customer).build();
    	EClass shipper = newEClassBuilder().withName("Shipper").withEStructuralFeatures(companyName,shipperOrdersRef,phone,territoryRef)
    			.withESuperTypes(company).build();
    	EClass onlineOrder = newEClassBuilder().withName("OnlineOrder")
    			.withESuperTypes(order).build();
    	EClass individual = newEClassBuilder().withName("Individual").withEStructuralFeatures(firstName)
    			.withESuperTypes(customer).build();
    	EClass supplier = newEClassBuilder().withName("Supplier")
    			.withESuperTypes(company).build();
    	EClass territory = newEClassBuilder().withName("Territory").withEStructuralFeatures(shipperTerritory).build();
    	
    	//relations again
    	useEReference(orderDetails).withEType(orderDetail).build();
    	useEReference(productRef).withEType(product).build();
    	useEReference(categoryRef).withEType(category).withEOpposite(productsRef).build();
    	useEReference(productsRef).withEType(product).withEOpposite(categoryRef).build();
    	useEReference(categories).withEType(category).build();
    	useEReference(ordersRef).withEType(order).withEOpposite(employeeRef).build();
    	useEReference(employeeRef).withEType(employee).withEOpposite(ordersRef).build();
    	useEReference(shipperOrdersRef).withEType(order).withEOpposite(shipperRef).build();
    	useEReference(shipperRef).withEType(shipper).withEOpposite(shipperOrdersRef).build();
    	useEReference(addressesCustomer).withEType(address).build();
    	useEReference(ordersCustomer).withEType(order).withEOpposite(customerOrder).build();
    	useEReference(customerOrder).withEType(customer).withEOpposite(ordersCustomer).build();
    	useEReference(owner).withEType(employee).withEOpposite(categoryEmployee).build();
    	useEReference(categoryEmployee).withEType(category).withEOpposite(owner).build();
    	useEReference(shipperTerritory).withEType(shipper).withEOpposite(territoryRef).build();
    	useEReference(territoryRef).withEType(territory).withEOpposite(shipperTerritory).build();
    	useEReference(shipAddress).withEType(address).build();
    	
    	//packages
    	EPackage demo = newEPackageBuilder().withName("demo").withNsURI("http://blackbelt.hu/judo/northwind/northwind/demo")
    			.withNsPrefix("runtimenorthwindNorthwindDemo").build();
    	EPackage services = newEPackageBuilder().withName("services").withNsURI("http://blackbelt.hu/judo/northwind/northwind/services")
    			.withNsPrefix("runtimenorthwindNorthwindServices").build();
    	EPackage entities = newEPackageBuilder().withName("entities")
    			.withEClassifiers(order,
						orderDetail,product,category,employee,
						shipper,internationalOrder,customer,address,
						internationalAddress,company,onlineOrder,individual,supplier,territory)
    			.withNsURI("http://blackbelt.hu/judo/northwind/northwind/entities")
    			.withNsPrefix("runtimenorthwindNorthwindEntities").build();
    	EPackage types = newEPackageBuilder().withName("types")
    			.withEClassifiers(timestamp,stringType,doubleType,integerType,binary,dateType,countriesEnum,phoneType,booleanType)
    			.withNsURI("http://blackbelt.hu/judo/northwind/northwind/types")
    			.withNsPrefix("runtimenorthwindNorthwindTypes").build();
    	EPackage measured = newEPackageBuilder().withName("measured").withEClassifiers(timeStoredInMonths,timeStoredInSeconds,massStoredInKilograms)
    			.withNsURI("http://blackbelt.hu/judo/northwind/demo/types/measured")
    			.withNsPrefix("runtimenorthwindDemoTypesMeasured").build();
    	EPackage measures = newEPackageBuilder().withName("measures")
    			.withNsURI("http://blackbelt.hu/judo/northwind/demo/measures")
    			.withNsPrefix("runtimenorthwindDemoMeasures").build();
    	
    	//packages again
    	useEPackage(demo).withESubpackages(services,entities,types,measures).build();
    	useEPackage(types).withESubpackages(measured).build();
    	
    	asmModel.addContent(demo);
        
        //annotations
        EAnnotation orderAnnotation = asmUtils.getExtensionAnnotationByName(order, "entity", true).get();
        orderAnnotation.getDetails().put("value", "true");
    	EAnnotation orderDetailAnnotation = asmUtils.getExtensionAnnotationByName(orderDetail, "entity", true).get();
    	orderDetailAnnotation.getDetails().put("value", "true");
    	EAnnotation productAnnotation = asmUtils.getExtensionAnnotationByName(product, "entity", true).get();
    	productAnnotation.getDetails().put("value", "true");
    	EAnnotation categoryAnnotation = asmUtils.getExtensionAnnotationByName(category, "entity", true).get();
    	categoryAnnotation.getDetails().put("value", "true");
    	EAnnotation employeeAnnotation = asmUtils.getExtensionAnnotationByName(employee, "entity", true).get();
    	employeeAnnotation.getDetails().put("value", "true");
    	EAnnotation shipperAnnotation = asmUtils.getExtensionAnnotationByName(shipper, "entity", true).get();
    	shipperAnnotation.getDetails().put("value", "true");
    	EAnnotation intOrderAnnotation = asmUtils.getExtensionAnnotationByName(internationalOrder, "entity", true).get();
    	intOrderAnnotation.getDetails().put("value", "true");
    	EAnnotation addressAnnotation = asmUtils.getExtensionAnnotationByName(address, "entity", true).get();
    	addressAnnotation.getDetails().put("value", "true");
    	EAnnotation customerAnnotation = asmUtils.getExtensionAnnotationByName(customer, "entity", true).get();
    	customerAnnotation.getDetails().put("value", "true");
    	EAnnotation intAddrAnnotation = asmUtils.getExtensionAnnotationByName(internationalAddress, "entity", true).get();
    	intAddrAnnotation.getDetails().put("value", "true");
    	EAnnotation companyAnnotation = asmUtils.getExtensionAnnotationByName(company, "entity", true).get();
    	companyAnnotation.getDetails().put("value", "true");
    	EAnnotation onlineOrderAnnotation = asmUtils.getExtensionAnnotationByName(onlineOrder, "entity", true).get();
    	onlineOrderAnnotation.getDetails().put("value", "true");
    	EAnnotation individaulAnnotation = asmUtils.getExtensionAnnotationByName(individual, "entity", true).get();
    	individaulAnnotation.getDetails().put("value", "true");
    	EAnnotation supplierAnnotation = asmUtils.getExtensionAnnotationByName(supplier, "entity", true).get();
    	supplierAnnotation.getDetails().put("value", "true");
    	EAnnotation territoryAnnotation = asmUtils.getExtensionAnnotationByName(territory, "entity", true).get();
    	territoryAnnotation.getDetails().put("value", "true");
    	EAnnotation weightAnnotation = asmUtils.getExtensionAnnotationByName(weight, "constraints", true).get();
    	weightAnnotation.getDetails().put("precision", "15");
    	weightAnnotation.getDetails().put("scale", "4");
    	weightAnnotation.getDetails().put("measure", "demo.measures.Mass");
    	weightAnnotation.getDetails().put("unit", "kilogram");
    }
    
    private void populateMeasureModel() {
        
        BaseMeasure time = newBaseMeasureBuilder().withName("Time").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("millisecond").withSymbol("ms").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.MILLISECOND).build(),
    			newDurationUnitBuilder().withName("second").withSymbol("s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.SECOND).build(),
    			newDurationUnitBuilder().withName("minute").withSymbol("min").withRateDividend(new BigDecimal(60.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("hour").withSymbol("h").withRateDividend(new BigDecimal(3600.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.HOUR).build(),
    			newDurationUnitBuilder().withName("day").withSymbol("").withRateDividend(new BigDecimal(86400.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.DAY).build(),
    			newDurationUnitBuilder().withName("week").withSymbol("").withRateDividend(new BigDecimal(604800.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.WEEK).build(),
    			newDurationUnitBuilder().withName("halfDay").withSymbol("").withRateDividend(new BigDecimal(43200.0)).withRateDivisor(new BigDecimal(1.0)).build())
        	.build();
        
        BaseMeasure mass = newBaseMeasureBuilder().withName("Mass").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("milligram").withSymbol("mg").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("gram").withSymbol("g").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("dekagram").withSymbol("dkg").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("kilogram").withSymbol("kg").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("quintal").withSymbol("q").withRateDividend(new BigDecimal(100.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("tonne").withSymbol("t").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        BaseMeasure length = newBaseMeasureBuilder().withName("Length").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("nanometre").withSymbol("nm").withRateDividend(new BigDecimal(1.0E-9)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("micrometre").withSymbol("μm").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("millimetre").withSymbol("mm").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("centimetre").withSymbol("cm").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("decimetre").withSymbol("dm").withRateDividend(new BigDecimal(0.1)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("metre").withSymbol("m").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("kilometre").withSymbol("km").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("inch").withSymbol("&quot;").withRateDividend(new BigDecimal(0.0254)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("foot").withSymbol("ft").withRateDividend(new BigDecimal(0.3048)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("mile").withSymbol("mi").withRateDividend(new BigDecimal(1609.344)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        DerivedMeasure velocity = newDerivedMeasureBuilder().withName("Velocity").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("kilometrePerHour").withSymbol("km/h").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(3.6)).build(),
    			newDurationUnitBuilder().withName("metrePerSecond").withSymbol("m/s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(length).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(-1).withBaseMeasure(time).build())
    		.build();
        
        measureModel.addContent(time);
        measureModel.addContent(mass);
        measureModel.addContent(length);
        measureModel.addContent(velocity);
    }

    private void validateExpressions(final Resource expressionResource) throws Exception {
        final List<ModelContext> modelContexts = Arrays.asList(
                wrappedEmfModelContextBuilder()
                        .log(new Slf4jLog(log))
                        .name("NORTHWIND")
                        .validateModel(false)
                        .aliases(ImmutableList.of("ASM"))
                        .resource(asmModel.getResource())
                        .build(),
                wrappedEmfModelContextBuilder()
                        .log(new Slf4jLog(log))
                        .name("NORTHWIND_MEASURES")
                        .validateModel(false)
                        .aliases(ImmutableList.of("MEASURES"))
                        .resource(measureModel.getResource())
                        .build(),
                wrappedEmfModelContextBuilder()
                        .log(new Slf4jLog(log))
                        .name("TEST")
                        .validateModel(false)
                        .aliases(ImmutableList.of("EXPR"))
                        .resource(expressionResource)
                        .build()
        );

        ExecutionContext executionContext = executionContextBuilder()
                .log(new Slf4jLog(log))
                .resourceSet(expressionResource.getResourceSet())
                .metaModels(Collections.emptyList())
                .modelContexts(modelContexts)
                .injectContexts(ImmutableMap.of(
                        "evaluator", new ExpressionEvaluator(),
                        "modelAdapter", modelAdapter))
                .build();

        // run the model / metadata loading
        executionContext.load();

        // Validation script
        executionContext.executeProgram(
                evlExecutionContextBuilder()
                        .source(new File("../model/src/main/epsilon/validations/expression.evl").toURI())
                        .expectedErrors(Collections.emptyList())
                        .expectedWarnings(Collections.emptyList())
                        .build());

        executionContext.commit();
        executionContext.close();
    }

    private Expression createExpression(String jqlExpressionAsString) {
        return createExpression(null, jqlExpressionAsString);
    }

    private Expression createExpression(final EClass clazz, final String jqlExpressionString) {
        final Expression expression = expressionBuilder.createExpression(clazz, jqlExpressionString);
        assertThat(expression, notNullValue());
        return expression;
    }

    private Matcher<Expression> collectionOf(String typeName) {
        return new DiagnosingMatcher<Expression>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("A collection of type with name: ").appendText(typeName);
            }

            @Override
            protected boolean matches(Object item, Description mismatchDescription) {
                boolean result = false;
                if (item instanceof CollectionExpression) {
                    CollectionExpression collection = (CollectionExpression) item;
                    TypeName collectionTypeName = modelAdapter.buildTypeName((EClassifier) (collection).getObjectType(modelAdapter)).get();
                    if (collectionTypeName.getName().equals(typeName)) {
                        result = true;
                    } else {
                        mismatchDescription.appendValue(collectionTypeName.getName());
                    }
                } else {
                    mismatchDescription.appendText("Not a collection: ").appendValue(item);
                }
                return result;
            }
        };
    }

    private Matcher<Expression> objectOf(String typeName) {
        return new DiagnosingMatcher<Expression>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("An object of type with name: ").appendText(typeName);
            }

            @Override
            protected boolean matches(Object item, Description mismatchDescription) {
                boolean result = false;
                if (item instanceof ObjectExpression) {
                    ObjectExpression oe = (ObjectExpression) item;
                    TypeName objectTypeName = modelAdapter.buildTypeName((EClassifier) (oe).getObjectType(modelAdapter)).get();
                    if (objectTypeName.getName().equals(typeName)) {
                        result = true;
                    } else {
                        mismatchDescription.appendValue(objectTypeName.getName());
                    }
                } else {
                    mismatchDescription.appendText("Not an object: ").appendValue(item);
                }
                return result;
            }

        };
    }

    private EClass findBase(String entityName) {
        return asmUtils.all(EClass.class).filter(c -> entityName.equals(c.getName())).findAny().orElseThrow(IllegalArgumentException::new);
    }
    
    private void runEpsilonOnMeasure() throws Exception {
        try {
            MeasureEpsilonValidator.validateMeasure(new Slf4jLog(),
            		measureModel,
                    MeasureEpsilonValidator.calculateMeasureValidationScriptURI(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            throw ex;
        }
    }
    
    private void runEpsilonOnAsm() throws Exception {
        try {
            AsmEpsilonValidator.validateAsm(new Slf4jLog(),
            		asmModel,
            		AsmEpsilonValidator.calculateAsmValidationScriptURI(),
            		Collections.emptyList(),
            		Collections.emptyList());
        } catch (EvlScriptExecutionException ex) {
            logger.error("EVL failed", ex);
            logger.error("\u001B[31m - unexpected errors: {}\u001B[0m", ex.getUnexpectedErrors());
            logger.error("\u001B[33m - unexpected warnings: {}\u001B[0m", ex.getUnexpectedWarnings());
            throw ex;
        }
    }

    @Test
    void testOrderDate() {
        final EClass order = asmUtils.all(EClass.class).filter(c -> "Order".equals(c.getName())).findAny().get();
        final Expression expression = createExpression(order, "self.orderDate");
        assertNotNull(expression);

        expressionBuilder.createGetterBinding(order, expression, "orderDate", ATTRIBUTE);
        log.info("Order date: " + expression);
    }

    @Test
    void testOrderCategories() {
        EClass order = findBase("Order");
        ReferenceExpression expression = (ReferenceExpression)createExpression(order, "self.orderDetails.product.category");
        assertNotNull(expression);
        expressionBuilder.createGetterBinding(order, expression, "categories", RELATION);
        assertThat(expression, collectionOf("Category"));
    }

    @Test
    void testSimpleArithmeticOperation() {
        final Expression expression = createExpression("2*3");
        log.info("Simple arithmetic operation: " + expression);
        assertNotNull(expression);
    }

    @Test
    void testStaticExpressions() {
        Expression allProducts = createExpression("demo::entities::Product");
        assertThat(allProducts, instanceOf(CollectionExpression.class));
        allProducts = createExpression("(demo::entities::Product)");
        assertThat(allProducts, instanceOf(CollectionExpression.class));
        Expression allOrdersCount = createExpression("demo::entities::Order!count()");
        assertThat(allOrdersCount, instanceOf(IntegerExpression.class));
        Expression allEmployeeOrders = createExpression("demo::entities::Employee=>orders");
        assertThat(allEmployeeOrders, instanceOf(CollectionExpression.class));
        assertThat(allEmployeeOrders, collectionOf("Order"));
        Expression allProductsSorted = createExpression("demo::entities::Product!sort()");
        assertThat(allProductsSorted, instanceOf(SortExpression.class));
    }

    @Test
    void testSimpleDaoTest() {
        EClass order = findBase("Order");
        createExpression(order, "self.orderDetails");
        Expression orderCategories = createExpression(order, "self.orderDetails.product.category");
        assertThat(orderCategories, collectionOf("Category"));
        orderCategories = createExpression(order, "((((self).orderDetails).product).category)");
        assertThat(orderCategories, collectionOf("Category"));

        createExpression(order, "self.shipper.companyName");
        createExpression(order, "(self).shipper.companyName");
        createExpression(order, "self.shipper");
        createExpression(order, "(self.shipper)");
        createExpression(order, "self.orderDate");

        EClass internationalOrder = findBase("InternationalOrder");
        createExpression(internationalOrder, "self.customsDescription");
        createExpression(internationalOrder, "self.exciseTax");

        EClass product = findBase("Product");
        createExpression(product, "self.category");
        createExpression(product, "self.productName");
        createExpression(product, "self.unitPrice");

        EClass shipper = findBase("Shipper");
        createExpression(shipper, "self.companyName");

        EClass category = findBase("Category");
        createExpression(category, "self.products");
        createExpression(category, "self.categoryName");

        EClass orderDetail = findBase("OrderDetail");
        createExpression(orderDetail, "self.product");
        createExpression(orderDetail, "self.product.category");
        createExpression(orderDetail, "self.product.productName");
        createExpression(orderDetail, "self.unitPrice");
        createExpression(orderDetail, "self.quantity");
        createExpression(orderDetail, "self.discount");
        createExpression(orderDetail, "self.quantity * self.unitPrice * (1 - self.discount)");

    }

    @Test
    void testCast() {
        EClass customer = findBase("Customer");
        createExpression(customer, "self=>orders!asCollection(demo::entities::InternationalOrder)");
        createExpression(customer, "self=>addresses!sort()!head()!asType(demo::entities::InternationalAddress).country");

        EClass order = findBase("Order");
        createExpression(order, "self=>customer!asType(demo::entities::Company)");
    }

    @Test
    void testContainer() {
        EClass address = findBase("Address");
        createExpression(address, "self!container(demo::entities::Customer)");
    }

    @Test
    void testConstants() {
        createExpression("1");
        createExpression("1.2");
        createExpression("true");
        createExpression("false");
        createExpression("'a'");
        createExpression("\"\"");
        createExpression("\"b\"");
        DateConstant dateConstant = (DateConstant) createExpression("`2019-12-31`");
        createExpression("`2019-12-31T13:52:03 Europe/Budapest`");
        createExpression("demo::entities::Category!sort()!head().picture");
    }

    @Test
    void testDateOperations() {
        createExpression("`2019-12-31`!elapsedTimeFrom(`2020-01-01`)");
        createExpression("`2019-12-31` < `2020-01-01`");
        createExpression("`2019-12-31` > `2020-01-01`");
        createExpression("`2019-12-31` == `2020-01-01`");
        createExpression("`2019-12-31` != `2020-01-01`");
        createExpression("`2019-12-31` <= `2020-01-01`");
        createExpression("`2019-12-31` >= `2020-01-01`");
        assertThrows(UnsupportedOperationException.class, () -> createExpression("`2019-12-31` + 1"));
        createExpression("`2019-12-31` + 1[day]");
        createExpression("1[day] + `2019-12-31`");
        assertThrows(IllegalArgumentException.class, () -> createExpression("1[day] - `2019-12-31`"));
    }

    @Test
    void testTimestampOperations() {
        EClass orderType = asmUtils.getClassByFQName("demo.entities.Order").get();
        Expression expression = createExpression(orderType, "self.orderDate + 10.5[day]");
        assertThat(expression, instanceOf(TimestampExpression.class));
        TimestampDifferenceExpression elapsedTimeFrom = (TimestampDifferenceExpression) createExpression("`2019-12-31T00:00:00.000+0100`!elapsedTimeFrom(`2019-12-31T00:00:00.000+0200`)");
        assertThat(elapsedTimeFrom.getMeasure(), notNullValue());
        createExpression("`2019-12-31T00:00:00.000+0100` > `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` < `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` == `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` != `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` <= `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` >= `2019-12-31T00:00:00.000+0200`");
        createExpression("`2019-12-31T00:00:00.000+0100` + 1[week]");
        createExpression("`2019-12-31T00:00:00.000+0100` - 1[demo::measures::Time#day]");
        assertThrows(UnsupportedOperationException.class, () -> createExpression("`2019-12-31T00:00:00.000+0100` + 1"));
        assertThrows(UnsupportedOperationException.class, () -> createExpression("`2019-12-31T00:00:00.000+0100` - 1"));
        assertThrows(IllegalArgumentException.class, () -> createExpression("1[day] - `2019-12-31T00:00:00.000+0100`"));
    }

    @Test
    void testDecimalOperations() {
        createExpression("1.0 < 3.14");
        createExpression("1.0 > 3.14");
        createExpression("1.0 <= 3.14");
        createExpression("1.0 >= 3.14");
        createExpression("1.0 == 3.14");
        createExpression("1.0 != 3.14");
        createExpression("1.0 + 3.14");
        createExpression("1.0 - 3.14");
        createExpression("1.0 * 3.14");
        createExpression("1.0 / 3.14");
        createExpression("1 < 3.14");
        Expression decimalDivision = createExpression("1 / 2");
        assertThat(decimalDivision, instanceOf(DecimalExpression.class));
        assertThrows(UnsupportedOperationException.class, () -> createExpression("1.0 mod 2"));
        assertThrows(UnsupportedOperationException.class, () -> createExpression("1.0 div 2"));
    }

    @Test
    void testTernaryOperation() {
        createExpression("true ? 1 : 2");
        createExpression("true ? 1 : 2 + 3");
        createExpression("true ? 1.0 : 2.0 + 3");
        Expression decimalSwitch1 = createExpression("true ? 1 : 2.0");
        assertThat(decimalSwitch1, instanceOf(DecimalSwitchExpression.class));
        Expression decimalSwitch2 = createExpression("true ? 1.0 : 2");
        assertThat(decimalSwitch2, instanceOf(DecimalSwitchExpression.class));

        Expression stringSwitch = createExpression("false ? demo::entities::Order!any().shipper.companyName : 'b'");
        assertThat(stringSwitch, instanceOf(StringExpression.class));

        Expression sametype = createExpression("true ? demo::entities::Order : demo::entities::Order");
        assertThat(((CollectionSwitchExpression) sametype).getElementName().getName(), is("Order"));

        Expression ancestor = createExpression("true ? demo::entities::OnlineOrder : demo::entities::Order");
        assertThat(((CollectionSwitchExpression) ancestor).getElementName().getName(), is("Order"));

        Expression expression = createExpression("true ? demo::entities::OnlineOrder : demo::entities::InternationalOrder");
        assertThat(((CollectionSwitchExpression) expression).getElementName().getName(), is("Order"));

        Expression customerAncestor = createExpression("true ? demo::entities::Individual : demo::entities::Shipper");
        assertThat(((CollectionSwitchExpression) customerAncestor).getElementName().getName(), is("Customer"));

        Expression companyAncestor = createExpression("true ? demo::entities::Supplier : demo::entities::Shipper");
        assertThat(((CollectionSwitchExpression) companyAncestor).getElementName().getName(), is("Company"));

        assertThrows(IllegalArgumentException.class, () -> createExpression("true ? demo::entities::Product : demo::entities::Order"));

        createExpression("true ? demo::entities::Category!sort()!head().picture : demo::entities::Product!sort()!head()->category.picture");
        createExpression("true ? demo::types::Countries#AT : demo::types::Countries#RO");

        Expression objectExpression = createExpression("true ? demo::entities::Category!sort()!head() : demo::entities::Product!sort()!head()->category");
        assertThat(objectExpression, instanceOf(ObjectSwitchExpression.class));
        assertThat(((ObjectSwitchExpression) objectExpression).getElementName().getName(), is("Category"));

        createExpression("true ? 'a' : demo::entities::Category!sort()!head().categoryName");
        createExpression("true ? `2019-10-12` : `2019-10-23`");
        createExpression("true ? `2019-10-12T` : `2019-10-23T`");
    }

    @Test
    void testCollectionVariableNames() {
        Expression productListOrdered = createExpression("demo::entities::Category=>products!sort(e | e.unitPrice ASC, e.productName DESC)!sort(p | p.unitPrice)!join(s | s.productName, ',')");
        assertThat(productListOrdered, instanceOf(StringExpression.class));
    }

    @Test
    void testCollectionOperations() {
        EClass category = findBase("Category");
        Expression products = createExpression(category, "self.products");
        assertThat(products, collectionOf("Product"));

        createExpression(category, "self=>products!count()");
        createExpression(category, "self=>products!join(p1 | p1.productName, ', ')!length()");
        createExpression(category, "self=>products!sort(e | e.unitPrice)!head(5)");
        Expression productListTail = createExpression(category, "self=>products!sort(e | e.unitPrice)!tail(5)");
        assertThat(productListTail, collectionOf("Product"));
        Expression cheapProducts = createExpression(category, "self=>products!filter(p | p.unitPrice < 2.0)");
        assertThat(cheapProducts, collectionOf("Product"));
        createExpression(category, "self=>products!filter(p | p.unitPrice < 2.0)!count()");

        createExpression(category, "self=>products!min(p | p.unitPrice)");
        createExpression(category, "self=>products!max(p | p.unitPrice)");
        createExpression(category, "self=>products!sum(p | p.unitPrice)");
        createExpression(category, "self=>products!avg(p | p.unitPrice)");
        createExpression(category, "self=>products!min(p | p.quantityPerUnit)");
        createExpression(category, "self=>products!max(p | p.quantityPerUnit)");
        createExpression(category, "self=>products!sum(p | p.quantityPerUnit)");
        createExpression(category, "self=>products!avg(p | p.quantityPerUnit)");
        createExpression(category, "self=>products!filter(p | p.unitPrice < 2.0)!avg(p | p.unitPrice)");

        Expression productHead = createExpression(category, "self=>products!sort(p | p.unitPrice)!head()");
        assertThat(productHead, objectOf("Product"));
        createExpression(category, "self=>products!sort(p | p.unitPrice)!tail()");

        Expression containsProduct = createExpression(category, "self=>products!contains(self=>products!sort(p | p.unitPrice)!tail())");
        assertThat(containsProduct, instanceOf(LogicalExpression.class));
        createExpression(category, "self=>products!sort(p | p.unitPrice)!tail()!memberOf(self=>products)");
    }

    @Test
    void testCustomAttributes() {
        EClass category = findBase("Category");
    }

    @Test
    void testParenNavigation() {
        EClass order = findBase("Order");
        Expression expression = createExpression(order, "(self.shipper).companyName");
        System.out.println(expression);
    }

    @Test
    void testStringOperations() {
        EClass category = findBase("Category");
        createExpression(category, "self.categoryName < 'c'");
        createExpression(category, "self.categoryName > 'c'");
        createExpression(category, "self.categoryName <= 'c'");
        createExpression(category, "self.categoryName >= 'c'");
        createExpression(category, "self.categoryName == 'c'");
        createExpression(category, "self.categoryName != 'c'");

        // Concatenate
        createExpression("'a'+'b'");
        createExpression("demo::entities::Order!any().shipper.companyName + demo::entities::Order!any().shipper.companyName");
        createExpression("'_' + demo::entities::Order!any().shipper.companyName");
    }

    @Test
    void testLambdaScoping() {
        EClass category = findBase("Category");
        createExpression(category, "self=>products!sort(p | p.unitPrice)!head() == self=>products!sort(p | p.unitPrice)!tail()");
        Expression qExpression = createExpression(category, "self=>products!sort(p | p.unitPrice)!head() == self=>owner=>orders!sort(q | p.unitPrice)!tail()");
        // TODO handle scope change on different side of binary operations (and other expressions probably)
        //assertThrows(IllegalStateException.class, () -> createExpression(category, "self=>products!sort(p | p.unitPrice)!head() = self=>products!sort(q | p.unitPrice)!tail()"));
    }

    @Test
    void testObjectOperations() {
        EClass category = findBase("Category");
        createExpression(category, "demo::entities::Category!any()=>products!sort(p | p.unitPrice)!head() == p.category=>products!sort(q | q.unitPrice)!tail()");
        createExpression(category, "demo::entities::Category!any()=>products!sort(p | p.unitPrice)!head() != p.category=>products!sort(p | p.unitPrice)!tail()");

        createExpression("not demo::entities::OrderDetail!any().product!kindof(demo::entities::Product)");
        createExpression("demo::entities::OrderDetail!any().product!typeof(demo::entities::Product)");

        EClass order = findBase("Order");
        createExpression(order, "self->customer!asType(demo::entities::Individual)!filter(c | c.firstName == 'joe')");
        ObjectFilterExpression objectFilter = (ObjectFilterExpression) createExpression(order, "self.shipper!filter(s | s.companyName == 'DHL')");
        assertThat(objectFilter.getVariableName(), is("s"));

    }

    @Test
    void testChainedObjectFilter() {
        EClass order = findBase("Order");
        ObjectFilterExpression chainedObjectFilter = (ObjectFilterExpression) createExpression(order, "self.shipper!filter(s | s.companyName == 'DHL')!filter(c | c.phone!isDefined()).territory!filter(t | t.shipper.phone == c.phone)");
    }

    @Test
    void testUnaryOperations() {
        createExpression("-1");
        createExpression("-1.0");
        createExpression("not true");
    }

    @Test
    void testEnums() {
        EClass order = findBase("Order");
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(order, "self.shipAddress.country == Countries#AT"));

        Expression expression = createExpression("1 < 2 ? demo::types::Countries#AT : demo::types::Countries#RO");
        createExpression("demo::types::Countries#AT == demo::types::Countries#RO");

    }

    @Test
    void testEnumTypeDifference() {
        assertThrows(IllegalArgumentException.class, () -> createExpression("true ? demo::types::Countries#AT : demo::types::Titles#MR"));
        assertThrows(IllegalArgumentException.class, () -> createExpression("demo::types::Countries#AT == demo::types::Titles#MR"));
    }

    @Test
    void testMeasures() {
        createExpression("5[demo::measures::Time#min]");
        createExpression("1.23[min]");

        Expression measuredIntDiv = createExpression("2[min] div 1");
        assertTrue(modelAdapter.isMeasured((NumericExpression) measuredIntDiv));
        Expression notMeasuredDecimalRatio = createExpression("2.0[min] / 1[min]");
        assertFalse(modelAdapter.isMeasured((NumericExpression) notMeasuredDecimalRatio));
        Expression measuredDecimalRatio = createExpression("2.0[demo::measures::Velocity#m/s] / 1[s]");
        assertTrue(modelAdapter.isMeasured((NumericExpression) measuredDecimalRatio));
    }

    @Test
    void testNumericFunctions() {
        Expression roundedConstant = createExpression("1.2!round()");
        assertThat(roundedConstant, instanceOf(IntegerExpression.class));
        Expression roundedAttribute = createExpression("(demo::entities::Product!any().unitPrice)!round()");
        assertThat(roundedAttribute, instanceOf(IntegerExpression.class));
    }

    @Test
    void testSelectorFunctions() {
        EClass category = findBase("Category");
        Expression expression = createExpression("demo::entities::Product!sort()");
        assertThat(expression, instanceOf(SortExpression.class));
        expression = createExpression("demo::entities::Product!sort()!head()");
        assertThat(expression, instanceOf(ObjectSelectorExpression.class));
        expression = createExpression("demo::entities::Product!sort()!head().weight");
        assertThat(expression, instanceOf(AttributeSelector.class));
        expression = createExpression(category, "self.products!sort()!head().weight");
        assertThat(expression, instanceOf(AttributeSelector.class));
    }

    @Test
    void testStringFunctions() {
        EClass order = findBase("Order");

        // LowerCase
        createExpression(order, "self.shipper.companyName!lowerCase()");

        // UpperCase
        createExpression(order, "self.shipper.companyName!upperCase()");

        // Length
        Expression shipperNameLength = createExpression(order, "self.shipper.companyName!length()");
        assertThat(shipperNameLength, instanceOf(NumericExpression.class));
        createExpression(order, "self.shipper.companyName!lowerCase()!length() > 0");

        // SubString
        createExpression(order, "self.shipper.companyName!substring(1, 4)!length() > 0");
        createExpression(order, "self.shipper.companyName!substring(1, self.shipper.companyName!length()-1)");

        // Position
        createExpression(order, "self.shipper.companyName!position('a') > 0");
        createExpression(order, "self.shipper.companyName!position(self.shipper.companyName) > 0");

        // Replace
        createExpression(order, "self.shipper.companyName!replace('\\n', '')");
        createExpression(order, "self.shipper.companyName!replace('$', self.shipper.companyName)");

        // Trim
        createExpression(order, "self.shipper.companyName!trim()");

        // First
        createExpression(order, "self.shipper.companyName!first(5)");

        // Last
        createExpression(order, "self.shipper.companyName!last(1)");

        // Matches
        createExpression(order, "self.shipper.companyName!matches('blackbelt\\\\.hu')");
    }

    @Test
    public void testDefined() {
        EClass order = findBase("Order");
        createExpression(order, "self.shipper!isUndefined()");
        createExpression(order, "self.shipper.companyName!isUndefined()");
        createExpression(order, "not self.shipper!isUndefined()");
    }

//    TODO @Test
//    public void testSequences() {
//        createExpression("demo::GlobalSequence!next()");
//        createExpression("demo::GlobalSequence!current()");
//    }

    @Test
    public void test002() {
        EClass customer = findBase("Customer");
        createExpression(customer, "self.addresses");

        createExpression("-1.5!round() < 1.2 and demo::entities::Order!sort()!head()!kindOf(demo::entities::InternationalOrder)");
        createExpression("demo::entities::Product!filter(p | p.discounted)!count() > 10 ? 1.2 : 8.7");
        // select categories, where the category has more than 10 products
        createExpression("demo::entities::Category!filter(c | demo::entities::Product!filter(p | p.category == c)!count() > 10)");

        createExpression("true ? 8[dkg]+12[g] : 2[g] + 4[g] + demo::entities::Product!sort()!head().weight");

        createExpression("(2 + 4) * 8[kg] * 60[kilometrePerHour] / 3[s]");
        createExpression("9[mm]/(1/45[cm])");
        createExpression("9[mg] < 2[kg]");
        createExpression("`2019-01-02T03:04:05.678+01:00 [Europe/Budapest]` + 102[s]");
        Expression timeStampAddition = createExpression("demo::entities::Order!sort()!head().orderDate - 3[day]");
        assertThat(timeStampAddition, instanceOf(TimestampExpression.class));
        createExpression("`2019-01-02T03:04:05.678+01:00 [Europe/Budapest]`!elapsedTimeFrom(`2019-01-30T15:57:08.123+01:00 [Europe/Budapest]`)");
        
        Expression customerExpression = createExpression(
        		"demo::entities::Order!filter("
        				+ "o | o=>orderDetails->product!contains("
        					+ "demo::entities::Product!filter("
        						+ "p | p.productName == 'Lenovo B51')!sort()!head()))"
        		+ "!asCollection("
        			+ "demo::entities::InternationalOrder)"
        		+ "!filter(io | io.exciseTax > 1/2 + io=>orderDetails!sum("
        			+ "iod | iod.unitPrice))"
        		+ "!sort(iof | iof.freight, iof=>orderDetails!count() DESC)"
        		+ "!head()->customer"
        		+ "!filter(c | "
        			+ "c=>addresses!sort()!head()"
        			+ "!asType(demo::entities::InternationalAddress).country == demo::types::Countries#RO and c=>addresses!sort()!head().postalCode!matches('11%'))=>addresses"
        		
        		);
        assertThat(customerExpression, instanceOf(CollectionNavigationFromObjectExpression.class));
    }

    @Test
    public void test003() {
        createExpression("demo::entities::Employee!filter(e | e.lastName == 'Gipsz' and e.firstName == 'Jakab')!sort()!head()=>orders!sort(o | o.orderDate DESC)");
        createExpression("demo::entities::Order=>orderDetails!filter(od | od.product.category.picture!isDefined())");
        createExpression("demo::entities::Order!any()=>orderDetails!sum(od | od.quantity * od.unitPrice * (1 - od.discount))");
    }
}
