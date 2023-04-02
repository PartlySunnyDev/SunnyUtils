package me.partlysunny.gui

import org.bukkit.entity.Player
import java.util.*

object GuiManager {
    private val guis: MutableMap<String?, GuiInstance> = HashMap()
    private val previousGuis: MutableMap<UUID, String?> = HashMap()
    private val currentGuis: MutableMap<UUID, String?> = HashMap()
    fun openInventory(p: Player, id: String?) {
        val guiInstance = guis[id] ?: return
        val uniqueId = p.uniqueId
        if (currentGuis.containsKey(uniqueId)) {
            previousGuis[uniqueId] = currentGuis[uniqueId]
        }
        currentGuis[uniqueId] = id
        guiInstance.openFor(p)
    }

    fun registerGui(id: String?, gui: GuiInstance) {
        guis[id] = gui
    }

    fun unregisterGui(id: String?) {
        guis.remove(id)
    }

    fun getPreviousGui(player: UUID): String? {
        return previousGuis[player]
    }

    fun getCurrentGui(player: UUID): String? {
        return currentGuis[player]
    }
}
