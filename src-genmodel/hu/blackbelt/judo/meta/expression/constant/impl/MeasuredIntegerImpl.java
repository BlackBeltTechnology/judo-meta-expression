/**
 */
package hu.blackbelt.judo.meta.expression.constant.impl;

import hu.blackbelt.judo.meta.expression.ExpressionPackage;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.MeasuredExpression;

import hu.blackbelt.judo.meta.expression.constant.ConstantPackage;
import hu.blackbelt.judo.meta.expression.constant.MeasuredInteger;

import java.lang.String;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Measured Integer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl#getMeasure <em>Measure</em>}</li>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredIntegerImpl#getUnitName <em>Unit Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MeasuredIntegerImpl extends IntegerImpl implements MeasuredInteger {
	/**
	 * The cached value of the '{@link #getMeasure() <em>Measure</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMeasure()
	 * @generated
	 * @ordered
	 */
	protected MeasureName measure;

	/**
	 * The default value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected String unitName = UNIT_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MeasuredIntegerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ConstantPackage.Literals.MEASURED_INTEGER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MeasureName getMeasure() {
		if (measure != null && measure.eIsProxy()) {
			InternalEObject oldMeasure = (InternalEObject)measure;
			measure = (MeasureName)eResolveProxy(oldMeasure);
			if (measure != oldMeasure) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ConstantPackage.MEASURED_INTEGER__MEASURE, oldMeasure, measure));
			}
		}
		return measure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MeasureName basicGetMeasure() {
		return measure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMeasure(MeasureName newMeasure) {
		MeasureName oldMeasure = measure;
		measure = newMeasure;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ConstantPackage.MEASURED_INTEGER__MEASURE, oldMeasure, measure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getUnitName() {
		return unitName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUnitName(String newUnitName) {
		String oldUnitName = unitName;
		unitName = newUnitName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ConstantPackage.MEASURED_INTEGER__UNIT_NAME, oldUnitName, unitName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ConstantPackage.MEASURED_INTEGER__MEASURE:
				if (resolve) return getMeasure();
				return basicGetMeasure();
			case ConstantPackage.MEASURED_INTEGER__UNIT_NAME:
				return getUnitName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ConstantPackage.MEASURED_INTEGER__MEASURE:
				setMeasure((MeasureName)newValue);
				return;
			case ConstantPackage.MEASURED_INTEGER__UNIT_NAME:
				setUnitName((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ConstantPackage.MEASURED_INTEGER__MEASURE:
				setMeasure((MeasureName)null);
				return;
			case ConstantPackage.MEASURED_INTEGER__UNIT_NAME:
				setUnitName(UNIT_NAME_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ConstantPackage.MEASURED_INTEGER__MEASURE:
				return measure != null;
			case ConstantPackage.MEASURED_INTEGER__UNIT_NAME:
				return UNIT_NAME_EDEFAULT == null ? unitName != null : !UNIT_NAME_EDEFAULT.equals(unitName);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MeasuredExpression.class) {
			switch (derivedFeatureID) {
				case ConstantPackage.MEASURED_INTEGER__MEASURE: return ExpressionPackage.MEASURED_EXPRESSION__MEASURE;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MeasuredExpression.class) {
			switch (baseFeatureID) {
				case ExpressionPackage.MEASURED_EXPRESSION__MEASURE: return ConstantPackage.MEASURED_INTEGER__MEASURE;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (unitName: ");
		result.append(unitName);
		result.append(')');
		return result.toString();
	}

} //MeasuredIntegerImpl