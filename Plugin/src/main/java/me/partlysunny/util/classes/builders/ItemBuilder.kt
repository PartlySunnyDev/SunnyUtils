package me.partlysunny.util.classes.builders

import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemBuilder {
    private val meta: ItemMeta?
    private val s: ItemStack
    private val enchants: MutableMap<Enchantment?, Int?> = HashMap()
    private val nbti: NBTItem

    constructor(m: Material?) {
        s = ItemStack(m!!)
        nbti = NBTItem(s)
        meta = s.itemMeta
    }

    constructor(s: ItemStack) {
        this.s = s.clone()
        meta = s.itemMeta?.clone()
        nbti = NBTItem(this.s)
    }

    fun setNbtTag(key: String?, value: Any?): ItemBuilder {
        nbti.setObject(key, value)
        return this
    }

    fun setName(name: String?): ItemBuilder {
        meta?.setDisplayName(name)
        return this
    }

    fun setLore(vararg lore: String?): ItemBuilder {
        if (meta != null) meta.lore = listOf(*lore)
        return this
    }

    fun addEnchantment(e: Enchantment?, level: Int): ItemBuilder {
        enchants[e] = level
        return this
    }

    fun addEnchantment(bundle: EnchantBundle): ItemBuilder {
        enchants.putAll(bundle.bundle())
        return this
    }

    fun setUnbreakable(u: Boolean): ItemBuilder {
        if (meta != null) meta.isUnbreakable = u
        return this
    }

    fun build(): ItemStack {
        if (meta != null) s.setItemMeta(meta)
        for (m in enchants.keys) {
            s.addUnsafeEnchantment(m!!, enchants[m]!!)
        }
        nbti.mergeCustomNBT(s)
        return s
    }

    fun setAmount(amount: Int): ItemBuilder {
        s.amount = amount
        return this
    }

    companion object {
        fun builder(m: Material?): ItemBuilder {
            return ItemBuilder(m)
        }

        fun builder(i: ItemStack): ItemBuilder {
            return ItemBuilder(i)
        }
    }
}
