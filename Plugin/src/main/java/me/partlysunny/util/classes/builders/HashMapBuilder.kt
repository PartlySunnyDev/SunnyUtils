package me.partlysunny.util.classes.builders;

import java.util.HashMap;

public class HashMapBuilder<K, V> {

    private final HashMap<K, V> internal = new HashMap<>();

    public static <A, B> HashMapBuilder<A, B> builder(Class<A> a, Class<B> b) {
        return new HashMapBuilder<>();
    }

    public HashMapBuilder<K, V> put(K key, V value) {
        internal.put(key, value);
        return this;
    }

    public HashMap<K, V> build() {
        return internal;
    }

}
