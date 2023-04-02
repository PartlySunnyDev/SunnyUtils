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
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import java.util.*

class PotionEffectTypeSelectGui : SelectGui<PotionEffectType?>() {
    override fun getGui(p: HumanEntity): Gui {
        if (p !is Player) return ChestGui(3, "")
        val gui = ChestGui(5, ChatColor.GRAY.toString() + "Select Potion Effect Type")
        val pane = PaginatedPane(0, 0, 9, 5)
        val potionEffectList = arrayOfNulls<String>(PotionEffectType.values().size)
        for ((count, e) in PotionEffectType.values().withIndex()) {
            potionEffectList[count] = e.key.key
        }
        IFUtil.addListPages(pane, p, this, 1, 1, 7, 3, Util.getAlphabetSorted(potionEffectList), gui)
        gui.addPane(pane)
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        return gui
    }

    override fun getValueFromString(s: String): PotionEffectType? {
        return PotionEffectType.getByKey(NamespacedKey.minecraft(s.lowercase(Locale.getDefault())))
    }
}
