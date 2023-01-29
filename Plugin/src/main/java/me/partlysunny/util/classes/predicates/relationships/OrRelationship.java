package me.partlysunny.util.classes.predicates.relationships;

import me.partlysunny.util.classes.predicates.CheckerPredicate;
import me.partlysunny.util.classes.predicates.PredicateContext;

public class OrRelationship extends PredicateRelationship {
    public OrRelationship(CheckerPredicate a, CheckerPredicate b) {
        super(a, b);
    }

    @Override
    public boolean check(PredicateContext ctx) {
        return a.process(ctx) || b.process(ctx);
    }
}
