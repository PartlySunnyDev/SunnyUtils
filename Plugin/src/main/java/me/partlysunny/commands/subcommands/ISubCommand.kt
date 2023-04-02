package me.partlysunny.commands.subcommands

import org.bukkit.command.CommandSender

interface ISubCommand {
    val id: String
    val description: String
    val usage: String
    fun execute(executor: CommandSender, args: Array<String?>?)
}
