package hu.blackbelt.judo.meta.expression.esm;

import hu.blackbelt.judo.meta.esm.measure.*;
import hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilder;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.namespace.*;
import hu.blackbelt.judo.meta.esm.namespace.util.builder.PackageBuilder;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.structure.util.builder.*;
import hu.blackbelt.judo.meta.esm.type.*;

import java.util.*;

import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.*;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
import static hu.blackbelt.judo.meta.esm.structure.MemberType.DERIVED;
import static hu.blackbelt.judo.meta.esm.structure.MemberType.STORED;
import static hu.blackbelt.judo.meta.esm.structure.RelationKind.ASSOCIATION;
import static hu.blackbelt.judo.meta.esm.structure.util.builder.StructureBuilders.*;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newEnumerationMemberBuilder;
import static hu.blackbelt.judo.meta.esm.type.util.builder.TypeBuilders.newEnumerationTypeBuilder;

public class EsmTestModelCreator {

    public static class MeasureCreator {
        private String name;
        private Collection<Unit> units = new ArrayList<>();
        private Collection<MeasureDefinitionTerm> terms = new ArrayList<>();

        public MeasureCreator(String name) {
            this.name = name;
        }

        public Measure create() {
            MeasureBuilder builder = newMeasureBuilder().withName(name);
            if (!units.isEmpty()) {
                builder.withUnits(units);
            }
            if (!terms.isEmpty()) {
                builder.withTerms(terms);
            }
            return builder.build();
        }

        public MeasureCreator withUnit(String name) {
            units.add(newUnitBuilder().withName(name).build());
            return this;
        }

        public MeasureCreator withDurationUnit(String name, DurationType durationType) {
            units.add(newDurationUnitBuilder().withName(name).withUnitType(durationType).build());
            return this;
        }

        public MeasureCreator withTerm(Unit unit, int exponent) {
            terms.add(newMeasureDefinitionTermBuilder().withUnit(unit).withExponent(exponent).build());
            return this;
        }

    }

    public static class EntityCreator {
        private String name;
        private final Collection<DataFeature> attributes = new LinkedList<>();
        private final Collection<RelationFeature> relations = new LinkedList<>();
        private final Collection<QueryFeature> queries = new LinkedList<>();
        private final Collection<Generalization> generalizations = new LinkedList<>();
        private final Collection<EntitySequence> sequences = new LinkedList<>();

        public EntityCreator(String name) {
            this.name = name;
        }

        public EntityCreator withAttribute(String name, Primitive datatype) {
            attributes.add(createAttribute(name, datatype, ""));
            return this;
        }

        public EntityCreator withPrimitiveQuery(String name, TransferObjectType input, String expression, Primitive output) {
            DataFeature primitiveQuery = createPrimitiveQuery(name, input, expression, output);
            queries.add(primitiveQuery);
            attributes.add(primitiveQuery);
            return this;
        }

        public EntityCreator withComplexQuery(String name, TransferObjectType input, String expression, TransferObjectType output) {
            OneWayRelationMember complexQuery = createComplexQuery(name, input, expression, output);
            queries.add(complexQuery);
            relations.add(complexQuery);
            return this;
        }

        public EntityCreator withDerivedAttribute(String name, Primitive datatype, String getterExpression) {
            attributes.add(createAttribute(name, datatype, getterExpression));
            return this;
        }

        public EntityCreator withDerivedReference(String name, hu.blackbelt.judo.meta.esm.structure.Class target, String getterExpression) {
            relations.add(createRelation(name, target, 1, getterExpression));
            return this;
        }

        public EntityCreator withObjectRelation(String name, hu.blackbelt.judo.meta.esm.structure.Class target) {
            relations.add(createRelation(name, target, 1, ""));
            return this;
        }

        public EntityCreator withObjectRelation(RelationFeature owr) {
            relations.add(owr);
            return this;
        }

        public EntityCreator withCollectionRelation(String name, hu.blackbelt.judo.meta.esm.structure.Class target) {
            relations.add(createRelation(name, target, -1, ""));
            return this;
        }

