package me.partlysunny.util.classes.builders

import jline.internal.Nullable
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType
import java.util.*

class PotionBuilder(f: PotionFormat?) {
    private val toReturn: ItemStack
    private val meta: PotionMeta?

    init {
        var m = when (f) {
            PotionFormat.SPLASH -> Material.SPLASH_POTION
            PotionFormat.LINGERING -> Material.LINGERING_POTION
            PotionFormat.POTION -> Material.POTION
            null -> throw IllegalStateException("Invalid potion state found!")
        }
        toReturn = ItemStack(m)
        meta = toReturn.itemMeta as PotionMeta?
    }

    fun setName(name: String?): PotionBuilder {
        meta!!.setDisplayName(name)
        return this
    }

    fun setLore(vararg lore: String?): PotionBuilder {
        meta!!.lore = Arrays.asList(*lore)
        return this
    }

    fun setPotionData(t: PotionType?, @Nullable color: Color?): PotionBuilder {
        if (t != null) {
            meta!!.basePotionData = PotionData(t)
        }
        if (color != null) {
            meta!!.color = color
        }
        return this
    }

    fun addCustomEffect(e: PotionEffect?): PotionBuilder {
        meta!!.addCustomEffect(e!!, true)
        return this
    }

    fun build(): ItemStack {
        toReturn.setItemMeta(meta)
        return toReturn
    }

    enum class PotionFormat {
        SPLASH,
        POTION,
        LINGERING
    }

    companion object {
        fun builder(f: PotionFormat?): PotionBuilder {
            return PotionBuilder(f)
        }
    }
}
