package me.partlysunny.commands.subcommands

import me.partlysunny.commands.SCommand
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class HelpSubCommand : ISubCommand {
    override val id: String
        get() = "help"
    override val description: String
        get() = "Get a list of all commands."
    override val usage: String
        get() = ""

    override fun execute(executor: CommandSender, args: Array<String?>?) {
        val commands: Collection<ISubCommand> = SCommand.Companion.subCommands.values
        executor.sendMessage(ChatColor.YELLOW.toString() + "List of commands (run with /sbl <command>):")
        for (c in commands) {
            executor.sendMessage(ChatColor.WHITE.toString() + c.id + c.usage + ": " + c.description)
        }
    }
}
