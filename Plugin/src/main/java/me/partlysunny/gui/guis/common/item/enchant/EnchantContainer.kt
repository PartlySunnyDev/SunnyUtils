package me.partlysunny.gui.guis.common.item.enchant

import org.bukkit.enchantments.Enchantment

class EnchantContainer(private var enchant: Enchantment?, private var lvl: Int) {
    fun enchant(): Enchantment? {
        return enchant
    }

    fun setEnchant(enchant: Enchantment?) {
        this.enchant = enchant
    }

    fun lvl(): Int {
        return lvl
    }

    fun setLvl(lvl: Int) {
        this.lvl = lvl
    }
}
