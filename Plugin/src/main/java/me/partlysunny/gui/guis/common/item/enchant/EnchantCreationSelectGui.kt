package me.partlysunny.gui.guis.common.item.enchant

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import me.partlysunny.gui.GuiManager
import me.partlysunny.gui.SelectGui
import me.partlysunny.gui.SelectGuiManager
import me.partlysunny.util.IFUtil
import me.partlysunny.util.Util
import me.partlysunny.util.classes.builders.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class EnchantCreationSelectGui : SelectGui<EnchantContainer>() {
    override fun getGui(p: HumanEntity): Gui {
        if (p !is Player) return ChestGui(3, "")
        val gui = ChestGui(3, ChatColor.AQUA.toString() + "Enchant Creator")
        val b = SelectGuiManager.getSelectGui("enchantment").getValue(p.getUniqueId()) as Enchantment
        val a = values.containsKey(p.getUniqueId())
        if (b != null) {
            if (a) {
                val plValue = values[p.getUniqueId()]
                plValue!!.setEnchant(b)
            } else {
                values[p.getUniqueId()] = EnchantContainer(b, 0)
            }
            SelectGuiManager.getSelectGui("potionEffectType").resetValue(p.getUniqueId())
        }
        val c = if (a) values[p.getUniqueId()] else EnchantContainer(Enchantment.ARROW_DAMAGE, 0)
        values[p.getUniqueId()] = c!!
        val mainPane = StaticPane(0, 0, 9, 3)
        mainPane.fillWith(ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        IFUtil.addSelectionLink(
            mainPane,
            p,
            "enchantCreationSelect",
            "enchantmentSelect",
            ItemBuilder.Companion.builder(Material.ENCHANTED_BOOK).addEnchantment(c.enchant(), c.lvl()).build(),
            3,
            1
        )
        IFUtil.addTextInputLink(
            mainPane,
            p,
            "enchantCreationSelect",
            ChatColor.RED.toString() + "Enter enchant lvl or \"cancel\" to cancel",
            ItemBuilder.Companion.builder(Material.PAPER).setName(ChatColor.GRAY.toString() + "Change LVL")
                .setLore(ChatColor.DARK_GRAY.toString() + "Current lvl: " + c.lvl()).build(),
            5,
            1
        ) { pl: Player ->
            val hasValue = values.containsKey(pl.uniqueId)
            val currentInput = Util.getTextInputAsInt(pl)
            if (currentInput == null) {
                Util.invalid("Invalid value!", pl)
                return@addTextInputLink
            }
            if (hasValue) {
                values[pl.uniqueId]!!.setLvl(currentInput)
            } else {
                values[pl.uniqueId] = EnchantContainer(Enchantment.ARROW_DAMAGE, currentInput)
            }
        }
        mainPane.addItem(
            GuiItem(
                ItemBuilder.Companion.builder(Material.ARROW).setName(ChatColor.GREEN.toString() + "Back").build()
            ) { item: InventoryClickEvent? ->
                resetValue(p.getUniqueId())
                GuiManager.openInventory(p, getReturnTo(p))
            }, 0, 2
        )
        mainPane.addItem(
            GuiItem(
                ItemBuilder.Companion.builder(Material.GREEN_CONCRETE).setName(ChatColor.GREEN.toString() + "Confirm")
                    .build()
            ) { item: InventoryClickEvent? ->
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                (SelectGuiManager.getSelectGui("enchantModifier") as EnchantModifierSelectGui).addEnchantTo(
                    p.getUniqueId(),
                    getValue(p.getUniqueId())
                )
                GuiManager.openInventory(p, getReturnTo(p))
            }, 8, 1
        )
        gui.addPane(mainPane)
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        return gui
    }

    override fun getValueFromString(s: String): EnchantContainer {
        val values = s.split(" ++".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return EnchantContainer(Enchantment.getByKey(NamespacedKey.minecraft(values[0])), s.toInt())
    }
}
