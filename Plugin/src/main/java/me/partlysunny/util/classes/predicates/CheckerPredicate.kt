package me.partlysunny.util.classes.predicates;

import com.google.common.base.Preconditions;
import me.partlysunny.util.classes.builders.HashMapBuilder;
import me.partlysunny.util.classes.predicates.relationships.PredicateRelationship;
import me.partlysunny.util.classes.predicates.relationships.Relationship;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CheckerPredicate {

    public static final CheckerPredicate TRUE = new CheckerPredicate("true");
    public static final CheckerPredicate FALSE = new CheckerPredicate("false");
    private final String predicate;
    private final List<String> chunks;
    public CheckerPredicate(String s) {
        this.predicate = s;
        this.chunks = chunk();
        checkValid();
    }

    public static CheckerPredicate from(boolean b) {
        return b ? TRUE : FALSE;
    }

    private void checkValid() {
        //Check brackets
        Preconditions.checkArgument(isBracketReady(), "Predicate %s is not bracket ready!".formatted(predicate));
        //Check chunks
        Preconditions.checkArgument(chunks.size() % 2 == 1, "Invalid predicate %s, chunks must have odd length: Chunks are: %s".formatted(predicate, chunks));
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            if (i % 2 == 1) {
                try {
                    Relationship.valueOf(chunk);
                } catch (Exception e) {
                    throw new IllegalArgumentException("All predicate joiners must be %s, Found: %s".formatted(Arrays.toString(Relationship.values()), chunk));
                }
            }
        }
    }

    private boolean isBracketReady() {
        Stack<Boolean> stack = new Stack<>();
        for (int i = 0; i < predicate.length(); i++) {
            if (predicate.charAt(i) == '(') {
                stack.push(true);
            } else if (predicate.charAt(i) == ')') {
                if (stack.empty()) return false;
                stack.pop();
            }
        }
        return stack.empty();
    }

    private List<String> chunk() {
        List<String> chunks = new ArrayList<>();
        Stack<Integer> brackets = new Stack<>();

        int start = 0;
        for (int i = 0; i < predicate.length(); i++) {
            char c = predicate.charAt(i);
            if (c == '(') {
                brackets.push(i);
            } else if (c == ')') {
                int startBracket = brackets.pop();
                if (brackets.empty()) {
                    chunks.add(predicate.substring(startBracket + 1, i));
                    start = i + 2;
                }
            }
            for (Relationship r : Relationship.values()) {
                String name = r.toString();
                if (c == name.charAt(0) && i < predicate.length() - name.length() && i > 0) {
                    String next = predicate.substring(i - 1, i + name.length() + 1);
                    if (next.equals(" " + name + " ")) {
                        if (!brackets.empty()) {
                            continue;
                        }
                        if (start != i) chunks.add(predicate.substring(start, i));
                        chunks.add(next);
                        start = i + name.length();
                        i += name.length() - 1;
                    }
                }
            }

        }
        if (start < predicate.length()) {
            if (!brackets.empty()) {
                chunks.add(predicate.substring(brackets.pop()));
            } else {
                chunks.add(predicate.substring(start));
            }
        }
        chunks = chunks.stream().map(String::trim).collect(Collectors.toList());
        return chunks;
    }

    private String processTermWithContext(PredicateContext ctx, String term) {
        if (term.length() == 0) return term;
        if (term.startsWith("?")) {
            return ctx.get(term.substring(1));
        }
        return term;
    }

    public boolean process(PredicateContext ctx) {

        if (chunks.size() == 1) {
            String expression = chunks.get(0);
            //Process the one chunk
            //"true" or "false"
            if (expression.equals("true")) {
                return true;
            } else if (expression.equals("false")) {
                return false;
            }
            String[] expressionItems = expression.split(" ++");
            if (expressionItems.length == 3) {
                String term1 = processTermWithContext(ctx, expressionItems[0]);
                String operator = expressionItems[1];
                String term2 = processTermWithContext(ctx, expressionItems[2]);

                switch (operator) {
                    case "=" -> {
                        boolean stringEquals = term1.equals(term2);
                        if (!stringEquals) {
                            try {
                                stringEquals = Double.parseDouble(term1) == (Double.parseDouble(term2));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        return stringEquals;
                    }
                    case "!=" -> {
                        boolean stringEquals = !term1.equals(term2);
                        if (!stringEquals) {
                            try {
                                stringEquals = Double.parseDouble(term1) != (Double.parseDouble(term2));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        return stringEquals;
                    }
                    case ">" -> {
                        try {
                            return Double.parseDouble(term1) > (Double.parseDouble(term2));
                        } catch (NumberFormatException ignored) {
                            throw new IllegalArgumentException("Left and right terms must both be parsable doubles in expression %s".formatted(expression));
                        }
                    }
                    case "<" -> {
                        try {
                            return Double.parseDouble(term1) < (Double.parseDouble(term2));
                        } catch (NumberFormatException ignored) {
                            throw new IllegalArgumentException("Left and right terms must both be parsable doubles in expression %s".formatted(expression));
                        }
                    }
                    case ">=" -> {
                        try {
                            return Double.parseDouble(term1) >= (Double.parseDouble(term2));
                        } catch (NumberFormatException ignored) {
                            throw new IllegalArgumentException("Left and right terms must both be parsable doubles in expression %s".formatted(expression));
                        }
                    }
                    case "<=" -> {
                        try {
                            return Double.parseDouble(term1) <= (Double.parseDouble(term2));
                        } catch (NumberFormatException ignored) {
                            throw new IllegalArgumentException("Left and right terms must both be parsable doubles in expression %s".formatted(expression));
                        }
                    }
                }
            }
            throw new IllegalArgumentException("Invalid expression found in predicate! %s".formatted(expression));
        }

        PredicateRelationship endRelationship = null;
        for (int i = 0; i < chunks.size(); i++) {
            String item = chunks.get(i);
            //If the variable "i" is even then it is an operator, otherwise it's an expression
            if (i % 2 == 1) {
                BiFunction<CheckerPredicate, CheckerPredicate, PredicateRelationship> operationProcessor = (a, b) -> {
                    for (Relationship r : Relationship.values()) {
                        if (item.equals(r.toString())) {
                            try {
                                return r.clazz().getDeclaredConstructor(CheckerPredicate.class, CheckerPredicate.class).newInstance(a, b);
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    throw new IllegalStateException("Somehow the program really broke down somewhere, better contact devs");
                };
                if (endRelationship == null) {
                    endRelationship = operationProcessor.apply(new CheckerPredicate(chunks.get(i - 1)), new CheckerPredicate(chunks.get(i + 1)));
                } else {
                    endRelationship = operationProcessor.apply(from(endRelationship.check(ctx)), new CheckerPredicate(chunks.get(i + 1)));
                }
            }
        }
        assert endRelationship != null;
        return endRelationship.check(ctx);

    }

    public static final class PredicateTest {

        @Test
        public void chunkTest() {
            Assert.assertEquals(List.of("(a AND b) OR c", "AND", "d", "OR", "e"), new CheckerPredicate("((a AND b) OR c) AND d OR e").chunk());
            Assert.assertEquals(List.of("a AND b", "OR", "e", "AND", "?weather is SUNNY", "OR", "d AND c"), new CheckerPredicate("(a AND b) OR e AND ?weather is SUNNY OR (d AND c)").chunk());
            Assert.assertEquals(List.of("a AND b", "OR", "e", "AND", "?weather is STORMY", "AND", "d AND c"), new CheckerPredicate("(a AND b) OR e AND ?weather is STORMY AND (d AND c)").chunk());
        }

        @Test
        public void relationshipTest() {
            Assert.assertTrue(new CheckerPredicate("(true XOR false) OR ((true AND false) XNOR (false OR false))").process(new PredicateContext()));
        }

        @Test
        public void expressionTest() {
            Assert.assertTrue(new CheckerPredicate("?weather = SUNNY").process(new PredicateContext(new HashMapBuilder<String, String>().put("weather", "SUNNY").build())));
            Assert.assertTrue(new CheckerPredicate("(?weather = SUNNY AND ?time > 400) OR (?weather = STORMY)").process(new PredicateContext(new HashMapBuilder<String, String>().put("weather", "STORMY").put("time", "300").build())));
            Assert.assertFalse(new CheckerPredicate("(?weather = SUNNY AND ?time > 400) OR (?weather = STORMY)").process(new PredicateContext(new HashMapBuilder<String, String>().put("weather", "SUNNY").put("time", "200").build())));
        }

    }


}
