package me.partlysunny.util.classes.builders

class HashMapBuilder<K, V> {
    private val internal = HashMap<K, V>()
    fun put(key: K, value: V): HashMapBuilder<K, V> {
        internal[key] = value
        return this
    }

    fun build(): HashMap<K, V> {
        return internal
    }

    companion object {
        fun <A, B> builder(a: Class<A>?, b: Class<B>?): HashMapBuilder<A, B> {
            return HashMapBuilder()
        }
    }
}
