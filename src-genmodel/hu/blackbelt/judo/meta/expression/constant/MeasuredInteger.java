/**
 */
package hu.blackbelt.judo.meta.expression.constant;

import hu.blackbelt.judo.meta.expression.MeasuredExpression;

import java.lang.String;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Measured Integer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.MeasuredInteger#getUnitName <em>Unit Name</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getMeasuredInteger()
 * @model
 * @generated
 */
public interface MeasuredInteger extends hu.blackbelt.judo.meta.expression.constant.Integer, MeasuredExpression {
	/**
	 * Returns the value of the '<em><b>Unit Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unit Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit Name</em>' attribute.
	 * @see #setUnitName(String)
	 * @see hu.blackbelt.judo.meta.expression.constant.ConstantPackage#getMeasuredInteger_UnitName()
	 * @model required="true"
	 * @generated
	 */
	String getUnitName();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.constant.MeasuredInteger#getUnitName <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit Name</em>' attribute.
	 * @see #getUnitName()
	 * @generated
	 */
	void setUnitName(String value);

} // MeasuredInteger
