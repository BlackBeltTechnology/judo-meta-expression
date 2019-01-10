/**
 */
package hu.blackbelt.judo.meta.expression.logical;

import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.VariableReference;

import hu.blackbelt.judo.meta.expression.variable.LogicalVariable;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Variable Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.logical.LogicalVariableReference#getVariable <em>Variable</em>}</li>
 * </ul>
 *
 * @see hu.blackbelt.judo.meta.expression.logical.LogicalPackage#getLogicalVariableReference()
 * @model
 * @generated
 */
public interface LogicalVariableReference extends LogicalExpression, VariableReference {
	/**
	 * Returns the value of the '<em><b>Variable</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variable</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variable</em>' reference.
	 * @see #setVariable(LogicalVariable)
	 * @see hu.blackbelt.judo.meta.expression.logical.LogicalPackage#getLogicalVariableReference_Variable()
	 * @model required="true"
	 * @generated
	 */
	LogicalVariable getVariable();

	/**
	 * Sets the value of the '{@link hu.blackbelt.judo.meta.expression.logical.LogicalVariableReference#getVariable <em>Variable</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Variable</em>' reference.
	 * @see #getVariable()
	 * @generated
	 */
	void setVariable(LogicalVariable value);

} // LogicalVariableReference
