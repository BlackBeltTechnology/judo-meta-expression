/**
 */
package hu.blackbelt.judo.meta.expression.numeric;

import hu.blackbelt.judo.meta.expression.DecimalExpression;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Decimal Opposite Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.numeric.DecimalOppositeExpression#getExpression <em>Expression</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.numeric.NumericPackage#getDecimalOppositeExpression()
 * @model
 * @generated
 */
public interface DecimalOppositeExpression extends DecimalExpression {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Expression</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expression</em>' containment reference.
	 * @see #setExpression(DecimalExpression)
	 * @see hu.blackbelt.judo.meta.expression.numeric.NumericPackage#getDecimalOppositeExpression_Expression()
	 * @model containment="true" required="true"
	 * @generated
	 */
	DecimalExpression getExpression();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.numeric.DecimalOppositeExpression#getExpression <em>Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' containment reference.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(DecimalExpression value);

} // DecimalOppositeExpression
