package me.partlysunny.gui

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import org.bukkit.entity.HumanEntity

interface GuiInstance {
    fun getGui(p: HumanEntity): Gui
    fun openFor(e: HumanEntity) {
        getGui(e).show(e)
    }
}
