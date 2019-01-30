/**
 */
package hu.blackbelt.judo.meta.expression.constant;

import hu.blackbelt.judo.meta.expression.ExpressionPackage;

import java.lang.String;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see hu.blackbelt.judo.meta.expression.constant.ConstantFactory
 * @model kind="package"
 * @generated
 */
public interface ConstantPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "constant";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://blackbelt.hu/judo/meta/expression/constant";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "const";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ConstantPackage eINSTANCE = hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl.init();

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.ConstantImpl <em>Constant</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getConstant()
	 * @generated
	 */
	int CONSTANT = 0;

	/**
	 * The number of structural features of the '<em>Constant</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTANT_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Constant</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTANT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.NumberImpl <em>Number</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.NumberImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getNumber()
	 * @generated
	 */
	int NUMBER = 1;

	/**
	 * The number of structural features of the '<em>Number</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMBER_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Number</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMBER_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.IntegerImpl <em>Integer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.IntegerImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getInteger()
	 * @generated
	 */
	int INTEGER = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTEGER__VALUE = NUMBER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Integer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTEGER_FEATURE_COUNT = NUMBER_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Integer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTEGER_OPERATION_COUNT = NUMBER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.DecimalImpl <em>Decimal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.DecimalImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getDecimal()
	 * @generated
	 */
	int DECIMAL = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DECIMAL__VALUE = NUMBER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Decimal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DECIMAL_FEATURE_COUNT = NUMBER_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Decimal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DECIMAL_OPERATION_COUNT = NUMBER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.BooleanImpl <em>Boolean</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.BooleanImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getBoolean()
	 * @generated
	 */
	int BOOLEAN = 4;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOOLEAN__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Boolean</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOOLEAN_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Boolean</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOOLEAN_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.StringImpl <em>String</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.StringImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getString()
	 * @generated
	 */
	int STRING = 5;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>String</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>String</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.LiteralImpl <em>Literal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.LiteralImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getLiteral()
	 * @generated
	 */
	int LITERAL = 6;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LITERAL__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LITERAL_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LITERAL_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.InstanceImpl <em>Instance</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.InstanceImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getInstance()
	 * @generated
	 */
	int INSTANCE = 7;

	/**
	 * The feature id for the '<em><b>Element Name</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INSTANCE__ELEMENT_NAME = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Instance</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INSTANCE_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Instance</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INSTANCE_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.CustomDataImpl <em>Custom Data</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.CustomDataImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getCustomData()
	 * @generated
	 */
	int CUSTOM_DATA = 8;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_DATA__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Custom Data</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_DATA_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Custom Data</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_DATA_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl <em>Measured Decimal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getMeasuredDecimal()
	 * @generated
	 */
	int MEASURED_DECIMAL = 9;

	/**
	 * The feature id for the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_DECIMAL__MEASURE = ExpressionPackage.MEASURED_EXPRESSION__MEASURE;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_DECIMAL__VALUE = ExpressionPackage.MEASURED_EXPRESSION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Unit Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_DECIMAL__UNIT_NAME = ExpressionPackage.MEASURED_EXPRESSION_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Measured Decimal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_DECIMAL_FEATURE_COUNT = ExpressionPackage.MEASURED_EXPRESSION_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Measured Decimal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_DECIMAL_OPERATION_COUNT = ExpressionPackage.MEASURED_EXPRESSION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl <em>Measured Integer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getMeasuredInteger()
	 * @generated
	 */
	int MEASURED_INTEGER = 10;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_INTEGER__VALUE = INTEGER__VALUE;

	/**
	 * The feature id for the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_INTEGER__MEASURE = INTEGER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Unit Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_INTEGER__UNIT_NAME = INTEGER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Measured Integer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_INTEGER_FEATURE_COUNT = INTEGER_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Measured Integer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEASURED_INTEGER_OPERATION_COUNT = INTEGER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.DateImpl <em>Date</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.DateImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getDate()
	 * @generated
	 */
	int DATE = 11;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Date</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Date</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.TimestampImpl <em>Timestamp</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.TimestampImpl
	 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getTimestamp()
	 * @generated
	 */
	int TIMESTAMP = 12;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMP__VALUE = CONSTANT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Timestamp</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMP_FEATURE_COUNT = CONSTANT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Timestamp</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMP_OPERATION_COUNT = CONSTANT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Constant <em>Constant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Constant</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Constant
	 * @generated
	 */
	EClass getConstant();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Number <em>Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Number</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Number
	 * @generated
	 */
	EClass getNumber();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Integer <em>Integer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Integer</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Integer
	 * @generated
	 */
	EClass getInteger();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Integer#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Integer#getValue()
	 * @see #getInteger()
	 * @generated
	 */
	EAttribute getInteger_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Decimal <em>Decimal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Decimal</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Decimal
	 * @generated
	 */
	EClass getDecimal();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Decimal#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Decimal#getValue()
	 * @see #getDecimal()
	 * @generated
	 */
	EAttribute getDecimal_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Boolean <em>Boolean</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Boolean</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Boolean
	 * @generated
	 */
	EClass getBoolean();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Boolean#isValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Boolean#isValue()
	 * @see #getBoolean()
	 * @generated
	 */
	EAttribute getBoolean_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.String <em>String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.String
	 * @generated
	 */
	EClass getString();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.String#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.String#getValue()
	 * @see #getString()
	 * @generated
	 */
	EAttribute getString_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Literal <em>Literal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Literal</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Literal
	 * @generated
	 */
	EClass getLiteral();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Literal#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Literal#getValue()
	 * @see #getLiteral()
	 * @generated
	 */
	EAttribute getLiteral_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Instance <em>Instance</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Instance</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Instance
	 * @generated
	 */
	EClass getInstance();

	/**
	 * Returns the meta object for the reference '{@link hu.blackbelt.judo.meta.expression.constant.Instance#getElementName <em>Element Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Element Name</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Instance#getElementName()
	 * @see #getInstance()
	 * @generated
	 */
	EReference getInstance_ElementName();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.CustomData <em>Custom Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Custom Data</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.CustomData
	 * @generated
	 */
	EClass getCustomData();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.CustomData#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.CustomData#getValue()
	 * @see #getCustomData()
	 * @generated
	 */
	EAttribute getCustomData_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal <em>Measured Decimal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Measured Decimal</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal
	 * @generated
	 */
	EClass getMeasuredDecimal();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal#getUnitName <em>Unit Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Name</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal#getUnitName()
	 * @see #getMeasuredDecimal()
	 * @generated
	 */
	EAttribute getMeasuredDecimal_UnitName();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.MeasuredInteger <em>Measured Integer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Measured Integer</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.MeasuredInteger
	 * @generated
	 */
	EClass getMeasuredInteger();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.MeasuredInteger#getUnitName <em>Unit Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Name</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.MeasuredInteger#getUnitName()
	 * @see #getMeasuredInteger()
	 * @generated
	 */
	EAttribute getMeasuredInteger_UnitName();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Date <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Date</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Date
	 * @generated
	 */
	EClass getDate();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Date#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Date#getValue()
	 * @see #getDate()
	 * @generated
	 */
	EAttribute getDate_Value();

	/**
	 * Returns the meta object for class '{@link hu.blackbelt.judo.meta.expression.constant.Timestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Timestamp</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Timestamp
	 * @generated
	 */
	EClass getTimestamp();

	/**
	 * Returns the meta object for the attribute '{@link hu.blackbelt.judo.meta.expression.constant.Timestamp#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see hu.blackbelt.judo.meta.expression.constant.Timestamp#getValue()
	 * @see #getTimestamp()
	 * @generated
	 */
	EAttribute getTimestamp_Value();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ConstantFactory getConstantFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.ConstantImpl <em>Constant</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getConstant()
		 * @generated
		 */
		EClass CONSTANT = eINSTANCE.getConstant();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.NumberImpl <em>Number</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.NumberImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getNumber()
		 * @generated
		 */
		EClass NUMBER = eINSTANCE.getNumber();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.IntegerImpl <em>Integer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.IntegerImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getInteger()
		 * @generated
		 */
		EClass INTEGER = eINSTANCE.getInteger();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTEGER__VALUE = eINSTANCE.getInteger_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.DecimalImpl <em>Decimal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.DecimalImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getDecimal()
		 * @generated
		 */
		EClass DECIMAL = eINSTANCE.getDecimal();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DECIMAL__VALUE = eINSTANCE.getDecimal_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.BooleanImpl <em>Boolean</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.BooleanImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getBoolean()
		 * @generated
		 */
		EClass BOOLEAN = eINSTANCE.getBoolean();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BOOLEAN__VALUE = eINSTANCE.getBoolean_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.StringImpl <em>String</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.StringImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getString()
		 * @generated
		 */
		EClass STRING = eINSTANCE.getString();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING__VALUE = eINSTANCE.getString_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.LiteralImpl <em>Literal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.LiteralImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getLiteral()
		 * @generated
		 */
		EClass LITERAL = eINSTANCE.getLiteral();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LITERAL__VALUE = eINSTANCE.getLiteral_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.InstanceImpl <em>Instance</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.InstanceImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getInstance()
		 * @generated
		 */
		EClass INSTANCE = eINSTANCE.getInstance();

		/**
		 * The meta object literal for the '<em><b>Element Name</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INSTANCE__ELEMENT_NAME = eINSTANCE.getInstance_ElementName();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.CustomDataImpl <em>Custom Data</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.CustomDataImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getCustomData()
		 * @generated
		 */
		EClass CUSTOM_DATA = eINSTANCE.getCustomData();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CUSTOM_DATA__VALUE = eINSTANCE.getCustomData_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl <em>Measured Decimal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getMeasuredDecimal()
		 * @generated
		 */
		EClass MEASURED_DECIMAL = eINSTANCE.getMeasuredDecimal();

		/**
		 * The meta object literal for the '<em><b>Unit Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEASURED_DECIMAL__UNIT_NAME = eINSTANCE.getMeasuredDecimal_UnitName();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl <em>Measured Integer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getMeasuredInteger()
		 * @generated
		 */
		EClass MEASURED_INTEGER = eINSTANCE.getMeasuredInteger();

		/**
		 * The meta object literal for the '<em><b>Unit Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MEASURED_INTEGER__UNIT_NAME = eINSTANCE.getMeasuredInteger_UnitName();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.DateImpl <em>Date</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.DateImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getDate()
		 * @generated
		 */
		EClass DATE = eINSTANCE.getDate();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATE__VALUE = eINSTANCE.getDate_Value();

		/**
		 * The meta object literal for the '{@link hu.blackbelt.judo.meta.expression.constant.impl.TimestampImpl <em>Timestamp</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.TimestampImpl
		 * @see hu.blackbelt.judo.meta.expression.constant.impl.ConstantPackageImpl#getTimestamp()
		 * @generated
		 */
		EClass TIMESTAMP = eINSTANCE.getTimestamp();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESTAMP__VALUE = eINSTANCE.getTimestamp_Value();

	}

} //ConstantPackage
