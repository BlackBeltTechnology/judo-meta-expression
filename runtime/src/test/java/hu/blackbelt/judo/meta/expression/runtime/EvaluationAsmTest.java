package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAggregatedExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.DecimalComparator;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.TimestampDurationOperator;
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

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;

public class EvaluationAsmTest extends ExecutionContextOnAsmTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
    }

    protected Resource getExpressionResource() {
        final ResourceSet expressionModelResourceSet = ExpressionModelLoader.createExpressionResourceSet();

        final String createdSourceModelName = "urn:expression.judo-meta-expression";
        final Resource expressionResource = expressionModelResourceSet.createResource(
                URI.createURI(createdSourceModelName));

        final TypeName orderType = newTypeNameBuilder().withNamespace("northwind.entities").withName("Order").build();
        final TypeName orderDetailType = newTypeNameBuilder().withNamespace("northwind.entities").withName("OrderDetail").build();

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
                .withAlias("shipperName")
                .build();

        final CollectionNavigationFromObjectExpression orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .withAlias("items")
                .withName("od")
                .build();

        final CollectionNavigationFromObjectExpression itemsOfDiscountedProductsBase = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();
        final CollectionVariable itemsOfDiscountedProducts = newCollectionFilterExpressionBuilder()
                .withCollectionExpression(itemsOfDiscountedProductsBase)
                .withCondition(
                        newLogicalAttributeBuilder()
                                .withObjectExpression(newObjectNavigationExpressionBuilder()
                                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                                .withVariable(itemsOfDiscountedProductsBase)
                                                .build())
                                        .withReferenceName("product")
                                        .build())
                                .withAttributeName("discounted")
                                .build())
                .withName("d")
                .build();

        final ObjectVariable orderDetail = newInstanceBuilder()
                .withElementName(orderDetailType)
                .withName("self")
                .withDefinition(newInstanceReferenceBuilder()
                        .withVariable(orderDetails)
                        .build())
                .build();

        final NumericExpression price = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetail)
                                        .build())
                                .withAttributeName("quantity")
                                .build())
                        .withOperator(DecimalOperator.MULTIPLY)
                        .withRight(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetail)
                                        .build())
                                .withAttributeName("unitPrice")
                                .build())
                        .build())
                .withOperator(DecimalOperator.MULTIPLY)
                .withRight(newDecimalAritmeticExpressionBuilder()
                        .withLeft(newIntegerConstantBuilder()
                                .withValue(BigInteger.valueOf(1))
                                .build())
                        .withOperator(DecimalOperator.SUBSTRACT)
                        .withRight(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetail)
                                        .build())
                                .withAttributeName("discount")
                                .build())
                        .build())
                .withAlias("price")
                .build();

        final DecimalAggregatedExpression totalPrice = newDecimalAggregatedExpressionBuilder()
                .withCollectionExpression(newCollectionVariableReferenceBuilder()
                        .withVariable(orderDetails)
                        .build())
                .withOperator(DecimalAggregator.SUM)
                .withExpression(price)
                .withAlias("totalPrice")
                .build();

        final IntegerExpression itemCount = newCountExpressionBuilder()
                .withCollectionExpression(newCollectionVariableReferenceBuilder()
                        .withVariable(itemsOfDiscountedProducts)
                        .build())
                .withAlias("numberOfItems")
                .build();

        final CollectionVariable categories = newObjectNavigationFromCollectionExpressionBuilder()
                .withCollectionExpression(newObjectNavigationFromCollectionExpressionBuilder()
                        .withCollectionExpression(newCollectionVariableReferenceBuilder()
                                .withVariable(orderDetails)
                                .build())
                        .withReferenceName("product")
                        .withName("p")
                        .build())
                .withReferenceName("category")
                .withName("c")
                .build();

        final StringExpression categoryName = newStringAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(categories)
                        .build())
                .withAttributeName("categoryName")
                .build();

        final StringExpression categoryDescription = newStringAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(categories)
                        .build())
                .withAttributeName("description")
                .build();

        final TimestampExpression completedDate = newTimestampAdditionExpressionBuilder()
                .withTimestamp(newTimestampAttributeBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(order)
                                .build())
                        .withAttributeName("shippedDate")
                        .build())
                .withOperator(TimestampDurationOperator.ADD)
                .withDuration(newMeasuredIntegerBuilder()
                        .withValue(BigInteger.ONE)
                        .withUnitName("week")
                        .build())
                .withAlias("completedDate")
                .build();

        final CollectionNavigationFromObjectExpression discountedItemsBase = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();
        final CollectionVariable discountedItems = newCollectionFilterExpressionBuilder()
                .withCollectionExpression(discountedItemsBase) // TODO - reusing variables (?)
                .withCondition(newDecimalComparisonBuilder()
                        .withLeft(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(discountedItemsBase)
                                        .build())
                                .withAttributeName("discount")
                                .build())
                        .withOperator(DecimalComparator.GREATER_THAN)
                        .withRight(newIntegerConstantBuilder()
                                .withValue(BigInteger.ONE)
                                .build())
                        .build())
                .withName("di")
                .withAlias("discountedItems")
                .build();

        final StringExpression discountedProductName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newObjectVariableReferenceBuilder()
                                .withVariable(discountedItems)
                                .build())
                        .withReferenceName("product")
                        .build())
                .withAttributeName("productName")
                .withAlias("product")
                .build();

        expressionResource.getContents().addAll(Arrays.asList(orderType, order
                , orderDate
                , shipperName
                , orderDetails
                , orderDetail
                , itemsOfDiscountedProducts
                , totalPrice
                , itemCount
                , categories
                , categoryName
                , categoryDescription
                , completedDate
                , discountedItems
                , discountedProductName
        ));

        return expressionResource;
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
