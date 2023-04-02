package me.partlysunny.util.classes.predicates.relationships

import me.partlysunny.util.classes.predicates.CheckerPredicate
import me.partlysunny.util.classes.predicates.PredicateContext

abstract class PredicateRelationship protected constructor(
    protected val a: CheckerPredicate?,
    protected val b: CheckerPredicate?
) {
    abstract fun check(ctx: PredicateContext): Boolean
}