        public EntityCreator withGeneralization(hu.blackbelt.judo.meta.esm.structure.Class target) {
            generalizations.add(newGeneralizationBuilder().withTarget(target).build());
            return this;
        }

        public EntityCreator withSequence(String name) {
            sequences.add(newEntitySequenceBuilder().withName(name).build());
            return this;
        }

        public EntityType create() {
            EntityTypeBuilder builder = newEntityTypeBuilder().withName(name);
            if (!queries.isEmpty()) {
                builder.withQueries(queries);
            }
            if (!attributes.isEmpty()) {
                builder.withAttributes(attributes);
            }
            if (!relations.isEmpty()) {
                builder.withRelations(relations);
            }
            if (!generalizations.isEmpty()) {
                builder.withGeneralizations(generalizations);
            }
            if (!sequences.isEmpty()) {
                builder.withSequences(sequences);
            }
            EntityType entityType = builder.build();
            entityType.setMappedEntity(entityType);
            return entityType;
        }

        public EntityCreator withTwoWayRelation(TwoWayRelationMember partner) {
            relations.add(partner);
            return this;
        }

        public EntityCreator withTwoWayRelation(String name, Class target, TwoWayRelationMember partner, boolean multi) {
            TwoWayRelationMember relationMember = newTwoWayRelationMemberBuilder()
                    .withName(name)
                    .withPartner(partner)
                    .withTarget(target)
                    .withUpper(multi ? -1 : 1)
                    .build();
            partner.setPartner(relationMember);
            relations.add(relationMember);
            return this;
        }
    }

    public static Package createPackage(String name, NamespaceElement... children) {
        PackageBuilder packageBuilder = newPackageBuilder().withName(name);
        if (children.length > 0) {
            packageBuilder = packageBuilder.withElements(Arrays.asList(children));
        }
        return packageBuilder.build();
    }

    public static RelationFeature createRelation(String name, Class target, int upperBound, String getterExpression) {
        OneWayRelationMemberBuilder builder = newOneWayRelationMemberBuilder().withName(name).withTarget(target).withUpper(upperBound);
        builder.withGetterExpression(getterExpression);
        builder.withMemberType(getterExpression != null && !getterExpression.trim().isEmpty() ? DERIVED : STORED);
        return builder.build();
    }

    public static DataFeature createAttribute(String name, Primitive datatype, String getterExpression) {
        DataMemberBuilder builder = newDataMemberBuilder().withName(name).withDataType(datatype);
        builder.withGetterExpression(getterExpression);
        builder.withMemberType(getterExpression != null && !getterExpression.trim().isEmpty() ? DERIVED : STORED);
        return builder.build();
    }

    public static DataFeature createPrimitiveQuery(String name, TransferObjectType input, String expression, Primitive output) {
        return newDataMemberBuilder()
                .withName(name)
                .withInput(input)
                .withIsQuery(true)
                .withDataType(output)
                .withMemberType(DERIVED)
                .withGetterExpression(expression)
                .build();
    }

    public static OneWayRelationMember createComplexQuery(String name, TransferObjectType input, String expression, TransferObjectType output) {
        return newOneWayRelationMemberBuilder()
                .withName(name)
                .withInput(input)
                .withIsQuery(true)
                .withTarget(output)
                .withMemberType(DERIVED)
                .withLower(0).withUpper(-1)
                .withRelationKind(ASSOCIATION)
                .withGetterExpression(expression)
                .build();
    }

    public static EnumerationType createEnum(String name, String... members) {
        List<EnumerationMember> enumerationMembers = new LinkedList<>();
        for (String member : members) {
            enumerationMembers.add(newEnumerationMemberBuilder().withName(member).withOrdinal(enumerationMembers.size()).build());
        }
        return newEnumerationTypeBuilder()
                .withName(name)
                .withMembers(enumerationMembers)
                .build();
    }

    public static Model createTestModel(NamespaceElement... elems) {
        return newModelBuilder().withName("demo").withElements(Arrays.asList(elems)).build();
    }


}
