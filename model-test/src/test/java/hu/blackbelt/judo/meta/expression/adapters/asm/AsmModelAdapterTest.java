package hu.blackbelt.judo.meta.expression.adapters.asm;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.buildAsmModel;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredDecimalBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newMeasuredIntegerBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.buildMeasureModel;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newBaseMeasureTermBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDerivedMeasureBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newDurationUnitBuilder;
import static hu.blackbelt.judo.meta.measure.util.builder.MeasureBuilders.newUnitBuilder;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.IntegerConstant;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;
import hu.blackbelt.judo.meta.expression.numeric.NumericAttribute;
import hu.blackbelt.judo.meta.measure.BaseMeasure;
import hu.blackbelt.judo.meta.measure.DerivedMeasure;
import hu.blackbelt.judo.meta.measure.DurationType;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;

public class AsmModelAdapterTest {

    private MeasureModel measureModel;
    private AsmModel asmModel;
    private AsmUtils asmUtils;

    private ModelAdapter<EClassifier, EDataType, EAttribute, EEnum, EClass, EClass, EReference, EClassifier, Measure, Unit> modelAdapter;

    @BeforeEach
    public void setUp() throws Exception {
    	
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

        modelAdapter = new AsmModelAdapter(asmModel.getResourceSet(), measureModel.getResourceSet());
        asmUtils = new AsmUtils(asmModel.getResourceSet());
    }

