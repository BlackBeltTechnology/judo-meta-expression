/**
 */
package hu.blackbelt.judo.meta.expression.operator.impl;

import hu.blackbelt.judo.meta.expression.ExpressionPackage;

import hu.blackbelt.judo.meta.expression.collection.CollectionPackage;

import hu.blackbelt.judo.meta.expression.collection.impl.CollectionPackageImpl;

import hu.blackbelt.judo.meta.expression.constant.ConstantPackage;

import hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl;

import hu.blackbelt.judo.meta.expression.custom.CustomPackage;

import hu.blackbelt.judo.meta.expression.custom.impl.CustomPackageImpl;

import hu.blackbelt.judo.meta.expression.enumeration.EnumerationPackage;

import hu.blackbelt.judo.meta.expression.enumeration.impl.EnumerationPackageImpl;

import hu.blackbelt.judo.meta.expression.impl.ExpressionPackageImpl;

import hu.blackbelt.judo.meta.expression.logical.LogicalPackage;

import hu.blackbelt.judo.meta.expression.logical.impl.LogicalPackageImpl;

import hu.blackbelt.judo.meta.expression.numeric.NumericPackage;

import hu.blackbelt.judo.meta.expression.numeric.impl.NumericPackageImpl;

import hu.blackbelt.judo.meta.expression.object.ObjectPackage;

import hu.blackbelt.judo.meta.expression.object.impl.ObjectPackageImpl;

import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.DecimalComparator;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerComparator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.LogicalOperator;
import hu.blackbelt.judo.meta.expression.operator.ObjectComparator;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.expression.operator.OperatorFactory;
import hu.blackbelt.judo.meta.expression.operator.OperatorPackage;
import hu.blackbelt.judo.meta.expression.operator.StringComparator;
import hu.blackbelt.judo.meta.expression.operator.TimestampDurationOperator;

import hu.blackbelt.judo.meta.expression.string.StringPackage;

import hu.blackbelt.judo.meta.expression.string.impl.StringPackageImpl;

import hu.blackbelt.judo.meta.expression.temporal.TemporalPackage;

import hu.blackbelt.judo.meta.expression.temporal.impl.TemporalPackageImpl;

import hu.blackbelt.judo.meta.expression.variable.VariablePackage;

import hu.blackbelt.judo.meta.expression.variable.impl.VariablePackageImpl;

