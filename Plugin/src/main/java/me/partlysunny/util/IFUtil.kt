package me.partlysunny.util

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import me.partlysunny.ConsoleLogger.error
import me.partlysunny.gui.GuiManager
import me.partlysunny.gui.SelectGui
import me.partlysunny.gui.SelectGuiManager
import me.partlysunny.gui.textInput.ChatListener
import me.partlysunny.util.classes.Pair
import me.partlysunny.util.classes.builders.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.roundToInt

object IFUtil {
    fun setClickSoundTo(s: Sound?, gui: Gui) {
        s ?: return
        gui.setOnGlobalClick { event: InventoryClickEvent ->
            val whoClicked = event.whoClicked
            if (whoClicked is Player) {
                whoClicked.playSound(whoClicked.getLocation(), s, 1f, 1f)
            }
            event.isCancelled = true
        }
    }

    fun changePage(p: PaginatedPane, amount: Int) {
        val current = p.page
        val newAmount = current + amount
        if (newAmount < 0) p.page = 0 else p.page = newAmount.coerceAtMost(p.pages - 1)
    }

    fun addReturnButton(pane: StaticPane, p: Player, returnTo: String?, x: Int, y: Int) {
        pane.addItem(
            GuiItem(
                ItemBuilder.builder(Material.ARROW).setName(ChatColor.GREEN.toString() + "Back").build()
            ) { GuiManager.openInventory(p, returnTo) }, x, y
        )
    }

    fun addSelectionLink(
        pane: StaticPane,
        p: Player,
        currentGui: String?,
        selectionLink: String,
        toShow: ItemStack?,
        x: Int,
        y: Int
    ) {
        pane.addItem(GuiItem(toShow!!) {
            SelectGuiManager.getSelectGui(selectionLink.substring(0, selectionLink.length - 6))
                .setReturnTo(p.uniqueId, currentGui!!)
            p.closeInventory()
            GuiManager.openInventory(p, selectionLink)
        }, x, y)
    }

    fun addTextInputLink(
        pane: StaticPane,
        p: Player,
        currentGui: String,
        message: String?,
        toShow: ItemStack?,
        x: Int,
        y: Int,
        toDo: Consumer<Player>
    ) {
        pane.addItem(GuiItem(toShow!!) {
            ChatListener.Companion.startChatListen(p, currentGui, message, toDo)
            p.closeInventory()
        }, x, y)
    }

    @SafeVarargs
    fun getGeneralSelectionMenu(title: String?, p: Player, vararg items: Pair<String?, ItemStack?>): ChestGui {
        if (items.size > 9) {
            error("Too many items! (Max supported 9)")
        }
        val linspace = Util.fakeSpace(items.size)
        val ui = ChestGui(3, title!!)
        val pane = StaticPane(0, 0, 9, 3)
        pane.fillWith(ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE).build())
        setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, ui)
        for ((count, d) in linspace.withIndex()) {
            pane.addItem(
                GuiItem(
                    items[count].b()!!
                ) { GuiManager.openInventory(p, items[count].a()) }, d.roundToInt(), 1
            )
        }
        ui.addPane(pane)
        return ui
    }

    fun addListPages(
        pane: PaginatedPane,
        p: Player,
        from: SelectGui<*>,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        a: Array<String?>?,
        gui: ChestGui
    ) {
        pane.setOnClick { event: InventoryClickEvent ->
            val whoClicked = event.whoClicked
            if (whoClicked is Player) {
                whoClicked.playSound(whoClicked.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, 1f, 1f)
            }
            event.isCancelled = true
        }
        val displaySize = width * height
        if (displaySize < 1) {
            return
        }
        var numPages = ceil((a!!.size / (displaySize * 1f)).toDouble()).toInt()
        if (numPages == 0) {
            numPages = 1
        }
        var count = 0
        for (i in 0 until numPages) {
            val border = StaticPane(0, 0, 9, 5, Pane.Priority.HIGH)
            val items = StaticPane(x, y, width, height, Pane.Priority.HIGHEST)
            addPageNav(pane, numPages, i, border, gui)
            items.fillWith(ItemBuilder.Companion.builder(Material.GRAY_STAINED_GLASS_PANE).setName("").build())
            for (j in count until count + displaySize) {
                if (j > a.size - 1) {
                    break
                }
                val itemName = a[j]
                items.addItem(
                    GuiItem(
                        ItemBuilder.Companion.builder(Material.PAPER).setName(ChatColor.GRAY.toString() + itemName)
                            .build()
                    ) { item: InventoryClickEvent? ->
                        from.update(p.uniqueId, itemName!!)
                        from.returnTo(p)
                    }, (j - count) % width, (j - count) / width
                )
            }
            count += displaySize
            addReturnButton(border, p, from.getReturnTo(p), 0, 4)
            pane.addPane(i, border)
            pane.addPane(i, items)
        }
    }

    fun addPageNav(pane: PaginatedPane, numPages: Int, i: Int, border: StaticPane, gui: ChestGui) {
        border.fillWith(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).setName("").build())
        if (i != 0) {
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.ARROW).setName(ChatColor.GRAY.toString() + "Page Back").setLore(
                        ChatColor.GREEN.toString() + "Right click for 5 pages",
                        ChatColor.RED.toString() + "Shift Click for 15 pages"
                    ).build()
                ) { item: InventoryClickEvent ->
                    if (item.isShiftClick) changePage(pane, -15) else if (item.isLeftClick) changePage(
                        pane,
                        -1
                    ) else if (item.isRightClick) changePage(pane, -5)
                    gui.update()
                }, 0, 2
            )
        }
        if (i != numPages - 1) {
            border.addItem(
                GuiItem(
                    ItemBuilder.builder(Material.ARROW).setName(ChatColor.GRAY.toString() + "Page Forward").setLore(
                        ChatColor.GREEN.toString() + "Right click for 5 pages",
                        ChatColor.RED.toString() + "Shift Click for 15 pages"
                    ).build()
                ) { item: InventoryClickEvent ->
                    if (item.isShiftClick) changePage(pane, 15) else if (item.isLeftClick) changePage(
                        pane,
                        1
                    ) else if (item.isRightClick) changePage(pane, 5)
                    gui.update()
                }, 8, 2
            )
        }
    }
}
