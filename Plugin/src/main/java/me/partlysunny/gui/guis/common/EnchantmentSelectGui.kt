package me.partlysunny.gui.guis.common

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import me.partlysunny.gui.SelectGui
import me.partlysunny.util.IFUtil
import me.partlysunny.util.Util
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import java.util.*

class EnchantmentSelectGui : SelectGui<Enchantment?>() {
    override fun getGui(p: HumanEntity): Gui {
        if (p !is Player) return ChestGui(3, "")
        val gui = ChestGui(5, ChatColor.GRAY.toString() + "Select Enchantment")
        val pane = PaginatedPane(0, 0, 9, 5)
        val enchantList = arrayOfNulls<String>(Enchantment.values().size)
        var count = 0
        for (e in Enchantment.values()) {
            enchantList[count] = e.key.key
            count++
        }
        IFUtil.addListPages(pane, p, this, 1, 1, 7, 3, Util.getAlphabetSorted(enchantList), gui)
        gui.addPane(pane)
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        return gui
    }

    override fun getValueFromString(s: String): Enchantment? {
        return Enchantment.getByKey(NamespacedKey.minecraft(s.lowercase(Locale.getDefault())))
    }
}
