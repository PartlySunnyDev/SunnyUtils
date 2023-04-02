package me.partlysunny.util.classes.predicates

class PredicateContext {
    private val context = HashMap<String?, String?>()

    constructor()
    constructor(init: HashMap<String?, String?>?) {
        context.putAll(init!!)
    }

    operator fun get(key: String?): String? {
        return context[key]
    }

    operator fun set(key: String?, value: String?) {
        context[key] = value
    }

    fun clear() {
        context.clear()
    }
}
