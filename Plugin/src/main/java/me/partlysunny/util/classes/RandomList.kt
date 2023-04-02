package me.partlysunny.util.classes

import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class RandomCollectionObject<T>(val `object`: T, val weight: Double)

class RandomList<E> @JvmOverloads constructor(private val random: Random = Random()) :
    ArrayList<RandomCollectionObject<E>>() {

    constructor(c: Collection<RandomCollectionObject<E>>?) : this() {
        addAll(c!!)
    }

    fun add(e: E, chance: Double): Boolean {
        return this.add(RandomCollectionObject(e, chance))
    }

    fun remove(o: Any): Boolean {
        return (clone() as RandomList<*>).stream()
            .anyMatch { t: RandomCollectionObject<out Any?>? -> (t?.`object` == o) && remove(t) }
    }

    fun totalWeight(): Double {
        return stream().mapToDouble { obj: RandomCollectionObject<E> -> obj.weight }.sum()
    }

    fun raffle(): E {
        return raffle(this)
    }

    fun raffle(predicate: Predicate<RandomCollectionObject<E>?>?): E {
        val aux = stream()
            .filter(predicate)
            .collect(Collectors.toCollection<RandomCollectionObject<E>, RandomList<E>>({ RandomList() }))
        return raffle(aux)
    }

    private fun raffle(list: RandomList<E>): E {
        val auxMap: NavigableMap<Double, RandomCollectionObject<E>> = TreeMap()
        list.forEach { rco: RandomCollectionObject<E> ->
            var auxWeight = auxMap.values.stream().mapToDouble { obj: RandomCollectionObject<E> -> obj.weight }.sum()
            auxWeight += rco.weight
            auxMap[auxWeight] = rco
        }
        val totalWeight = list.random.nextDouble() * auxMap.values.stream()
            .mapToDouble { obj: RandomCollectionObject<E> -> obj.weight }.sum()
        return auxMap.ceilingEntry(totalWeight).value.`object`
    }

}