import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class OperatorPackageImpl extends EPackageImpl implements OperatorPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum stringComparatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum logicalOperatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum objectSelectorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum integerOperatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum decimalOperatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum integerComparatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum decimalComparatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum integerAggregatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum decimalAggregatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum objectComparatorEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum timestampDurationOperatorEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see hu.blackbelt.judo.meta.expression.operator.OperatorPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private OperatorPackageImpl() {
		super(eNS_URI, OperatorFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link OperatorPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static OperatorPackage init() {
		if (isInited) return (OperatorPackage)EPackage.Registry.INSTANCE.getEPackage(OperatorPackage.eNS_URI);

		// Obtain or create and register package
		OperatorPackageImpl theOperatorPackage = (OperatorPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof OperatorPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new OperatorPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ExpressionPackageImpl theExpressionPackage = (ExpressionPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ExpressionPackage.eNS_URI) instanceof ExpressionPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ExpressionPackage.eNS_URI) : ExpressionPackage.eINSTANCE);
		ConstantPackageImpl theConstantPackage = (ConstantPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ConstantPackage.eNS_URI) instanceof ConstantPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ConstantPackage.eNS_URI) : ConstantPackage.eINSTANCE);
		VariablePackageImpl theVariablePackage = (VariablePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(VariablePackage.eNS_URI) instanceof VariablePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(VariablePackage.eNS_URI) : VariablePackage.eINSTANCE);
		NumericPackageImpl theNumericPackage = (NumericPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(NumericPackage.eNS_URI) instanceof NumericPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(NumericPackage.eNS_URI) : NumericPackage.eINSTANCE);
		LogicalPackageImpl theLogicalPackage = (LogicalPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(LogicalPackage.eNS_URI) instanceof LogicalPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(LogicalPackage.eNS_URI) : LogicalPackage.eINSTANCE);
		StringPackageImpl theStringPackage = (StringPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(StringPackage.eNS_URI) instanceof StringPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(StringPackage.eNS_URI) : StringPackage.eINSTANCE);
		EnumerationPackageImpl theEnumerationPackage = (EnumerationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(EnumerationPackage.eNS_URI) instanceof EnumerationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(EnumerationPackage.eNS_URI) : EnumerationPackage.eINSTANCE);
		ObjectPackageImpl theObjectPackage = (ObjectPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ObjectPackage.eNS_URI) instanceof ObjectPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ObjectPackage.eNS_URI) : ObjectPackage.eINSTANCE);
		CollectionPackageImpl theCollectionPackage = (CollectionPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) instanceof CollectionPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) : CollectionPackage.eINSTANCE);
		CustomPackageImpl theCustomPackage = (CustomPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CustomPackage.eNS_URI) instanceof CustomPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CustomPackage.eNS_URI) : CustomPackage.eINSTANCE);
		TemporalPackageImpl theTemporalPackage = (TemporalPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(TemporalPackage.eNS_URI) instanceof TemporalPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(TemporalPackage.eNS_URI) : TemporalPackage.eINSTANCE);

		// Create package meta-data objects
		theOperatorPackage.createPackageContents();
		theExpressionPackage.createPackageContents();
		theConstantPackage.createPackageContents();
		theVariablePackage.createPackageContents();
		theNumericPackage.createPackageContents();
		theLogicalPackage.createPackageContents();
		theStringPackage.createPackageContents();
		theEnumerationPackage.createPackageContents();
		theObjectPackage.createPackageContents();
		theCollectionPackage.createPackageContents();
		theCustomPackage.createPackageContents();
		theTemporalPackage.createPackageContents();

		// Initialize created meta-data
		theOperatorPackage.initializePackageContents();
		theExpressionPackage.initializePackageContents();
		theConstantPackage.initializePackageContents();
		theVariablePackage.initializePackageContents();
		theNumericPackage.initializePackageContents();
		theLogicalPackage.initializePackageContents();
		theStringPackage.initializePackageContents();
		theEnumerationPackage.initializePackageContents();
		theObjectPackage.initializePackageContents();
		theCollectionPackage.initializePackageContents();
		theCustomPackage.initializePackageContents();
		theTemporalPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theOperatorPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(OperatorPackage.eNS_URI, theOperatorPackage);
		return theOperatorPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getStringComparator() {
		return stringComparatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getLogicalOperator() {
		return logicalOperatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getObjectSelector() {
		return objectSelectorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getIntegerOperator() {
		return integerOperatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDecimalOperator() {
		return decimalOperatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getIntegerComparator() {
		return integerComparatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDecimalComparator() {
		return decimalComparatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getIntegerAggregator() {
		return integerAggregatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDecimalAggregator() {
		return decimalAggregatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getObjectComparator() {
		return objectComparatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getTimestampDurationOperator() {
		return timestampDurationOperatorEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OperatorFactory getOperatorFactory() {
		return (OperatorFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create enums
		stringComparatorEEnum = createEEnum(STRING_COMPARATOR);
		logicalOperatorEEnum = createEEnum(LOGICAL_OPERATOR);
		objectSelectorEEnum = createEEnum(OBJECT_SELECTOR);
		integerOperatorEEnum = createEEnum(INTEGER_OPERATOR);
		decimalOperatorEEnum = createEEnum(DECIMAL_OPERATOR);
		integerComparatorEEnum = createEEnum(INTEGER_COMPARATOR);
		decimalComparatorEEnum = createEEnum(DECIMAL_COMPARATOR);
		integerAggregatorEEnum = createEEnum(INTEGER_AGGREGATOR);
		decimalAggregatorEEnum = createEEnum(DECIMAL_AGGREGATOR);
		objectComparatorEEnum = createEEnum(OBJECT_COMPARATOR);
		timestampDurationOperatorEEnum = createEEnum(TIMESTAMP_DURATION_OPERATOR);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Initialize enums and add enum literals
		initEEnum(stringComparatorEEnum, StringComparator.class, "StringComparator");
		addEEnumLiteral(stringComparatorEEnum, StringComparator.LESS_THAN);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.GREATER_THAN);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.LESS_OR_EQUAL);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.GREATER_OR_EQUAL);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.EQUAL);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.NOT_EQUAL);
		addEEnumLiteral(stringComparatorEEnum, StringComparator.MATCHES);

		initEEnum(logicalOperatorEEnum, LogicalOperator.class, "LogicalOperator");
		addEEnumLiteral(logicalOperatorEEnum, LogicalOperator.OR);
		addEEnumLiteral(logicalOperatorEEnum, LogicalOperator.AND);
		addEEnumLiteral(logicalOperatorEEnum, LogicalOperator.XOR);
		addEEnumLiteral(logicalOperatorEEnum, LogicalOperator.IMPLIES);

		initEEnum(objectSelectorEEnum, ObjectSelector.class, "ObjectSelector");
		addEEnumLiteral(objectSelectorEEnum, ObjectSelector.HEAD);
		addEEnumLiteral(objectSelectorEEnum, ObjectSelector.TAIL);

		initEEnum(integerOperatorEEnum, IntegerOperator.class, "IntegerOperator");
		addEEnumLiteral(integerOperatorEEnum, IntegerOperator.ADD);
		addEEnumLiteral(integerOperatorEEnum, IntegerOperator.SUBSTRACT);
		addEEnumLiteral(integerOperatorEEnum, IntegerOperator.MULTIPLY);
		addEEnumLiteral(integerOperatorEEnum, IntegerOperator.MODULO);
		addEEnumLiteral(integerOperatorEEnum, IntegerOperator.DIVIDE);

		initEEnum(decimalOperatorEEnum, DecimalOperator.class, "DecimalOperator");
		addEEnumLiteral(decimalOperatorEEnum, DecimalOperator.ADD);
		addEEnumLiteral(decimalOperatorEEnum, DecimalOperator.SUBSTRACT);
		addEEnumLiteral(decimalOperatorEEnum, DecimalOperator.MULTIPLY);
		addEEnumLiteral(decimalOperatorEEnum, DecimalOperator.DIVIDE);

		initEEnum(integerComparatorEEnum, IntegerComparator.class, "IntegerComparator");
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.LESS_THAN);
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.GREATER_THAN);
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.LESS_OR_EQUAL);
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.GREATER_OR_EQUAL);
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.EQUAL);
		addEEnumLiteral(integerComparatorEEnum, IntegerComparator.NOT_EQUAL);

		initEEnum(decimalComparatorEEnum, DecimalComparator.class, "DecimalComparator");
		addEEnumLiteral(decimalComparatorEEnum, DecimalComparator.LESS_THAN);
		addEEnumLiteral(decimalComparatorEEnum, DecimalComparator.GREATER_THAN);

		initEEnum(integerAggregatorEEnum, IntegerAggregator.class, "IntegerAggregator");
		addEEnumLiteral(integerAggregatorEEnum, IntegerAggregator.MIN);
		addEEnumLiteral(integerAggregatorEEnum, IntegerAggregator.MAX);
		addEEnumLiteral(integerAggregatorEEnum, IntegerAggregator.SUM);

		initEEnum(decimalAggregatorEEnum, DecimalAggregator.class, "DecimalAggregator");
		addEEnumLiteral(decimalAggregatorEEnum, DecimalAggregator.MIN);
		addEEnumLiteral(decimalAggregatorEEnum, DecimalAggregator.MAX);
		addEEnumLiteral(decimalAggregatorEEnum, DecimalAggregator.AVG);
		addEEnumLiteral(decimalAggregatorEEnum, DecimalAggregator.SUM);

		initEEnum(objectComparatorEEnum, ObjectComparator.class, "ObjectComparator");
		addEEnumLiteral(objectComparatorEEnum, ObjectComparator.EQUAL);
		addEEnumLiteral(objectComparatorEEnum, ObjectComparator.NOT_EQUAL);

		initEEnum(timestampDurationOperatorEEnum, TimestampDurationOperator.class, "TimestampDurationOperator");
		addEEnumLiteral(timestampDurationOperatorEEnum, TimestampDurationOperator.ADD);
		addEEnumLiteral(timestampDurationOperatorEEnum, TimestampDurationOperator.SUBSTRACT);
	}

} //OperatorPackageImpl