    private void populateAsmModel() {
    	//enum
    	EEnum countriesEnum = newEEnumBuilder().withName("Countries").withELiterals(newEEnumLiteralBuilder().withLiteral("HU").withName("HU").withValue(0).build(),
    			newEEnumLiteralBuilder().withLiteral("AT").withName("AT").withValue(1).build(),
    			newEEnumLiteralBuilder().withLiteral("RO").withName("RO").withValue(2).build(),
    			newEEnumLiteralBuilder().withLiteral("SK").withName("SK").withValue(3).build()).build();
    	
    	EEnum titlesEnum = newEEnumBuilder().withName("Titles").withELiterals(newEEnumLiteralBuilder().withLiteral("MS").withName("MS").withValue(0).build(),
    			newEEnumLiteralBuilder().withLiteral("MRS").withName("MRS").withValue(1).build(),
    			newEEnumLiteralBuilder().withLiteral("MR").withName("MR").withValue(2).build(),
    			newEEnumLiteralBuilder().withLiteral("DR").withName("DR").withValue(3).build()).build();
    	
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
    			.withEClassifiers(timestamp,stringType,doubleType,integerType,binary,dateType,countriesEnum,phoneType,booleanType,titlesEnum)
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
        		newDurationUnitBuilder().withName("nanosecond").withSymbol("ns").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0E+9)).withType(DurationType.NANOSECOND).build(),
        		newDurationUnitBuilder().withName("microsecond").withSymbol("μs").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1000000.0)).withType(DurationType.MICROSECOND).build(),
    			newDurationUnitBuilder().withName("millisecond").withSymbol("ms").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.MILLISECOND).build(),
    			newDurationUnitBuilder().withName("second").withSymbol("s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.SECOND).build(),
    			newDurationUnitBuilder().withName("minute").withSymbol("min").withRateDividend(new BigDecimal(60.0)).withRateDivisor(new BigDecimal(1.0)).build(),
    			newDurationUnitBuilder().withName("hour").withSymbol("h").withRateDividend(new BigDecimal(3600.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.HOUR).build(),
    			newDurationUnitBuilder().withName("day").withSymbol("").withRateDividend(new BigDecimal(86400.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.DAY).build(),
    			newDurationUnitBuilder().withName("week").withSymbol("").withRateDividend(new BigDecimal(604800.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.WEEK).build(),
    			newDurationUnitBuilder().withName("halfDay").withSymbol("").withRateDividend(new BigDecimal(43200.0)).withRateDivisor(new BigDecimal(1.0)).build())
        	.build();
        
        BaseMeasure monthBasedTime = newBaseMeasureBuilder().withName("MonthBasedTime").withNamespace("demo::measures").withUnits(
    			newDurationUnitBuilder().withName("month").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.MONTH).build(),
    			newDurationUnitBuilder().withName("year").withRateDividend(new BigDecimal(12.0)).withRateDivisor(new BigDecimal(1.0)).withType(DurationType.YEAR).build())
        	.build();
        
        BaseMeasure mass = newBaseMeasureBuilder().withName("Mass").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("milligram").withSymbol("mg").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("gram").withSymbol("g").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("dekagram").withSymbol("dkg").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("kilogram").withSymbol("kg").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("quintal").withSymbol("q").withRateDividend(new BigDecimal(100.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("tonne").withSymbol("t").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        BaseMeasure length = newBaseMeasureBuilder().withName("Length").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("nanometre").withSymbol("nm").withRateDividend(new BigDecimal(1.0E-9)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("micrometre").withSymbol("μm").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("millimetre").withSymbol("mm").withRateDividend(new BigDecimal(0.001)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("centimetre").withSymbol("cm").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("decimetre").withSymbol("dm").withRateDividend(new BigDecimal(0.1)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("metre").withSymbol("m").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("kilometre").withSymbol("km").withRateDividend(new BigDecimal(1000.0)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("inch").withSymbol("&quot;").withRateDividend(new BigDecimal(0.0254)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("foot").withSymbol("ft").withRateDividend(new BigDecimal(0.3048)).withRateDivisor(new BigDecimal(1.0)).build(),
        		newUnitBuilder().withName("mile").withSymbol("mi").withRateDividend(new BigDecimal(1609.344)).withRateDivisor(new BigDecimal(1.0)).build())
    		.build();
        
        DerivedMeasure velocity = newDerivedMeasureBuilder().withName("Velocity").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("kilometrePerHour").withSymbol("km/h").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(3.6)).build(),
        		newUnitBuilder().withName("metrePerSecond").withSymbol("m/s").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(length).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(-1).withBaseMeasure(time).build())
    		.build();
        
    	DerivedMeasure area = newDerivedMeasureBuilder().withName("Area").withNamespace("demo::measures").withUnits(
      			newUnitBuilder().withName("squareMillimetre").withSymbol("mm²").withRateDividend(new BigDecimal(0.0000010)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareCentimetre").withSymbol("cm²").withRateDividend(new BigDecimal(0.00010)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareDecimetre").withSymbol("dm²").withRateDividend(new BigDecimal(0.01)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareMetre").withSymbol("m²").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("hectare").withSymbol("ha").withRateDividend(new BigDecimal(10000.0)).withRateDivisor(new BigDecimal(1.0)).build(),
      			newUnitBuilder().withName("squareKilometre").withSymbol("km²").withRateDividend(new BigDecimal(1000000.0)).withRateDivisor(new BigDecimal(1.0)).build())
    			.withTerms(newBaseMeasureTermBuilder().withExponent(2).withBaseMeasure(length).build())
    	.build();
    	
        DerivedMeasure force = newDerivedMeasureBuilder().withName("Force").withNamespace("demo::measures").withUnits(
        		newUnitBuilder().withName("newton").withSymbol("N").withRateDividend(new BigDecimal(1.0)).withRateDivisor(new BigDecimal(1.0)).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(-2).withBaseMeasure(time).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(mass).build())
        		.withTerms(newBaseMeasureTermBuilder().withExponent(1).withBaseMeasure(length).build())
    		.build();
        
        measureModel.addContent(time);
        measureModel.addContent(mass);
        measureModel.addContent(length);
        measureModel.addContent(velocity);
        measureModel.addContent(area);
        measureModel.addContent(force);
        measureModel.addContent(monthBasedTime);
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
        Optional<TypeName> categoryTypeName = modelAdapter.buildTypeName(asmUtils.resolve("demo.entities.Category").get());

        assertTrue(categoryTypeName.isPresent());
        assertThat(categoryTypeName.get().getName(), is("Category")); //TODO: check, seems kinda silly (+psm)
        assertThat(categoryTypeName.get().getNamespace(), is("demo::entities"));
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
        Optional<EClass> containerClass = asmUtils.resolve("demo.entities.Category").map(c -> (EClass) c);
        Optional<EReference> productsReference = containerClass.get().getEReferences().stream().filter(r -> "products".equals(r.getName())).findAny();
        Optional<EClass> targetClass = asmUtils.resolve("demo.entities.Product").map(c -> (EClass) c);

        assertTrue(containerClass.isPresent());
        assertTrue(targetClass.isPresent());
        assertTrue(productsReference.isPresent());
        assertThat(modelAdapter.getTarget(productsReference.get()), is(targetClass.get()));
    }

    @Test
    public void testGetAttribute() {
        Optional<EClass> eClass = asmUtils.resolve("demo.entities.Category").map(c -> (EClass) c);
        Optional<EAttribute> categoryNameEAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        assertTrue(eClass.isPresent());
        assertTrue(categoryNameEAttribute.isPresent());
        assertThat(modelAdapter.getAttribute(eClass.get(), "categoryName"), is(categoryNameEAttribute));
        assertThat(modelAdapter.getAttribute(eClass.get(), "productName"), is(Optional.empty()));
    }

    @Test
    public void testGetAttributeType() {
        Optional<EClass> eClass = asmUtils.resolve("demo.entities.Category").map(c -> (EClass) c);
        Optional<EAttribute> eAttribute = eClass.get().getEAttributes().stream().filter(a -> "categoryName".equals(a.getName())).findAny();

        assertTrue(eClass.isPresent());
        assertTrue(eAttribute.isPresent());
        assertThat(modelAdapter.getAttributeType(eClass.get(), "categoryName").get(),
                is(eAttribute.get().getEAttributeType()));
    }

    @Test
    public void testGetSuperType() {
        Optional<EClass> childClass = asmUtils.resolve("demo.entities.Company").map(c -> (EClass) c);
        Optional<EClass> superClass = asmUtils.resolve("demo.entities.Customer").map(c -> (EClass) c);

        assertTrue(childClass.isPresent());
        assertTrue(superClass.isPresent());
        assertTrue(modelAdapter.getSuperTypes(childClass.get()).contains(superClass.get()));

        Optional<EClass> negtestClass = asmUtils.resolve("demo.entities.Order").map(c -> (EClass) c);
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
        Optional<Unit> microsecond = time.get().getUnits().stream().filter(u -> "microsecond".equals(u.getName())).findAny();

        assertTrue(time.isPresent());
        assertTrue(day.isPresent());
        assertTrue(modelAdapter.isDurationSupportingAddition(day.get()));
        assertTrue(microsecond.isPresent());
        assertFalse(modelAdapter.isDurationSupportingAddition(microsecond.get()));
    }

    //TODO
    //@Test
    void testGetUnit() {
        TypeName type = modelAdapter.buildTypeName(asmUtils.resolve("demo.entities.Product").get()).get();
        Instance instance = newInstanceBuilder().withElementName(type).build();
        //EClass eClass = newEClassBuilder().withName("Product")..build();

        Optional<Unit> kilogram = getUnitByName("kilogram");
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
