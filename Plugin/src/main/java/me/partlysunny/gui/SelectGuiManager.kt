package me.partlysunny.gui

import me.partlysunny.gui.guis.common.EnchantmentSelectGui
import me.partlysunny.gui.guis.common.EntityTypeSelectGui
import me.partlysunny.gui.guis.common.PotionEffectTypeSelectGui
import me.partlysunny.gui.guis.common.item.ItemMakerSelectGui
import me.partlysunny.gui.guis.common.item.enchant.EnchantCreationSelectGui
import me.partlysunny.gui.guis.common.item.enchant.EnchantModifierSelectGui

object SelectGuiManager {
    private val selectGuis: MutableMap<String, SelectGui<*>> = HashMap()
    fun registerSelectGui(id: String, selectGui: SelectGui<*>) {
        selectGuis[id] = selectGui
        GuiManager.registerGui(id + "Select", selectGui)
    }

    fun getSelectGui(id: String): SelectGui<*> {
        return selectGuis[id]!!
    }

    fun unregisterSelectGui(id: String) {
        selectGuis.remove(id)
    }

    fun init() {
        registerSelectGui("enchantment", EnchantmentSelectGui())
        registerSelectGui("entityType", EntityTypeSelectGui())
        registerSelectGui("potionEffectType", PotionEffectTypeSelectGui())
        registerSelectGui("itemMaker", ItemMakerSelectGui())
        registerSelectGui("enchantModifier", EnchantModifierSelectGui())
        registerSelectGui("enchantCreation", EnchantCreationSelectGui())
    }
}
