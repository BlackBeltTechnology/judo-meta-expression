package hu.blackbelt.judo.meta.expression.builder.jql.esm;

import hu.blackbelt.judo.meta.esm.structure.EntityType;
import hu.blackbelt.judo.meta.esm.structure.RelationFeature;
import hu.blackbelt.judo.meta.esm.structure.TwoWayRelationMember;
import hu.blackbelt.judo.meta.esm.type.NumericType;
import hu.blackbelt.judo.meta.esm.type.StringType;
import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.builder.jql.CircularReferenceException;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildException;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator;
import hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.EntityCreator;
import hu.blackbelt.judo.meta.expression.numeric.Length;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.string.Concatenate;
import hu.blackbelt.judo.meta.expression.string.StringAttribute;
import hu.blackbelt.judo.meta.expression.string.Trim;
import org.junit.jupiter.api.Test;

import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.newTwoWayRelationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newNumericTypeBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newStringTypeBuilder;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createRelation;
import static hu.blackbelt.judo.meta.expression.esm.EsmTestModelCreator.createTestModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EsmJqlDerivedExpressionTest extends  AbstractEsmJqlExpressionBuilderTest {

    @Test
    void testDerivedAttribute() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        NumericType numericType = newNumericTypeBuilder().withName("int").withPrecision(10).withScale(0).build();
        EntityType person = new EntityCreator("Person")
                .withAttribute("email2", stringType)
                .withDerivedAttribute("email1", stringType, "self.email2")
                .withDerivedAttribute("email1Trimmed", stringType, "self.email1!trim()")
                .withDerivedAttribute("email1TrimmedLowerLength", stringType, "self.email1Trimmed!lowerCase()!length()")
                .withDerivedAttribute("emailWrong1", stringType, "self.emailWrong2")
                .withDerivedAttribute("emailWrong2", stringType, "self.emailWrong1")
                .create();
        initResources(createTestModel(person, stringType, numericType));
        Expression derivedExpression = createExpression(person, "self.email1");
        assertThat(derivedExpression, instanceOf(AttributeSelector.class));
        assertThat(((AttributeSelector) derivedExpression).getAttributeName(), is("email2"));

        Expression derivedExpressionFunction = createExpression(person, "self.email1!length()");
        assertThat(derivedExpressionFunction, instanceOf(Length.class));
        StringAttribute operandAttribute = (StringAttribute) ((Length) derivedExpressionFunction).getExpression();
        assertThat(operandAttribute.getAttributeName(), is("email2"));

        Expression email1Trimmed = createExpression(person, "self.email1Trimmed");
        assertThat(email1Trimmed, instanceOf(Trim.class));

        Expression email1TrimmedLowerCaseLength = createExpression(person, "self.email1TrimmedLowerLength");
        assertThat(email1TrimmedLowerCaseLength, instanceOf(Length.class));

        Expression email1Concat = createExpression(person, "self.email1 + self.email1Trimmed");
        assertThat(email1Concat, instanceOf(Concatenate.class));

        assertThrows(JqlExpressionBuildException.class, () -> createExpression(person, "self.emailWrong1"));
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(person, "self.email1!matches(self.emailWrong1)"));
    }

    @Test
    void testDerivedSelf() {
        NumericType numericType = newNumericTypeBuilder().withName("numeric").withScale(10).withPrecision(1).build();
        EntityType order = new EntityCreator("Order")
                        .withAttribute("productWeight", numericType)
                        .withDerivedAttribute("sumWeight", numericType, "self.quantity * self.productWeight")
                        .withDerivedAttribute("calculatedWeight", numericType, "self.sumWeight * 2")
                        .withAttribute("quantity", numericType)
                        .create();
        initResources(createTestModel(numericType, order));
        createExpression(order, "self.calculatedWeight / 2");
    }


    @Test
    void testReferenceDerivedAttribute() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        TwoWayRelationMember twr = newTwoWayRelationMemberBuilder()
                .withName("a")
                .withUpper(1).build();
        EntityType entityB = new EntityCreator("B")
                .withAttribute("field", stringType)
                .withDerivedAttribute("aField", stringType, "self.a.field")
                .withDerivedAttribute("aField2", stringType, "self.a.field2")
                .withDerivedAttribute("q", stringType, "self.a.field")
                .withTwoWayRelation(twr)
                .create();
        EntityType entityA = new EntityCreator("A")
                .withAttribute("field", stringType)
                .withAttribute("field2", stringType)
                .withDerivedAttribute("bField", stringType, "self.b.field")
                .withDerivedAttribute("aField2", stringType, "self.b.aField2")
                .withTwoWayRelation("b", entityB, twr, false)
                .withDerivedAttribute("q", stringType, "self.b.q")
                .create();
        twr.setTarget(entityA);
        initResources(createTestModel(stringType, entityA, entityB));

        createExpression(entityA, "self.bField + self.bField");
        createExpression(entityB, "self.aField + self.aField");
        assertThat(createExpression(entityA, "self.aField2"), hasToString("self->b->a.field2"));
        // when derived attribute contains a different 'self':
        createExpression(entityA, "self.aField2 + self.bField");

    }

    @Test
    void testReferenceDerivedInheritedAttribute() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        TwoWayRelationMember twr = newTwoWayRelationMemberBuilder()
                .withName("a")
                .withUpper(1).build();
        EntityType entityB = new EntityCreator("B")
                .withTwoWayRelation(twr)
                .withDerivedAttribute("w", stringType, "self.a.w")
                .create();
        EntityType entityParent = new EntityCreator("Parent")
                .withAttribute("p", stringType)
                .withDerivedAttribute("w", stringType, "self.b.w")
                .withDerivedAttribute("wrong1", stringType, "self.wrong2")
                .withDerivedAttribute("wrong2", stringType, "self.wrong1")
                .withTwoWayRelation("b", entityB, twr, false)
                .create();
        twr.setTarget(entityParent);
        EntityType entityA = new EntityCreator("A")
                .withGeneralization(entityParent)
                .withDerivedAttribute("parentP", stringType, "self.p")
                .withDerivedAttribute("parentWrong", stringType, "self.wrong1")
                .withDerivedAttribute("q", stringType, "self.b.w")
                .create();
        initResources(createTestModel(stringType, entityParent, entityA, entityB));
        createExpression(entityA, "self.p");
        createExpression(entityA, "self.parentP");
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(entityA, "self.wrong1"));
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(entityA, "self.parentWrong"));
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(entityA, "self.q"));
    }

    @Test
    public void testDerivedReferenceDerivedAttribute() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType entityB = new EntityCreator("B")
                .withAttribute("bField", stringType)
                .withDerivedAttribute("bFieldDerived", stringType, "self.bField")
                .create();
        EntityType entityA = new EntityCreator("A")
                .withObjectRelation("b", entityB)
                .withDerivedReference("bDerived", entityB, "self.b")
                .create();
        initResources(createTestModel(entityA, entityB, stringType));
        assertThat(createExpression(entityA, "self.b.bFieldDerived"), hasToString("self->b.bField"));
        assertThat(createExpression(entityA, "self.bDerived.bFieldDerived"), hasToString("self->b.bField"));
    }

    @Test
    public void testDerivedSingle() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        RelationFeature aRel = createRelation("a", null, 1, "");
        RelationFeature aDerivedRel = createRelation("aDerived", null, 1, "self.a");
        RelationFeature aDerivedCircleRel = createRelation("aDerivedCircle", null, 1, "self.aDerivedCircle");
        EntityType entityA = new EntityCreator("A")
                .withAttribute("aField", stringType)
                .withDerivedAttribute("aFieldDerived", stringType, "self.a.aField")
                .withDerivedAttribute("aDerivedFieldDerived", stringType, "self.aDerived.aField")
                .withObjectRelation(aRel)
                .withObjectRelation(aDerivedRel)
                .withObjectRelation(aDerivedCircleRel)
                .create();
        aRel.setTarget(entityA);
        aDerivedRel.setTarget(entityA);
        aDerivedCircleRel.setTarget(entityA);
        initResources(createTestModel(entityA, stringType));

        assertThat(createExpression(entityA, "self.aFieldDerived"), hasToString("self->a.aField"));
        assertThat(createExpression(entityA, "self.aDerived"), hasToString("self->a"));
        assertThat(createExpression(entityA, "self.aDerived.aField"), hasToString("self->a.aField"));
        assertThat(createExpression(entityA, "self.aFieldDerived"), hasToString("self->a.aField"));
        assertThat(createExpression(entityA, "self.aDerivedFieldDerived"), hasToString("self->a.aField"));
        assertThat(createExpression(entityA, "self.aDerived.a"), hasToString("self->a->a"));
        assertThat(createExpression(entityA, "self.a.aFieldDerived"), hasToString("self->a->a.aField"));
        assertThat(createExpression(entityA, "self.a.aDerivedFieldDerived"), hasToString("self->a->a.aField"));
    }

    @Test
    public void testDerivedAttributeChain() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType entityC = new EntityCreator("C").withAttribute("cField", stringType).create();
        EntityType entityB = new EntityCreator("B")
                .withAttribute("bField", stringType)
                .withDerivedAttribute("cField", stringType, "self.c.cField")
                .withObjectRelation("c", entityC).create();
        EntityType entityA = new EntityCreator("A").withObjectRelation("b", entityB)
                .withDerivedAttribute("bField", stringType, "self.b.bField")
                .withDerivedAttribute("cField", stringType, "self.b.c.cField")
                .withDerivedAttribute("bcField", stringType, "self.b.cField")
                .create();
        initResources(createTestModel(entityA, entityB, entityC, stringType));

        assertThat(createExpression(entityA, "self.bField"), hasToString("self->b.bField"));
        assertThat(createExpression(entityA, "self.cField"), hasToString("self->b->c.cField"));
        assertThat(createExpression(entityA, "self.b.cField"), hasToString("self->b->c.cField"));
        assertThat(createExpression(entityA, "self.bcField"), hasToString("self->b->c.cField"));
        assertThat(createExpression(entityA, "self.b.cField!length()"), hasToString("LENGTH(self->b->c.cField)"));

    }

    @Test
    public void testDerivedFunction() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType entityB = new EntityCreator("B")
                .withAttribute("b", stringType)
                .withDerivedAttribute("bLower", stringType, "self.b!lowerCase()")
                .create();
        EntityType entityA = new EntityCreator("A")
                .withObjectRelation("b", entityB)
                .withDerivedAttribute("bTrim", stringType, "self.b.bLower!trim()")
                .create();
        initResources(createTestModel(entityA, entityB, stringType));
        assertThat(createExpression(entityA, "self.bTrim!upperCase()"), hasToString("UPPER(TRIM(LOWER(self->b.b)))"));
    }

    @Test
    public void testDerivedReference() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        EntityType entityE = new EntityCreator("E")
                .withAttribute("eField", stringType)
                .withDerivedAttribute("eFieldDerived", stringType, "self.eField")
                .create();
        EntityType entityD = new EntityCreator("D").withAttribute("dField", stringType).create();
        EntityType entityC = new EntityCreator("C")
                .withAttribute("cField", stringType)
                .withObjectRelation("d", entityD)
                .withCollectionRelation("es", entityE)
                .withDerivedReference("e", entityE, "self=>es!sort()!head()")
                .create();
        EntityType entityB = new EntityCreator("B").withAttribute("bField", stringType)
                .withObjectRelation("c", entityC)
                .withDerivedReference("d", entityD, "self.c.d")
                .withCollectionRelation("es", entityE)
                .withDerivedReference("esSorted", entityE, "self.es!sort()")
                .create();
        EntityType entityA = new EntityCreator("A").withObjectRelation("b", entityB)
                .withDerivedReference("c", entityC, "self.b.c")
                .create();
        initResources(createTestModel(entityA, entityB, entityC, entityD, entityE, stringType));

        createExpression(entityA, "self.b.c.cField");
        createExpression(entityA, "self.b.d.dField");
        createExpression(entityA, "self.c.es");
        Expression expression = createExpression(entityA, "self.b.d");
        assertThat(expression.toString(), is("self->b->c->d"));
        assertThat(createExpression(entityB, "self.d").toString(), is("self->c->d"));
        AttributeSelector cFieldExpression = (AttributeSelector) createExpression(entityA, "self.c.cField");
        ObjectNavigationExpression navigationExpression = (ObjectNavigationExpression) cFieldExpression.getObjectExpression();
        assertThat(navigationExpression.getObjectExpression(), instanceOf(ObjectNavigationExpression.class));
