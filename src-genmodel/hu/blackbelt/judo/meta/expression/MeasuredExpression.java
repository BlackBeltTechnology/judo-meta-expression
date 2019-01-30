/**
 */
package hu.blackbelt.judo.meta.expression;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Measured Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.MeasuredExpression#getMeasure <em>Measure</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.ExpressionPackage#getMeasuredExpression()
 * @model abstract="true"
 * @generated
 */
public interface MeasuredExpression extends EObject {
	/**
	 * Returns the value of the '<em><b>Measure</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Measure</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Measure</em>' reference.
	 * @see #setMeasure(MeasureName)
	 * @see hu.blackbelt.judo.meta.expression.ExpressionPackage#getMeasuredExpression_Measure()
	 * @model
	 * @generated
	 */
	MeasureName getMeasure();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.MeasuredExpression#getMeasure <em>Measure</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Measure</em>' reference.
	 * @see #getMeasure()
	 * @generated
	 */
	void setMeasure(MeasureName value);

} // MeasuredExpression
