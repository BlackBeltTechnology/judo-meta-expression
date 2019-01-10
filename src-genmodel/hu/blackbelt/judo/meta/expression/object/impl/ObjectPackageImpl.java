/**
 */
package hu.blackbelt.judo.meta.expression.object.impl;

import hu.blackbelt.judo.meta.expression.ExpressionPackage;

import hu.blackbelt.judo.meta.expression.collection.CollectionPackage;

import hu.blackbelt.judo.meta.expression.collection.impl.CollectionPackageImpl;

import hu.blackbelt.judo.meta.expression.constant.ConstantPackage;

import hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl;

import hu.blackbelt.judo.meta.expression.enumeration.EnumerationPackage;

import hu.blackbelt.judo.meta.expression.enumeration.impl.EnumerationPackageImpl;

import hu.blackbelt.judo.meta.expression.impl.ExpressionPackageImpl;

import hu.blackbelt.judo.meta.expression.logical.LogicalPackage;

import hu.blackbelt.judo.meta.expression.logical.impl.LogicalPackageImpl;

import hu.blackbelt.judo.meta.expression.numeric.NumericPackage;

import hu.blackbelt.judo.meta.expression.numeric.impl.NumericPackageImpl;

import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.object.ObjectFactory;
import hu.blackbelt.judo.meta.expression.object.ObjectFilterExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectPackage;
import hu.blackbelt.judo.meta.expression.object.ObjectSelectorExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectSwitchExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;

import hu.blackbelt.judo.meta.expression.operator.OperatorPackage;

import hu.blackbelt.judo.meta.expression.operator.impl.OperatorPackageImpl;

import hu.blackbelt.judo.meta.expression.string.StringPackage;

