/**
 */
package hu.blackbelt.judo.meta.expression.constant;

import hu.blackbelt.judo.meta.expression.StringExpression;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>String</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.String#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getString()
 * @model
 * @generated
 */
public interface String extends Constant, StringExpression {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(java.lang.String)
	 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getString_Value()
	 * @model required="true"
	 * @generated
	 */
	java.lang.String getValue();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.constant.String#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(java.lang.String value);

} // String
