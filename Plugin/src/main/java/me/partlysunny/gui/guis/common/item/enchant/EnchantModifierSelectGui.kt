package me.partlysunny.gui.guis.common.item.enchant

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import me.partlysunny.gui.GuiManager
import me.partlysunny.gui.SelectGui
import me.partlysunny.gui.SelectGuiManager
import me.partlysunny.util.IFUtil
import me.partlysunny.util.Util
import me.partlysunny.util.classes.builders.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class EnchantModifierSelectGui : SelectGui<ItemStack>() {
    override fun getGui(p: HumanEntity): Gui {
        if (p !is Player) return ChestGui(3, "")
        val gui = ChestGui(5, ChatColor.DARK_AQUA.toString() + "Enchant Modifier")
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        val pane = PaginatedPane(0, 0, 9, 5)
        val displaySize = 21
        val a: MutableList<EnchantContainer> = ArrayList()
        val enchantments = getValue(p.getUniqueId())!!.enchantments
        for (e in enchantments.keys) {
            a.add(EnchantContainer(e, enchantments[e]!!))
        }
        var numPages = Math.ceil((a.size / (displaySize * 1f)).toDouble()).toInt()
        if (numPages == 0) {
            numPages = 1
        }
        var count = 0
        for (i in 0 until numPages) {
            val border = StaticPane(0, 0, 9, 5)
            val items = StaticPane(1, 1, 7, 3)
            IFUtil.addPageNav(pane, numPages, i, border, gui)
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.GREEN_CONCRETE)
                        .setName(ChatColor.GREEN.toString() + "Add new").build()
                ) {
                    val enchantCreation =
                        SelectGuiManager.getSelectGui("enchantCreation") as SelectGui<EnchantContainer?>
                    enchantCreation.setReturnTo(p.getUniqueId(), "enchantModifierSelect")
                    GuiManager.openInventory(p, "enchantCreationSelect")
                }, 1, 0
            )
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.YELLOW_CONCRETE)
                        .setName(ChatColor.GOLD.toString() + "Reload").build()
                ) { item: InventoryClickEvent? -> GuiManager.openInventory(p, "enchantModifierSelect") }, 2, 0
            )
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.BLUE_CONCRETE).setName(ChatColor.BLUE.toString() + "Update")
                        .build()
                ) { item: InventoryClickEvent? ->
                    p.sendMessage(ChatColor.GREEN.toString() + "Updated!")
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    GuiManager.openInventory(p, getReturnTo(p))
                }, 8, 2
            )
            items.fillWith(ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE).setName("").build())
            for (j in count until count + displaySize) {
                if (j > a.size - 1) {
                    break
                }
                val container = a[j]
                val enchantAsItem: ItemStack = ItemBuilder.builder(Material.ENCHANTED_BOOK)
                    .addEnchantment(container.enchant(), container.lvl()).build()
                Util.addLoreLine(enchantAsItem, ChatColor.RED.toString() + "Right click to delete!")
                Util.addLoreLine(enchantAsItem, ChatColor.GREEN.toString() + "Left click to edit!")
                items.addItem(GuiItem(enchantAsItem) { item: InventoryClickEvent ->
                    if (item.isRightClick) {
                        getValue(p.getUniqueId())!!.removeEnchantment(container.enchant()!!)
                        GuiManager.openInventory(p, "enchantModifierSelect")
                    }
                    if (item.isLeftClick) {
                        val enchantCreation =
                            SelectGuiManager.getSelectGui("enchantCreation") as SelectGui<EnchantContainer?>
                        enchantCreation.setReturnTo(p.getUniqueId(), "enchantModifierSelect")
                        enchantCreation.openWithValue(p, container, "enchantCreationSelect")
                    }
                }, (j - count) % 7, (j - count) / 7)
            }
            count += displaySize
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.ARROW).setName(ChatColor.GREEN.toString() + "Back").build()
                ) { item: InventoryClickEvent? ->
                    resetValue(p.getUniqueId())
                    GuiManager.openInventory(p, getReturnTo(p))
                }, 0, 4
            )
            pane.addPane(i, border)
            pane.addPane(i, items)
        }
        gui.addPane(pane)
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        return gui
    }

    fun addEnchantTo(id: UUID?, c: EnchantContainer?) {
        getValue(id!!)!!.addUnsafeEnchantment(c!!.enchant()!!, c.lvl())
    }

    override fun getValueFromString(s: String): ItemStack {
        return ItemStack(Material.AIR)
    }
}
