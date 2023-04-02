package me.partlysunny.util.classes.predicates.relationships

enum class Relationship(private val clazz: Class<out PredicateRelationship>) {
    AND(AndRelationship::class.java),
    NAND(NandRelationship::class.java),
    OR(OrRelationship::class.java),
    NOR(NorRelationship::class.java),
    XNOR(XNORRelationship::class.java),
    XOR(XORRelationship::class.java);

    fun clazz(): Class<out PredicateRelationship> {
        return clazz
    }
}