//        TODO JNG-1699: assertThat(createExpression(entityA, "self.c.e.eField"), hasToString("(head self->b->c=>es as e orderedBy e.id).eField"));
//        assertThat(createExpression(entityA, "self.c.e.eFieldDerived"), hasToString("(head self->b->c=>es as e orderedBy e.id).eField"));
//        assertThat(createExpression(entityA, "self.b.esSorted!head().eField"), hasToString("(head self->b=>es as e orderedBy e.id).eField"));
    }

    @Test
    public void testCircularReference() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        RelationFeature owr = EsmTestModelCreator.createRelation("a", null, 1, "");
        RelationFeature derived = EsmTestModelCreator.createRelation("c", null, 1, "self.a.c");
        EntityType entityC = new EntityCreator("C").withAttribute("cField", stringType).withObjectRelation(owr).withObjectRelation(derived).create();
        EntityType entityB = new EntityCreator("B").withAttribute("bField", stringType).withObjectRelation("c", entityC).create();
        EntityType entityA = new EntityCreator("A").withObjectRelation("b", entityB)
                .withDerivedReference("c", entityC, "self.b.c")
                .withDerivedReference("w", entityC, "self.c.a.w")
                .withAttribute("aField", stringType)
                .create();
        owr.setTarget(entityA);
        derived.setTarget(entityC);
        initResources(createTestModel(entityA, entityB, entityC, stringType));
        assertThat(createExpression(entityA, "self.c.a.aField"), hasToString("self->b->c->a.aField"));
        assertThat(createExpression(entityA, "self.c.c"), hasToString("self->b->c->a->b->c"));
        assertThrows(JqlExpressionBuildException.class, () -> createExpression(entityA, "self.w"));
    }

    @Test
    public void testReferenceChain() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        RelationFeature owr = EsmTestModelCreator.createRelation("a", null, 1, "");
        EntityType entityC = new EntityCreator("C").withAttribute("cField", stringType).withObjectRelation(owr).create();
        EntityType entityB = new EntityCreator("B").withAttribute("bField", stringType).withObjectRelation("c", entityC).create();
        EntityType entityA = new EntityCreator("A").withObjectRelation("b", entityB)
                .withDerivedReference("c", entityC, "self.b.c")
                .create();
        owr.setTarget(entityA);
        initResources(createTestModel(entityA, entityB, entityC, stringType));
        assertThat(createExpression(entityA, "self.c.a.c"), hasToString("self->b->c->a->b->c"));
    }

    @Test
    public void testDerivedInLambda() {
        StringType stringType = newStringTypeBuilder().withName("string").build();
        NumericType intType = newNumericTypeBuilder().withName("int").build();
        EntityType entityB = new EntityCreator("B")
                .withAttribute("bField", stringType)
                .withDerivedAttribute("bFieldLength", intType, "self.bField!length()")
                .create();
        EntityType entityA = new EntityCreator("A")
                .withCollectionRelation("bs", entityB)
                .withDerivedReference("bFiltered", entityB, "self->bs!filter(b | b.bFieldLength > 0)")
                .withDerivedAttribute("bCount", intType, "self->bFiltered!count()")
                .create();
        initResources(createTestModel(entityA, entityB, intType, stringType));
        assertThat(createExpression(entityA, "self.bCount"), hasToString("self=>bs!filter(b | (LENGTH(b.bField) > 0))!count()"));
    }
}
