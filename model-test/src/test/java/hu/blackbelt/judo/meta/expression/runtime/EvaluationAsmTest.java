package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.asm.ExpressionEpsilonValidatorOnAsm;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionNavigationFromObjectExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

@Disabled
public class EvaluationAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected ExpressionModel getExpressionModel() {
        final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                .uri(URI.createURI("expression:test"))
                .build();

        final TypeName orderType = newTypeNameBuilder().withNamespace("demo::entities").withName("Order").build();
        expressionModelResourceSupport.addContent(orderType);
        expressionModelResourceSupport.addContent(newTypeNameBuilder().withNamespace("demo::entities").withName("OrderDetail").build());

        final ObjectVariable order = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .build();

        final TimestampAttribute orderDate = newTimestampAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withAttributeName("orderDate")
                .build();

        final StringExpression shipperName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(order)
                                .build())
                        .withName("s")
                        .withReferenceName("shipper")
                        .build())
                .withAttributeName("companyName")
                .build();

        final CollectionNavigationFromObjectExpression orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .build();

//        final ObjectVariable od = orderDetails.createIterator("od", modelAdapter, expressionModelResourceSupport.getResource());

//        final CollectionVariable itemsOfDiscountedProducts = newCollectionFilterExpressionBuilder()
//                .withCollectionExpression(newCollectionVariableReferenceBuilder()
//                        .withVariable(orderDetails)
//                        .build())
//                .withCondition(
//                        newLogicalAttributeBuilder()
//                                .withObjectExpression(newObjectNavigationExpressionBuilder()
//                                        .withObjectExpression(newObjectVariableReferenceBuilder()
//                                                .withVariable(od)
//                                                .build())
//                                        .withReferenceName("product")
//                                        .build())
//                                .withAttributeName("discounted")
//                                .build())
//                .build();

//        final NumericExpression price = newDecimalAttributeBuilder()
//                .withObjectExpression(newObjectVariableReferenceBuilder()
//                        .withVariable(od)
//                        .build())
//                .withAttributeName("price")
//                .withAlias("price")
//                .build();

//        final NumericExpression price = newDecimalAritmeticExpressionBuilder()
//                .withLeft(newDecimalAritmeticExpressionBuilder()
//                        .withLeft(newIntegerAttributeBuilder()
//                                .withObjectExpression(newObjectVariableReferenceBuilder()
//                                        .withVariable(od)
//                                        .build())
//                                .withAttributeName("quantity")
//                                .build())
//                        .withOperator(DecimalOperator.MULTIPLY)
//                        .withRight(newDecimalAttributeBuilder()
//                                .withObjectExpression(newObjectVariableReferenceBuilder()
//                                        .withVariable(od)
//                                        .build())
//                                .withAttributeName("unitPrice")
//                                .build())
//                        .build())
//                .withOperator(DecimalOperator.MULTIPLY)
//                .withRight(newDecimalAritmeticExpressionBuilder()
//                        .withLeft(newIntegerConstantBuilder()
//                                .withValue(BigInteger.ONE)
//                                .build())
//                        .withOperator(DecimalOperator.SUBSTRACT)
//                        .withRight(newDecimalAttributeBuilder()
//                                .withObjectExpression(newObjectVariableReferenceBuilder()
//                                        .withVariable(od)
//                                        .build())
//                                .withAttributeName("discount")
//                                .build())
//                        .build())
//                .withAlias("price")
//                .build();
//
//        final DecimalAggregatedExpression totalPrice = newDecimalAggregatedExpressionBuilder()
//                .withCollectionExpression(newCollectionVariableReferenceBuilder()
//                        .withVariable(orderDetails)
//                        .build())
//                .withOperator(DecimalAggregator.SUM)
//                .withExpression(price)
//                .withAlias("totalPrice")
//                .build();
//
//        final IntegerExpression itemCount = newCountExpressionBuilder()
//                .withCollectionExpression(newCollectionVariableReferenceBuilder()
//                        .withVariable(itemsOfDiscountedProducts)
//                        .build())
//                .withAlias("numberOfItems")
//                .build();
//
//        final CollectionVariable categories = newObjectNavigationFromCollectionExpressionBuilder()
//                .withCollectionExpression(newObjectNavigationFromCollectionExpressionBuilder()
//                        .withCollectionExpression(newCollectionVariableReferenceBuilder()
//                                .withVariable(orderDetails)
//                                .build())
//                        .withReferenceName("product")
//                        .withName("p")
//                        .build())
//                .withReferenceName("category")
//                .withName("c")
//                .build();
//
//        final StringExpression categoryName = newStringAttributeBuilder()
//                .withObjectExpression(newObjectVariableReferenceBuilder()
//                        .withVariable(categories)
//                        .build())
//                .withAttributeName("categoryName")
//                .build();
//
//        final StringExpression categoryDescription = newStringAttributeBuilder()
//                .withObjectExpression(newObjectVariableReferenceBuilder()
//                        .withVariable(categories)
//                        .build())
//                .withAttributeName("description")
//                .build();
//
//        final TimestampExpression completedDate = newTimestampAdditionExpressionBuilder()
//                .withTimestamp(newTimestampAttributeBuilder()
//                        .withObjectExpression(newObjectVariableReferenceBuilder()
//                                .withVariable(order)
//                                .build())
//                        .withAttributeName("shippedDate")
//                        .build())
//                .withOperator(TimestampDurationOperator.ADD)
//                .withDuration(newMeasuredIntegerBuilder()
//                        .withValue(BigInteger.ONE)
//                        .withUnitName("week")
//                        .build())
//                .withAlias("completedDate")
//                .build();
//
//        final CollectionFilterExpression discountedItems = newCollectionFilterExpressionBuilder()
//                .withCollectionExpression(newCollectionVariableReferenceBuilder()
//                        .withVariable(orderDetails)
//                        .build())
//                .withCondition(newDecimalComparisonBuilder()
//                        .withLeft(newDecimalAttributeBuilder()
//                                .withObjectExpression(newObjectVariableReferenceBuilder()
//                                        .withVariable(od)
//                                        .build())
//                                .withAttributeName("discount")
//                                .build())
//                        .withOperator(NumericComparator.GREATER_THAN)
//                        .withRight(newIntegerConstantBuilder()
//                                .withValue(BigInteger.ZERO)
//                                .build())
//                        .build())
//                .build();
//
////        final ObjectVariable di = discountedItems.createIterator("di", modelAdapter, expressionResource);
//
//        final StringExpression discountedCategoryName = newStringAttributeBuilder()
//                .withObjectExpression(newObjectVariableReferenceBuilder()
//                        .withVariable(od)
//                        .build())
//                .withAttributeName("categoryName")
//                .build();

