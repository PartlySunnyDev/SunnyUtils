package me.partlysunny.util.classes.predicates.relationships;

public enum Relationship {
    AND(AndRelationship.class),
    NAND(NandRelationship.class),
    OR(OrRelationship.class),
    NOR(NorRelationship.class),
    XNOR(XNORRelationship.class),
    XOR(XORRelationship.class);

    private final Class<? extends PredicateRelationship> clazz;

    Relationship(Class<? extends PredicateRelationship> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends PredicateRelationship> clazz() {
        return clazz;
    }
}
