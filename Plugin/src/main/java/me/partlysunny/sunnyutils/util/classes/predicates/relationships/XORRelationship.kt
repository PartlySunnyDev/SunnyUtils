package me.partlysunny.sunnyutils.util.classes.predicates.relationships

import me.partlysunny.sunnyutils.util.classes.predicates.CheckerPredicate
import me.partlysunny.sunnyutils.util.classes.predicates.PredicateContext

class XORRelationship(a: CheckerPredicate?, b: CheckerPredicate?) : PredicateRelationship(a, b) {
    override fun check(ctx: PredicateContext): Boolean {
        return a!!.process(ctx) xor b!!.process(ctx)
    }
}
