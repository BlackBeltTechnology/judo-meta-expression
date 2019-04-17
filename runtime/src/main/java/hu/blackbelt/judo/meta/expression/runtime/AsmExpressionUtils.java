package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EReference;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class AsmExpressionUtils {

    public static Optional<EReference> getReferenceType(final EClass eClass, final String name) {
        return eClass.getEAllReferences().stream().filter(r -> Objects.equals(name, r.getName())).findFirst();
    }

    public static boolean isCollection(final EReference eReference) {
        return eReference.isMany();
    }

    public static EClassifier getTarget(final EReference eReference) {
        return eReference.getEType();
    }

    public static Optional<EAttribute> getAttribute(final EClass eClass, final String name) {
        return eClass.getEAllAttributes().stream().filter(a -> Objects.equals(name, a.getName())).findFirst();
    }

    public static Optional<EDataType> getAttributeType(final EClass eClass, final String attributeName) {
        final Optional<EAttribute> attribute = getAttribute(eClass, attributeName);

        if (attribute.isPresent()) {
            return Optional.of(attribute.get().getEAttributeType());
        } else {
            return Optional.empty();
        }
    }

    public static Collection<EClass> getSuperTypes(final EClass eClass) {
        return eClass.getEAllSuperTypes();
    }

    public static boolean isCustom(final EDataType eDataType) {
        // TODO - support registering custom data tp
        return !AsmUtils.isNumeric(eDataType) && !AsmUtils.isBoolean(eDataType) && !AsmUtils.isString(eDataType) && !AsmUtils.isDate(eDataType) && !AsmUtils.isTimestamp(eDataType) && !AsmUtils.isEnumeration(eDataType);
    }

    public static boolean contains(final EEnum eEnum, final String name) {
        return eEnum.getELiterals().stream().filter(l -> Objects.equals(name, l.getLiteral())).count() > 0;
    }

    public static String getName(final ENamedElement eNamedElement) {
        return eNamedElement.getName();
    }
}
