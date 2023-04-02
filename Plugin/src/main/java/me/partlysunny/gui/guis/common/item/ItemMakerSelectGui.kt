package me.partlysunny.gui.guis.common.item

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import me.partlysunny.gui.GuiManager
import me.partlysunny.gui.SelectGui
import me.partlysunny.gui.SelectGuiManager
import me.partlysunny.gui.textInput.ChatListener
import me.partlysunny.util.IFUtil
import me.partlysunny.util.Util
import me.partlysunny.util.Util.splitLoreForLine
import me.partlysunny.util.classes.builders.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ItemMakerSelectGui : SelectGui<ItemStack>() {
    override fun getGui(p: HumanEntity): Gui {
        if (p !is Player) return ChestGui(3, "")
        val gui = ChestGui(3, ChatColor.GRAY.toString() + "Item Maker")
        val mainPane = StaticPane(0, 0, 9, 3)
        mainPane.fillWith(ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        val pId = p.getUniqueId()
        val hasPlayer = values.containsKey(pId)
        var current: ItemStack? = ItemStack(Material.WOODEN_AXE)
        val materialValue = SelectGuiManager.getSelectGui("material").getValue(pId) as Material
        if (hasPlayer) {
            if (materialValue != null) {
                values[p.getUniqueId()]!!.type = materialValue
                SelectGuiManager.getSelectGui("material").resetValue(pId)
            }
            current = getValue(pId)
        } else {
            if (materialValue != null) {
                values[pId] = ItemStack(materialValue)
                SelectGuiManager.getSelectGui("material").resetValue(pId)
            }
        }
        val mat = current!!.type
        val name = current.itemMeta!!.displayName
        var lore = current.itemMeta!!.lore
        if (lore == null) {
            lore = ArrayList()
        }
        IFUtil.addSelectionLink(
            mainPane,
            p,
            "itemMakerSelect",
            "materialSelect",
            ItemBuilder.Companion.builder(mat).setName(mat.name).build(),
            1,
            1
        )
        val finalCurrent = current
        mainPane.addItem(
            GuiItem(
                ItemBuilder.Companion.builder(Material.ENCHANTED_BOOK)
                    .setName(ChatColor.LIGHT_PURPLE.toString() + "Modify Enchants").build()
            ) { x: InventoryClickEvent? ->
                SelectGuiManager.getSelectGui("enchantModifier").setReturnTo(p.getUniqueId(), "itemMakerSelect")
                p.closeInventory()
                (SelectGuiManager.getSelectGui("enchantModifier") as SelectGui<ItemStack?>).openWithValue(
                    p,
                    finalCurrent,
                    "enchantModifierSelect"
                )
            }, 3, 1
        )
        IFUtil.addTextInputLink(
            mainPane,
            p,
            "itemMakerSelect",
            ChatColor.RED.toString() + "Input new item name:",
            ItemBuilder.Companion.builder(Material.PAPER).setName(ChatColor.GRAY.toString() + "Change Name")
                .setLore(ChatColor.GRAY.toString() + "Current Name: " + name).build(),
            5,
            1
        ) { pl: Player ->
            val hasValue = values.containsKey(pl.uniqueId)
            val input = Util.processText(ChatListener.Companion.getCurrentInput(pl))
            if (input!!.length < 2 || input.length > 30) {
                Util.invalid("Characters must be at least 2 and at most 29!", pl)
                return@addTextInputLink
            }
            if (!hasValue) {
                values[pl.uniqueId] = ItemStack(Material.WOODEN_AXE)
            }
            Util.setName(values[pl.uniqueId], input)
        }
        IFUtil.addTextInputLink(
            mainPane,
            p,
            "itemMakerSelect",
            ChatColor.RED.toString() + "Input new lore (will auto wrap):",
            ItemBuilder.Companion.builder(Material.PAPER).setName(ChatColor.GRAY.toString() + "Change Lore")
                .setLore(*lore.toTypedArray<String?>()).build(),
            7,
            1
        ) { pl: Player ->
            val hasValue = values.containsKey(pl.uniqueId)
            val input = Util.processText(ChatListener.Companion.getCurrentInput(pl))
            if (input!!.length < 2) {
                Util.invalid("Characters must be at least 2!", pl)
                return@addTextInputLink
            }
            if (!hasValue) {
                values[pl.uniqueId] = ItemStack(Material.WOODEN_AXE)
            }
            Util.setLore(values[pl.uniqueId], splitLoreForLine(input))
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
                GuiManager.openInventory(p, getReturnTo(p))
            }, 8, 1
        )
        IFUtil.setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, gui)
        gui.addPane(mainPane)
        return gui
    }

    override fun getValueFromString(s: String): ItemStack {
        return ItemStack(Material.AIR)
    }
}