import hu.blackbelt.judo.meta.expression.string.impl.StringPackageImpl;

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
public class ObjectPackageImpl extends EPackageImpl implements ObjectPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectNavigationExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectSelectorExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectFilterExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectVariableReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass castObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectSwitchExpressionEClass = null;

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
	 * @see hu.blackbelt.judo.meta.expression.object.ObjectPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ObjectPackageImpl() {
		super(eNS_URI, ObjectFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ObjectPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ObjectPackage init() {
		if (isInited) return (ObjectPackage)EPackage.Registry.INSTANCE.getEPackage(ObjectPackage.eNS_URI);

		// Obtain or create and register package
		ObjectPackageImpl theObjectPackage = (ObjectPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ObjectPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ObjectPackageImpl());

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
		CollectionPackageImpl theCollectionPackage = (CollectionPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) instanceof CollectionPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CollectionPackage.eNS_URI) : CollectionPackage.eINSTANCE);

		// Create package meta-data objects
		theObjectPackage.createPackageContents();
		theExpressionPackage.createPackageContents();
		theConstantPackage.createPackageContents();
		theVariablePackage.createPackageContents();
		theOperatorPackage.createPackageContents();
		theNumericPackage.createPackageContents();
		theLogicalPackage.createPackageContents();
		theStringPackage.createPackageContents();
		theEnumerationPackage.createPackageContents();
		theCollectionPackage.createPackageContents();

		// Initialize created meta-data
		theObjectPackage.initializePackageContents();
		theExpressionPackage.initializePackageContents();
		theConstantPackage.initializePackageContents();
		theVariablePackage.initializePackageContents();
		theOperatorPackage.initializePackageContents();
		theNumericPackage.initializePackageContents();
		theLogicalPackage.initializePackageContents();
		theStringPackage.initializePackageContents();
		theEnumerationPackage.initializePackageContents();
		theCollectionPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theObjectPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ObjectPackage.eNS_URI, theObjectPackage);
		return theObjectPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getObjectNavigationExpression() {
		return objectNavigationExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectNavigationExpression_ObjectExpression() {
		return (EReference)objectNavigationExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getObjectSelectorExpression() {
		return objectSelectorExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectSelectorExpression_CollectionExpression() {
		return (EReference)objectSelectorExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getObjectSelectorExpression_Operator() {
		return (EAttribute)objectSelectorExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getObjectFilterExpression() {
		return objectFilterExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectFilterExpression_ObjectExpression() {
		return (EReference)objectFilterExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectFilterExpression_Condition() {
		return (EReference)objectFilterExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getObjectVariableReference() {
		return objectVariableReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectVariableReference_Variable() {
		return (EReference)objectVariableReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCastObject() {
		return castObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCastObject_ElementName() {
		return (EReference)castObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCastObject_ObjectExpression() {
		return (EReference)castObjectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getObjectSwitchExpression() {
		return objectSwitchExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getObjectSwitchExpression_ElementName() {
		return (EReference)objectSwitchExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ObjectFactory getObjectFactory() {
		return (ObjectFactory)getEFactoryInstance();
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
		objectNavigationExpressionEClass = createEClass(OBJECT_NAVIGATION_EXPRESSION);
		createEReference(objectNavigationExpressionEClass, OBJECT_NAVIGATION_EXPRESSION__OBJECT_EXPRESSION);

		objectSelectorExpressionEClass = createEClass(OBJECT_SELECTOR_EXPRESSION);
		createEReference(objectSelectorExpressionEClass, OBJECT_SELECTOR_EXPRESSION__COLLECTION_EXPRESSION);
		createEAttribute(objectSelectorExpressionEClass, OBJECT_SELECTOR_EXPRESSION__OPERATOR);

		objectFilterExpressionEClass = createEClass(OBJECT_FILTER_EXPRESSION);
		createEReference(objectFilterExpressionEClass, OBJECT_FILTER_EXPRESSION__OBJECT_EXPRESSION);
		createEReference(objectFilterExpressionEClass, OBJECT_FILTER_EXPRESSION__CONDITION);

		objectVariableReferenceEClass = createEClass(OBJECT_VARIABLE_REFERENCE);
		createEReference(objectVariableReferenceEClass, OBJECT_VARIABLE_REFERENCE__VARIABLE);

		castObjectEClass = createEClass(CAST_OBJECT);
		createEReference(castObjectEClass, CAST_OBJECT__ELEMENT_NAME);
		createEReference(castObjectEClass, CAST_OBJECT__OBJECT_EXPRESSION);

		objectSwitchExpressionEClass = createEClass(OBJECT_SWITCH_EXPRESSION);
		createEReference(objectSwitchExpressionEClass, OBJECT_SWITCH_EXPRESSION__ELEMENT_NAME);
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
		VariablePackage theVariablePackage = (VariablePackage)EPackage.Registry.INSTANCE.getEPackage(VariablePackage.eNS_URI);
		OperatorPackage theOperatorPackage = (OperatorPackage)EPackage.Registry.INSTANCE.getEPackage(OperatorPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		objectNavigationExpressionEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());
		objectNavigationExpressionEClass.getESuperTypes().add(theVariablePackage.getObjectVariable());
		objectNavigationExpressionEClass.getESuperTypes().add(theExpressionPackage.getReferenceSelector());
		objectSelectorExpressionEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());
		objectFilterExpressionEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());
		objectFilterExpressionEClass.getESuperTypes().add(theExpressionPackage.getLambda());
		objectVariableReferenceEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());
		objectVariableReferenceEClass.getESuperTypes().add(theExpressionPackage.getVariableReference());
		castObjectEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());
		castObjectEClass.getESuperTypes().add(theVariablePackage.getObjectVariable());
		objectSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getSwitchExpression());
		objectSwitchExpressionEClass.getESuperTypes().add(theExpressionPackage.getObjectExpression());

		// Initialize classes, features, and operations; add parameters
		initEClass(objectNavigationExpressionEClass, ObjectNavigationExpression.class, "ObjectNavigationExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectNavigationExpression_ObjectExpression(), theExpressionPackage.getObjectExpression(), null, "objectExpression", null, 1, 1, ObjectNavigationExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectSelectorExpressionEClass, ObjectSelectorExpression.class, "ObjectSelectorExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectSelectorExpression_CollectionExpression(), theExpressionPackage.getOrderedCollectionExpression(), null, "collectionExpression", null, 1, 1, ObjectSelectorExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectSelectorExpression_Operator(), theOperatorPackage.getObjectSelector(), "operator", null, 1, 1, ObjectSelectorExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectFilterExpressionEClass, ObjectFilterExpression.class, "ObjectFilterExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectFilterExpression_ObjectExpression(), theExpressionPackage.getObjectExpression(), null, "objectExpression", null, 1, 1, ObjectFilterExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectFilterExpression_Condition(), theExpressionPackage.getLogicalExpression(), null, "condition", null, 1, 1, ObjectFilterExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectVariableReferenceEClass, ObjectVariableReference.class, "ObjectVariableReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectVariableReference_Variable(), theVariablePackage.getObjectVariable(), null, "variable", null, 1, 1, ObjectVariableReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(castObjectEClass, CastObject.class, "CastObject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCastObject_ElementName(), theExpressionPackage.getElementName(), null, "elementName", null, 1, 1, CastObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCastObject_ObjectExpression(), theExpressionPackage.getObjectExpression(), null, "objectExpression", null, 1, 1, CastObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectSwitchExpressionEClass, ObjectSwitchExpression.class, "ObjectSwitchExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectSwitchExpression_ElementName(), theExpressionPackage.getElementName(), null, "elementName", null, 1, 1, ObjectSwitchExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
	}

} //ObjectPackageImpl
