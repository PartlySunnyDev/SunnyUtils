package me.partlysunny.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.stream.Collectors

class STabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1) {
            val arg = args[args.size - 1]
            return SCommand.subCommands.keys.stream()
                    .filter { s: String? -> arg.isEmpty() || s!!.startsWith(arg.lowercase()) }
                    .collect(Collectors.toList<String>())
        }
        return null
    }
}
