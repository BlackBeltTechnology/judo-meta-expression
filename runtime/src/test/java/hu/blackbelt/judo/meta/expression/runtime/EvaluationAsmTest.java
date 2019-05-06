package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.ObjectNavigationFromCollectionExpression;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAggregatedExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
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
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;

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
        final TypeName categoryType = newTypeNameBuilder().withNamespace("northwind.entities").withName("Category").build();

        final StringExpression shipperName = newStringAttributeBuilder()
                .withObjectExpression(newObjectNavigationExpressionBuilder()
                        .withObjectExpression(newInstanceBuilder()
                                .withElementName(orderType)
                                .withName("self")
                                .build())
                        .withName("s")
                        .withReferenceName("shipper")
                        .build())
                .withAttributeName("companyName")
                .build();

        final CollectionNavigationFromObjectExpression orderDetailsForTotalPrice = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(orderType)
                        .withName("self")
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();

        final CollectionNavigationFromObjectExpression orderDetailsForItemCount = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(orderType)
                        .withName("self")
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();

        final CollectionNavigationFromObjectExpression orderDetailsForCategoryName = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(orderType)
                        .withName("self")
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();

        final CollectionNavigationFromObjectExpression orderDetailsForCategoryDescription = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(orderType)
                        .withName("self")
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();

        final NumericExpression price = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetailsForTotalPrice)
                                        .build())
                                .withAttributeName("quantity")
                                .build())
                        .withOperator(DecimalOperator.MULTIPLY)
                        .withRight(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetailsForTotalPrice)
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
                                        .withVariable(orderDetailsForTotalPrice)
                                        .build())
                                .withAttributeName("discount")
                                .build())
                        .build())
                .build();

        final DecimalAggregatedExpression totalPrice = newDecimalAggregatedExpressionBuilder()
                .withCollectionExpression(orderDetailsForTotalPrice)
                .withOperator(DecimalAggregator.SUM)
                .withExpression(price)
                .build();

        final IntegerExpression itemCount = newCountExpressionBuilder()
                .withCollectionExpression(orderDetailsForItemCount)
                .build();

        final ObjectNavigationFromCollectionExpression categories = newObjectNavigationFromCollectionExpressionBuilder()
                .withCollectionExpression(newObjectNavigationFromCollectionExpressionBuilder()
                        .withCollectionExpression(orderDetailsForCategoryName)
                        .withReferenceName("product")
                        .withName("p")
                        .build())
                .withReferenceName("category")
                .withName("c")
                .build();

        final StringExpression categoryName = newStringAttributeBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(categoryType)
                        .withName("self")
                        .build())
                .withAttributeName("categoryName")
                .build();

        final StringExpression categoryDescription = newStringAttributeBuilder()
                .withObjectExpression(newInstanceBuilder()
                        .withElementName(categoryType)
                        .withName("self")
                        .build())
                .withAttributeName("description")
                .build();

        expressionResource.getContents().addAll(Arrays.asList(orderType, shipperName, totalPrice, itemCount, categories, categoryName, categoryDescription));

        return expressionResource;
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
