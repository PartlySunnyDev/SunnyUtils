package me.partlysunny.gui.textInput

import me.partlysunny.SunnySpigotCore
import me.partlysunny.gui.GuiManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Consumer

class ChatListener : Listener {
    @EventHandler
    fun onChatMessage(e: AsyncPlayerChatEvent) {
        if (e.isAsynchronous) {
            val player = e.player.uniqueId
            if (typing.contains(player)) {
                e.isCancelled = true
                currentInput[player] = e.message
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(SunnySpigotCore::class.java), Runnable {
                    GuiManager.openInventory(e.player, lastGui[player])
                    lastGui.remove(player)
                }, 1)
                todos[player]!!.accept(e.player)
                typing.remove(player)
                todos.remove(player)
            }
        }
    }

    companion object {
        private val currentInput: MutableMap<UUID, String> = HashMap()
        private val lastGui: MutableMap<UUID, String> = HashMap()
        private val typing: MutableList<UUID> = ArrayList()
        private val todos: MutableMap<UUID, Consumer<Player>> = HashMap()
        fun startChatListen(p: Player, redirectGui: String, message: String?, toDo: Consumer<Player>) {
            p.sendMessage(message)
            typing.add(p.uniqueId)
            lastGui[p.uniqueId] = redirectGui
            currentInput.remove(p.uniqueId)
            todos[p.uniqueId] = toDo
        }

        fun getCurrentInput(p: Player): String? {
            val s = currentInput[p.uniqueId]
            currentInput.remove(p.uniqueId)
            return s
        }
    }
}
