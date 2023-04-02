package me.partlysunny.util.classes

class Pair<A, B>(private var a: A, private var b: B) {
    fun a(): A? {
        return a
    }

    fun setA(a: A) {
        this.a = a
    }

    fun b(): B? {
        return b
    }

    fun setB(b: B) {
        this.b = b
    }

    fun flushNulls(repA: A, repB: B) {
        if (a() == null) setA(repA)
        if (b() == null) setB(repB)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj !is Pair<*, *>) {
            false
        } else obj.a == a && obj.b == b
    }
}
