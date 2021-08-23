package hu.blackbelt.judo.meta.expression.builder.jql.asm;

import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.ATTRIBUTE;
import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.BindingType.RELATION;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.buildMeasureModel;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEpsilonValidator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.measure.Measure;
import hu.blackbelt.judo.meta.measure.Unit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsmJqlExpressionBindingTest extends ExecutionContextOnAsmTest {

    private JqlExpressionBuilder<EClassifier, EDataType, EEnum, EClass, EAttribute, EReference, EClass, EAttribute, EReference, EClassifier, Measure, Unit> expressionBuilder;

    private ExpressionModelResourceSupport expressionModelResourceSupport;

    @BeforeEach
    void setUp() throws Exception {

        super.setUp();

        expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("urn:test.judo-meta-expression"))
                .build();

        measureModel = buildMeasureModel()
                .name(asmModel.getName())
                .build();

        expressionBuilder = new JqlExpressionBuilder<>(modelAdapter, expressionModelResourceSupport.getResource());
    }

    private Expression createExpression(final EClass clazz, final String jqlExpressionString) {
        final Expression expression = expressionBuilder.createExpression(clazz, jqlExpressionString);
        assertThat(expression, notNullValue());
        return expression;
    }

    private Expression createGetterExpression(EClass clazz, String jqlExpressionString, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Expression expression = createExpression(clazz, jqlExpressionString);
        expressionBuilder.createGetterBinding(clazz, expression, feature, bindingType);
        return expression;
    }

    private EClass findBase(String entityName) {
        return asmUtils.all(EClass.class).filter(c -> entityName.equals(c.getName())).findAny().orElseThrow(IllegalArgumentException::new);
    }

    @Test
    void testAttributeBinding() throws Exception {

        EClass order = findBase("Order");

        createGetterExpression(order, "self.shipper.companyName", "orderDate", ATTRIBUTE); //string expr to time stamp
        createGetterExpression(order, "self.orderDate", "freight", ATTRIBUTE); //time stamp expr to string 

        EClass internationalOrder = findBase("InternationalOrder");
        createGetterExpression(internationalOrder, "self.exciseTax", "customsDescription", ATTRIBUTE); // decimal expr to string

        EClass product = findBase("Product");
        createGetterExpression(product, "`2019-12-31`", "productName", ATTRIBUTE); //date expr to string
        createGetterExpression(product, "demo::types::Countries#AT", "unitPrice", ATTRIBUTE); //enum expr to decimal
        createGetterExpression(product, "self.discounted", "weight", ATTRIBUTE); // boolean expr to measured

        EClass orderDetail = findBase("OrderDetail");
        createGetterExpression(orderDetail, "self.quantity", "price", ATTRIBUTE); // integer expr to double should be ok

        ExpressionModel expressionModel = ExpressionModel.buildExpressionModel()
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .name(asmModel.getName())
                .build();
        ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(new Slf4jLog(), asmModel, measureModel, expressionModel, ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(),
                ImmutableList.of(
                        "StringExpressionMatchesBinding|Attribute named orderDate must be string type, because the assigned expression evaluates to a string.",
                        "TimestampExpressionMatchesBinding|Attribute named freight must be timestamp type, because the assigned expression evaluates to a timestamp.",
                        "DecimalExpressionMatchesBinding|Attribute named customsDescription must be decimal type, because the assigned expression evaluates to a decimal.",
                        "DateExpressionMatchesBinding|Attribute named productName must be date type, because the assigned expression evaluates to a date.",
                        "EnumerationExpressionMatchesBinding|Attribute named unitPrice must be enumeration type, because the assigned expression evaluates to an enumeration.",
                        "BooleanExpressionMatchesBinding|Attribute named weight must be boolean type, because the assigned expression evaluates to a boolean."),
                Collections.emptyList());
    }

    @Test
    void testReferenceBinding() throws Exception {

        EClass order = findBase("Order");
        createGetterExpression(order, "self.orderDetails", "category", RELATION);

        EClass category = findBase("Category");
        createGetterExpression(category, "self.owner", "products", RELATION);

        ExpressionModel expressionModel = ExpressionModel.buildExpressionModel()
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .name(asmModel.getName())
                .build();
        ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(new Slf4jLog(), asmModel, measureModel, expressionModel, ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(),
                ImmutableList.of(
                        "CollectionExpressionMatchesBinding|Reference named category refers to an object but the assigned expression evaluates to a collection.",
                        "ReferenceExpressionMatchesBinding|Reference named category does not match the type of the assigned expression",
                        "ObjectExpressionMatchesBinding|Reference named products refers to a collection but the assigned expression evaluates to an object.",
                        "ReferenceExpressionMatchesBinding|Reference named products does not match the type of the assigned expression"),
                Collections.emptyList());
    }
}
