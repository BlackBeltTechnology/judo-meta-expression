package hu.blackbelt.judo.meta.expression.esm;

import hu.blackbelt.judo.meta.esm.measure.DurationType;
import hu.blackbelt.judo.meta.esm.measure.Measure;
import hu.blackbelt.judo.meta.esm.measure.MeasureDefinitionTerm;
import hu.blackbelt.judo.meta.esm.measure.Unit;
import hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilder;
import hu.blackbelt.judo.meta.esm.namespace.Model;
import hu.blackbelt.judo.meta.esm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.esm.namespace.Package;
import hu.blackbelt.judo.meta.esm.namespace.util.builder.PackageBuilder;
import hu.blackbelt.judo.meta.esm.structure.*;
import hu.blackbelt.judo.meta.esm.structure.Class;
import hu.blackbelt.judo.meta.esm.structure.util.builder.DataMemberBuilder;
import hu.blackbelt.judo.meta.esm.structure.util.builder.EntityTypeBuilder;
import hu.blackbelt.judo.meta.esm.structure.util.builder.OneWayRelationMemberBuilder;
import hu.blackbelt.judo.meta.esm.type.EnumerationMember;
import hu.blackbelt.judo.meta.esm.type.EnumerationType;
import hu.blackbelt.judo.meta.esm.type.Primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static hu.blackbelt.judo.meta.esm.measure.util.builder.MeasureBuilders.*;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.esm.namespace.util.builder.NamespaceBuilders.newPackageBuilder;
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
        private Collection<DataFeature> attributes = new LinkedList<>();
        private Collection<RelationFeature> relations = new LinkedList<>();
        private Collection<Generalization> generalizations = new LinkedList<>();
        private Collection<EntitySequence> sequences = new LinkedList<>();

        public EntityCreator(String name) {
            this.name = name;
        }

        public EntityCreator withPrimitiveQuery(String name, String getterExpression, Primitive dataType) {
            attributes.add(createQuery(name, null, getterExpression, dataType));
            return this;
        }

        public EntityCreator withPrimitiveQuery(String name, EntityType input, String getterExpression, Primitive dataType) {
            attributes.add(createQuery(name, input, getterExpression, dataType));
            return this;
        }

        public EntityCreator withComplexQuery(String name, String getterExpression, EntityType dataType) {
            relations.add(createQuery(name, null, getterExpression, dataType));
            return this;
        }

        public EntityCreator withComplexQuery(String name, EntityType input, String getterExpression, EntityType dataType) {
            relations.add(createQuery(name, input, getterExpression, dataType));
            return this;
        }

        public EntityCreator withAttribute(String name, Primitive datatype) {
            attributes.add(createAttribute(name, datatype, ""));
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
            useEntityType(entityType)
                    .withMapping(newMappingBuilder().withTarget(entityType).build())
                    .build();
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
        builder.withMemberType(getterExpression != null && !getterExpression.trim().isEmpty() ? MemberType.DERIVED : MemberType.STORED);
        return builder.build();
    }

    public static DataFeature createAttribute(String name, Primitive datatype, String getterExpression) {
        DataMemberBuilder builder = newDataMemberBuilder().withName(name).withDataType(datatype);
        builder.withGetterExpression(getterExpression);
        builder.withMemberType(getterExpression != null && !getterExpression.trim().isEmpty() ? MemberType.DERIVED : MemberType.STORED);
        return builder.build();
    }

    public static DataFeature createQuery(String name, TransferObjectType input, String getterExpression, Primitive dataType) {
        return newDataMemberBuilder()
                .withName(name)
                .withInput(input)
                .withIsQuery(true)
                .withDataType(dataType)
                .withMemberType(MemberType.DERIVED)
                .withGetterExpression(getterExpression)
                .build();
    }

    public static RelationFeature createQuery(String name, TransferObjectType input, String getterExpression, TransferObjectType datatype) {
        return newOneWayRelationMemberBuilder()
                .withName(name)
                .withInput(input)
                .withIsQuery(true)
                .withTarget(datatype)
                .withLower(0).withUpper(-1)
                .withMemberType(MemberType.STORED)
                .withGetterExpression(getterExpression)
                .withRelationKind(RelationKind.ASSOCIATION)
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