//        final StringExpression discountedProductName = newStringAttributeBuilder()
//                .withObjectExpression(newObjectNavigationExpressionBuilder()
//                        .withObjectExpression(newObjectVariableReferenceBuilder()
//                                .withVariable(discountedItems)
//                                .build())
//                        .withReferenceName("product")
//                        .build())
//                .withAttributeName("productName")
//                .withAlias("product")
//                .build();

//        final NumericExpression number = newIntegerAritmeticExpressionBuilder()
//                .withLeft(newIntegerConstantBuilder().withValue(BigInteger.valueOf(2)).build())
//                .withOperator(IntegerOperator.MULTIPLY)
//                .withRight(newIntegerConstantBuilder().withValue(BigInteger.valueOf(3)).build())
//                .withAlias("result")
//                .build();
//        expressionResource.getContents().addAll(Arrays.asList(number));

        expressionModelResourceSupport.addContent(orderType);
        expressionModelResourceSupport.addContent(order);
        expressionModelResourceSupport.addContent(orderDate);
        expressionModelResourceSupport.addContent(shipperName);
        expressionModelResourceSupport.addContent(orderDetails);
        //expressionModelResourceSupport.addContent(price);
        //expressionModelResourceSupport.addContent(itemsOfDiscountedProducts);
        //expressionModelResourceSupport.addContent(totalPrice);
        //expressionModelResourceSupport.addContent(itemCount);
        //expressionModelResourceSupport.addContent(categories);
        //expressionModelResourceSupport.addContent(categoryName);
        //expressionModelResourceSupport.addContent(categoryDescription);
        //expressionModelResourceSupport.addContent(completedDate);
//        expressionModelResourceSupport.addContent(discountedItems);
//        expressionModelResourceSupport.addContent(discountedCategoryName);
        //expressionModelResourceSupport.addContent(discountedProductName);

        return ExpressionModel.buildExpressionModel()
                .name("expression")
                .expressionModelResourceSupport(expressionModelResourceSupport)
                .build();
    }

    @Test
    void test() throws Exception {
        ExpressionEpsilonValidatorOnAsm.validateExpressionOnAsm(log,
                asmModel, measureModel, expressionModel,
                ExpressionEpsilonValidator.calculateExpressionValidationScriptURI());
    }
}
