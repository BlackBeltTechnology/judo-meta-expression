/**
 */
package hu.blackbelt.judo.meta.expression.constant;

import hu.blackbelt.judo.meta.expression.DateExpression;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Date</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.Date#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getDate()
 * @model
 * @generated
 */
public interface Date extends Constant, DateExpression {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(java.util.Date)
	 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getDate_Value()
	 * @model required="true"
	 * @generated
	 */
	java.util.Date getValue();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.constant.Date#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(java.util.Date value);

} // Date
