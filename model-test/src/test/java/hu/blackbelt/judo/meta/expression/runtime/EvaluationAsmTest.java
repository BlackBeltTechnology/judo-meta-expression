package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.operator.NumericComparator;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAttribute;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newDecimalComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

public class EvaluationAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        modelContexts.clear();
    }

    protected Resource getExpressionResource() {
        final ResourceSet expressionModelResourceSet = ExpressionModelResourceSupport.createExpressionResourceSet();

        final String createdSourceModelName = "urn:expression.judo-meta-expression";
        final Resource expressionResource = expressionModelResourceSet.createResource(
                URI.createURI(createdSourceModelName));

        final TypeName orderType = newTypeNameBuilder().withNamespace("demo::entities").withName("Order").build();
        expressionResource.getContents().add(orderType);
        expressionResource.getContents().add(newTypeNameBuilder().withNamespace("demo::entities").withName("OrderDetail").build());


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

        final ObjectVariable od = orderDetails.createIterator("od", modelAdapter, expressionResource);

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

        final CollectionVariable discountedItems = newCollectionFilterExpressionBuilder()
                .withCollectionExpression(newCollectionVariableReferenceBuilder()
                        .withVariable(orderDetails)
                        .build())
                .withCondition(newDecimalComparisonBuilder()
                        .withLeft(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(od)
                                        .build())
                                .withAttributeName("discount")
                                .build())
                        .withOperator(NumericComparator.GREATER_THAN)
                        .withRight(newIntegerConstantBuilder()
                                .withValue(BigInteger.ZERO)
                                .build())
                        .build())
                .build();

        final ObjectVariable di = discountedItems.createIterator("di", modelAdapter, expressionResource);

        final StringExpression discountedCategoryName = newStringAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(di)
                        .build())
                .withAttributeName("categoryName")
                .build();

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

        expressionResource.getContents().addAll(Arrays.asList(orderType, order
                , orderDate
                , shipperName
                , orderDetails
//                , price
//                , itemsOfDiscountedProducts
//                , totalPrice
//                , itemCount
//                , categories
//                , categoryName
//                , categoryDescription
//                , completedDate
                , discountedItems
                , discountedCategoryName
//                , discountedProductName
        ));

        return expressionResource;
    }

    @Test
    void test() throws Exception {
    	ExpressionEpsilonValidator.validateExpression(log, 
        		modelContexts ,
        		ExpressionEpsilonValidator.calculateExpressionValidationScriptURI(), 
        		modelAdapter);
    }
}
