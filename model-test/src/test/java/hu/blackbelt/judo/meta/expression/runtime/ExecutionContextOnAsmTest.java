package hu.blackbelt.judo.meta.expression.runtime;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.buildAsmModel;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionNavigationFromObjectExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.measure.BaseMeasure;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.DurationType;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;

abstract class ExecutionContextOnAsmTest {

	final Log log = new Slf4jLog();

    AsmModel asmModel;
    MeasureModel measureModel;
    ExpressionModel expressionModel;

	private AsmUtils asmUtils;
    
    void setUp() throws Exception {
    	asmModel = buildAsmModel()
    			.uri(URI.createURI("urn:asm.judo-meta-asm"))
                .name("demo")
                .build();
    	asmUtils = new AsmUtils(asmModel.getResourceSet());
    	populateAsmModel();
    	
    	measureModel = buildMeasureModel()
                .name(asmModel.getName())
                .build();
    	populateMeasureModel();
        
  		log.info(asmModel.getDiagnosticsAsString());
  		log.info(measureModel.getDiagnosticsAsString());
    	assertTrue(asmModel.isValid());
    	assertTrue(measureModel.isValid());
    	
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("expression:test"))
                .build();

        final TypeName orderType = newTypeNameBuilder().withNamespace("demo::entities").withName("Order").build();
        expressionModelResourceSupport.addContent(orderType);
        expressionModelResourceSupport.addContent(newTypeNameBuilder().withNamespace("demo::entities").withName("OrderDetail").build());

        final ObjectVariable order = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .build();

        final TimestampAttribute orderDate = newTimestampAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withAttributeName("orderDate")
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
                .build();

        final CollectionNavigationFromObjectExpression orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .build();

        expressionModelResourceSupport.addContent(orderType);
        expressionModelResourceSupport.addContent(order);
        expressionModelResourceSupport.addContent(orderDate);
        expressionModelResourceSupport.addContent(shipperName);
        expressionModelResourceSupport.addContent(orderDetails);
        
        expressionModel = ExpressionModel.buildExpressionModel()
                .name("expression")
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .build();
        
    	log.info(expressionModel.getDiagnosticsAsString());
    	assertTrue(expressionModel.isValid());
    }

    protected AsmModel getAsmModel() throws Exception {
        return asmModel;
    }

    protected MeasureModel getMeasureModel() throws Exception {
    	return measureModel;
    }
    
    protected ExpressionModel getExpressionModel() throws Exception {
    	return expressionModel;
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
    	EAttribute price = newEAttributeBuilder().withName("price").withEType(doubleType).build();
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
    	EClass order = newEClassBuilder().withName("Order")
    			.withEStructuralFeatures(orderDate,orderDetails,categories,employeeRef,shipperRef,customerOrder,shipAddress,freight).build();
    	EClass orderDetail = newEClassBuilder().withName("OrderDetail").withEStructuralFeatures(productRef,unitPriceOrderDetail,quantity,discount,price).build();
    	EClass product = newEClassBuilder().withName("Product").withEStructuralFeatures(categoryRef,productName,unitPrice,quantityPerUnit,discounted,weight).build();
    	EClass category = newEClassBuilder().withName("Category").withEStructuralFeatures(productsRef,categoryName,picture,owner).build();
    	EClass employee = newEClassBuilder().withName("Employee").withEStructuralFeatures(ordersRef,categoryEmployee,firstNameEmployee,lastNameEmployee).build();
    	EClass internationalOrder = newEClassBuilder().withName("InternationalOrder").withEStructuralFeatures(exciseTax,customsDescription)
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
}
