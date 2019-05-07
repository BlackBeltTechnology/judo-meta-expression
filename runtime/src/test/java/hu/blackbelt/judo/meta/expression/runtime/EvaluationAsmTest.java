package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAggregatedExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
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

        final ObjectVariable order = newInstanceBuilder()
                .withElementName(orderType)
                .withName("self")
                .build();

        final TimestampAttribute shippedDate = newTimestampAttributeBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withAttributeName("shippedDate")
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

        final CollectionVariable orderDetails = newCollectionNavigationFromObjectExpressionBuilder()
                .withObjectExpression(newObjectVariableReferenceBuilder()
                        .withVariable(order)
                        .build())
                .withReferenceName("orderDetails")
                .withName("od")
                .build();

        final NumericExpression price = newDecimalAritmeticExpressionBuilder()
                .withLeft(newDecimalAritmeticExpressionBuilder()
                        .withLeft(newIntegerAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetails)
                                        .build())
                                .withAttributeName("quantity")
                                .build())
                        .withOperator(DecimalOperator.MULTIPLY)
                        .withRight(newDecimalAttributeBuilder()
                                .withObjectExpression(newObjectVariableReferenceBuilder()
                                        .withVariable(orderDetails)
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
                                        .withVariable(orderDetails)
                                        .build())
                                .withAttributeName("discount")
                                .build())
                        .build())
                .build();

        final DecimalAggregatedExpression totalPrice = newDecimalAggregatedExpressionBuilder()
                .withCollectionExpression(newCollectionVariableReferenceBuilder()
                        .withVariable(orderDetails)
                        .build())
                .withOperator(DecimalAggregator.SUM)
                .withExpression(price)
                .build();

        final IntegerExpression itemCount = newCountExpressionBuilder()
                .withCollectionExpression(newCollectionVariableReferenceBuilder()
                        .withVariable(orderDetails)
                        .build())
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

        expressionResource.getContents().addAll(Arrays.asList(orderType, order, shippedDate, shipperName, orderDetails, totalPrice, itemCount, categories, categoryName, categoryDescription));

        return expressionResource;
    }

    @Test
    void test() throws Exception {
        execute();
    }
}
