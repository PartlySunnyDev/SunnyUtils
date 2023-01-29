package me.partlysunny.util.classes.predicates;

import java.util.HashMap;

public class PredicateContext {

    private final HashMap<String, String> context = new HashMap<>();

    public PredicateContext() {}

    public PredicateContext(HashMap<String, String> init) {
        context.putAll(init);
    }

    public String get(String key) {
        return context.get(key);
    }

    public void set(String key, String value) {
        context.put(key, value);
    }

    public void clear() {
        context.clear();
    }
}
