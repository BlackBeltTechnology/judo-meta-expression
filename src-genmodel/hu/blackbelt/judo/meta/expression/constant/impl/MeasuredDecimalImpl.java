/**
 */
package hu.blackbelt.judo.meta.expression.constant.impl;

import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.NumericExpression;

import hu.blackbelt.judo.meta.expression.constant.Constant;
import hu.blackbelt.judo.meta.expression.constant.ConstantPackage;
import hu.blackbelt.judo.meta.expression.constant.Decimal;
import hu.blackbelt.judo.meta.expression.constant.MeasuredDecimal;

import hu.blackbelt.judo.meta.expression.impl.MeasuredExpressionImpl;

import java.lang.String;

import java.math.BigDecimal;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Measured Decimal</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl#getValue <em>Value</em>}</li>
 *   <li>{@link hu.blackbelt.judo.meta.expression.constant.impl.MeasuredDecimalImpl#getUnitName <em>Unit Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MeasuredDecimalImpl extends MeasuredExpressionImpl implements MeasuredDecimal {
	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal value = VALUE_EDEFAULT;

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
	protected MeasuredDecimalImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ConstantPackage.Literals.MEASURED_DECIMAL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(BigDecimal newValue) {
		BigDecimal oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ConstantPackage.MEASURED_DECIMAL__VALUE, oldValue, value));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ConstantPackage.MEASURED_DECIMAL__UNIT_NAME, oldUnitName, unitName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ConstantPackage.MEASURED_DECIMAL__VALUE:
				return getValue();
			case ConstantPackage.MEASURED_DECIMAL__UNIT_NAME:
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
			case ConstantPackage.MEASURED_DECIMAL__VALUE:
				setValue((BigDecimal)newValue);
				return;
			case ConstantPackage.MEASURED_DECIMAL__UNIT_NAME:
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
			case ConstantPackage.MEASURED_DECIMAL__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case ConstantPackage.MEASURED_DECIMAL__UNIT_NAME:
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
			case ConstantPackage.MEASURED_DECIMAL__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case ConstantPackage.MEASURED_DECIMAL__UNIT_NAME:
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
		if (baseClass == Constant.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == Expression.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == DataExpression.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == NumericExpression.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == hu.blackbelt.judo.meta.expression.constant.Number.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == DecimalExpression.class) {
			switch (derivedFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == Decimal.class) {
			switch (derivedFeatureID) {
				case ConstantPackage.MEASURED_DECIMAL__VALUE: return ConstantPackage.DECIMAL__VALUE;
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
		if (baseClass == Constant.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == Expression.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == DataExpression.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == NumericExpression.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == hu.blackbelt.judo.meta.expression.constant.Number.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == DecimalExpression.class) {
			switch (baseFeatureID) {
				default: return -1;
			}
		}
		if (baseClass == Decimal.class) {
			switch (baseFeatureID) {
				case ConstantPackage.DECIMAL__VALUE: return ConstantPackage.MEASURED_DECIMAL__VALUE;
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
		result.append(" (value: ");
		result.append(value);
		result.append(", unitName: ");
		result.append(unitName);
		result.append(')');
		return result.toString();
	}

} //MeasuredDecimalImpl
