package me.partlysunny.commands

import me.partlysunny.commands.subcommands.ISubCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class SCommand : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
        if (commandSender is Player) {
            if (!commandSender.hasPermission(Companion.command + ".admin")) {
                commandSender.sendMessage(ChatColor.RED.toString() + "You cannot use this command!")
                return true
            }
            if (strings.isEmpty()) {
                executeSubCommand("help", commandSender, arrayOf())
                return true
            }
            val subCommand = strings[0]
            val newArgs = ArrayList(listOf(*strings))
            newArgs.removeAt(0)
            if (!executeSubCommand(subCommand, commandSender, newArgs.toTypedArray<String?>())) {
                commandSender.sendMessage(ChatColor.RED.toString() + "That command does not exist!")
            }
        }
        return true
    }

    companion object {
        const val command = "sunny"
        var subCommands: MutableMap<String?, ISubCommand> = HashMap()
        fun registerSubCommand(c: ISubCommand) {
            subCommands[c.id] = c
        }

        fun executeSubCommand(id: String?, exe: CommandSender, args: Array<String?>?): Boolean {
            val subCommand = subCommands[id] ?: return false
            subCommand.execute(exe, args)
            return true
        }
    }
}
