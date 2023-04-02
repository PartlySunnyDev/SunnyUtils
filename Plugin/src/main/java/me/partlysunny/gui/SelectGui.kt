package me.partlysunny.gui

import org.bukkit.entity.Player
import java.util.*

abstract class SelectGui<T> : GuiInstance {
    protected val values: MutableMap<UUID, T> = HashMap()
    protected val guiToReturn: MutableMap<UUID, String> = HashMap()
    fun getValue(player: UUID): T? {
        return values[player]
    }

    fun returnTo(player: Player) {
        GuiManager.openInventory(player, guiToReturn[player.uniqueId])
    }

    fun resetValue(player: UUID) {
        values.remove(player)
    }

    fun update(player: UUID, value: String) {
        values[player] = getValueFromString(value)
    }

    fun setReturnTo(player: UUID, gui: String) {
        guiToReturn[player] = gui
    }

    fun getReturnTo(p: Player): String? {
        return guiToReturn[p.uniqueId]
    }

    fun openWithValue(p: Player, value: T, name: String?) {
        values[p.uniqueId] = value
        GuiManager.openInventory(p, name)
    }

    protected abstract fun getValueFromString(s: String): T
}
