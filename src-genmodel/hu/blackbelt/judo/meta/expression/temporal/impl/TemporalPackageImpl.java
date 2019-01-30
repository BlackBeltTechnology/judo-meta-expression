/**
 */
package hu.blackbelt.judo.meta.expression.temporal.impl;

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

import hu.blackbelt.judo.meta.expression.operator.OperatorPackage;

import hu.blackbelt.judo.meta.expression.operator.impl.OperatorPackageImpl;

import hu.blackbelt.judo.meta.expression.string.StringPackage;

import hu.blackbelt.judo.meta.expression.string.impl.StringPackageImpl;

import hu.blackbelt.judo.meta.expression.temporal.DateAttribute;
import hu.blackbelt.judo.meta.expression.temporal.DateSwitchExpression;
import hu.blackbelt.judo.meta.expression.temporal.TemporalFactory;
import hu.blackbelt.judo.meta.expression.temporal.TemporalPackage;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAdditionExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.temporal.TimestampDifferenceExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampSwitchExpression;

import hu.blackbelt.judo.meta.expression.variable.VariablePackage;

import hu.blackbelt.judo.meta.expression.variable.impl.VariablePackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TemporalPackageImpl extends EPackageImpl implements TemporalPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dateAttributeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timestampAttributeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dateSwitchExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timestampSwitchExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timestampAdditionExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timestampDifferenceExpressionEClass = null;

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
	 * @see hu.blackbelt.judo.meta.expression.temporal.TemporalPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TemporalPackageImpl() {
		super(eNS_URI, TemporalFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link TemporalPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TemporalPackage init() {
		if (isInited) return (TemporalPackage)EPackage.Registry.INSTANCE.getEPackage(TemporalPackage.eNS_URI);

		// Obtain or create and register package
		TemporalPackageImpl theTemporalPackage = (TemporalPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TemporalPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TemporalPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ExpressionPackageImpl theExpressionPackage = (ExpressionPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ExpressionPackage.eNS_URI) instanceof ExpressionPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ExpressionPackage.eNS_URI) : ExpressionPackage.eINSTANCE);
		ConstantPackageImpl theConstantPackage = (ConstantPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ConstantPackage.eNS_URI) instanceof ConstantPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ConstantPackage.eNS_URI) : ConstantPackage.eINSTANCE);
		VariablePackageImpl theVariablePackage = (VariablePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(VariablePackage.eNS_URI) instanceof VariablePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(VariablePackage.eNS_URI) : VariablePackage.eINSTANCE);
		OperatorPackageImpl theOperatorPackage = (OperatorPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(OperatorPackage.eNS_URI) instanceof OperatorPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(OperatorPackage.eNS_URI) : OperatorPackage.eINSTANCE);
		NumericPackageImpl theNumericPackage = (NumericPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(NumericPackage.eNS_URI) instanceof NumericPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(NumericPackage.eNS_URI) : NumericPackage.eINSTANCE);
		LogicalPackageImpl theLogicalPackage = (LogicalPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(LogicalPackage.eNS_URI) instanceof LogicalPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(LogicalPackage.eNS_URI) : LogicalPackage.eINSTANCE);
		StringPackageImpl theStringPackage = (StringPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(StringPackage.eNS_URI) instanceof StringPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(StringPackage.eNS_URI) : StringPackage.eINSTANCE);
		EnumerationPackageImpl theEnumerationPackage = (EnumerationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(EnumerationPackage.eNS_URI) instanceof EnumerationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(EnumerationPackage.eNS_URI) : EnumerationPackage.eINSTANCE);
		ObjectPackageImpl theObjectPackage = (ObjectPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ObjectPackage.eNS_URI) instanceof ObjectPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ObjectPackage.eNS_URI) : ObjectPackage.eINSTANCE);
		CollectionPackageImpl theCollectionPackage = (CollectionPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) instanceof CollectionPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) : CollectionPackage.eINSTANCE);
		CustomPackageImpl theCustomPackage = (CustomPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CustomPackage.eNS_URI) instanceof CustomPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CustomPackage.eNS_URI) : CustomPackage.eINSTANCE);

		// Create package meta-data objects
		theTemporalPackage.createPackageContents();
		theExpressionPackage.createPackageContents();
		theConstantPackage.createPackageContents();
		theVariablePackage.createPackageContents();
		theOperatorPackage.createPackageContents();
		theNumericPackage.createPackageContents();
		theLogicalPackage.createPackageContents();
		theStringPackage.createPackageContents();
		theEnumerationPackage.createPackageContents();
		theObjectPackage.createPackageContents();
		theCollectionPackage.createPackageContents();
		theCustomPackage.createPackageContents();

		// Initialize created meta-data
		theTemporalPackage.initializePackageContents();
		theExpressionPackage.initializePackageContents();
		theConstantPackage.initializePackageContents();
		theVariablePackage.initializePackageContents();
		theOperatorPackage.initializePackageContents();
		theNumericPackage.initializePackageContents();
		theLogicalPackage.initializePackageContents();
		theStringPackage.initializePackageContents();
		theEnumerationPackage.initializePackageContents();
		theObjectPackage.initializePackageContents();
		theCollectionPackage.initializePackageContents();
		theCustomPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTemporalPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TemporalPackage.eNS_URI, theTemporalPackage);
		return theTemporalPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDateAttribute() {
		return dateAttributeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTimestampAttribute() {
		return timestampAttributeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDateSwitchExpression() {
		return dateSwitchExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTimestampSwitchExpression() {
		return timestampSwitchExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTimestampAdditionExpression() {
		return timestampAdditionExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTimestampAdditionExpression_Timestamp() {
		return (EReference)timestampAdditionExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTimestampAdditionExpression_Duration() {
		return (EReference)timestampAdditionExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTimestampAdditionExpression_Operator() {
		return (EAttribute)timestampAdditionExpressionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTimestampDifferenceExpression() {
		return timestampDifferenceExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTimestampDifferenceExpression_StartTimestamp() {
		return (EReference)timestampDifferenceExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTimestampDifferenceExpression_EndTimestamp() {
		return (EReference)timestampDifferenceExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TemporalFactory getTemporalFactory() {
		return (TemporalFactory)getEFactoryInstance();
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

		// Create classes and their features
		dateAttributeEClass = createEClass(DATE_ATTRIBUTE);

		timestampAttributeEClass = createEClass(TIMESTAMP_ATTRIBUTE);

		dateSwitchExpressionEClass = createEClass(DATE_SWITCH_EXPRESSION);

		timestampSwitchExpressionEClass = createEClass(TIMESTAMP_SWITCH_EXPRESSION);

		timestampAdditionExpressionEClass = createEClass(TIMESTAMP_ADDITION_EXPRESSION);
		createEReference(timestampAdditionExpressionEClass, TIMESTAMP_ADDITION_EXPRESSION__TIMESTAMP);
		createEReference(timestampAdditionExpressionEClass, TIMESTAMP_ADDITION_EXPRESSION__DURATION);
		createEAttribute(timestampAdditionExpressionEClass, TIMESTAMP_ADDITION_EXPRESSION__OPERATOR);

		timestampDifferenceExpressionEClass = createEClass(TIMESTAMP_DIFFERENCE_EXPRESSION);
		createEReference(timestampDifferenceExpressionEClass, TIMESTAMP_DIFFERENCE_EXPRESSION__START_TIMESTAMP);
		createEReference(timestampDifferenceExpressionEClass, TIMESTAMP_DIFFERENCE_EXPRESSION__END_TIMESTAMP);
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

		// Obtain other dependent packages
		ExpressionPackage theExpressionPackage = (ExpressionPackage)EPackage.Registry.INSTANCE.getEPackage(ExpressionPackage.eNS_URI);
		OperatorPackage theOperatorPackage = (OperatorPackage)EPackage.Registry.INSTANCE.getEPackage(OperatorPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		dateAttributeEClass.getESuperTypes().add(theExpressionPackage.getDateExpression());
		dateAttributeEClass.getESuperTypes().add(theExpressionPackage.getAttributeSelector());
		timestampAttributeEClass.getESuperTypes().add(theExpressionPackage.getTimestampExpression());
		timestampAttributeEClass.getESuperTypes().add(theExpressionPackage.getAttributeSelector());
		dateSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getSwitchExpression());
		dateSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getDateExpression());
		timestampSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getSwitchExpression());
		timestampSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getTimestampExpression());
		timestampAdditionExpressionEClass.getESuperTypes().add(theExpressionPackage.getTimestampExpression());
		timestampDifferenceExpressionEClass.getESuperTypes().add(theExpressionPackage.getNumericExpression());

		// Initialize classes, features, and operations; add parameters
		initEClass(dateAttributeEClass, DateAttribute.class, "DateAttribute", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(timestampAttributeEClass, TimestampAttribute.class, "TimestampAttribute", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dateSwitchExpressionEClass, DateSwitchExpression.class, "DateSwitchExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(timestampSwitchExpressionEClass, TimestampSwitchExpression.class, "TimestampSwitchExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(timestampAdditionExpressionEClass, TimestampAdditionExpression.class, "TimestampAdditionExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTimestampAdditionExpression_Timestamp(), theExpressionPackage.getTimestampExpression(), null, "timestamp", null, 1, 1, TimestampAdditionExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTimestampAdditionExpression_Duration(), theExpressionPackage.getNumericExpression(), null, "duration", null, 1, 1, TimestampAdditionExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimestampAdditionExpression_Operator(), theOperatorPackage.getTimestampDurationOperator(), "operator", null, 1, 1, TimestampAdditionExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(timestampDifferenceExpressionEClass, TimestampDifferenceExpression.class, "TimestampDifferenceExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTimestampDifferenceExpression_StartTimestamp(), theExpressionPackage.getTimestampExpression(), null, "startTimestamp", null, 1, 1, TimestampDifferenceExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTimestampDifferenceExpression_EndTimestamp(), theExpressionPackage.getTimestampExpression(), null, "endTimestamp", null, 1, 1, TimestampDifferenceExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
	}

} //TemporalPackageImpl
