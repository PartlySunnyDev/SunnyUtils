package me.partlysunny.util.classes.predicates.relationships;

import me.partlysunny.util.classes.predicates.CheckerPredicate;
import me.partlysunny.util.classes.predicates.PredicateContext;

public abstract class PredicateRelationship {

    protected final CheckerPredicate a;
    protected final CheckerPredicate b;

    protected PredicateRelationship(CheckerPredicate a, CheckerPredicate b) {
        this.a = a;
        this.b = b;
    }

    public abstract boolean check(PredicateContext ctx);
}
