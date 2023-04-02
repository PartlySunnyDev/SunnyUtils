package me.partlysunny.util.classes.predicates.relationships

import me.partlysunny.util.classes.predicates.CheckerPredicate
import me.partlysunny.util.classes.predicates.PredicateContext

class NorRelationship(a: CheckerPredicate?, b: CheckerPredicate?) : PredicateRelationship(a, b) {
    override fun check(ctx: PredicateContext): Boolean {
        return !(a!!.process(ctx) || b!!.process(ctx))
    }
}
