package me.partlysunny.util.classes.predicates.relationships;

import me.partlysunny.util.classes.predicates.CheckerPredicate;
import me.partlysunny.util.classes.predicates.PredicateContext;

public class XNORRelationship extends PredicateRelationship {

    public XNORRelationship(CheckerPredicate a, CheckerPredicate b) {
        super(a, b);
    }

    @Override
    public boolean check(PredicateContext ctx) {
        return a.process(ctx) == b.process(ctx);
    }
}
