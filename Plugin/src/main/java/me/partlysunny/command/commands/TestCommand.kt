package me.partlysunny.command.commands

import me.partlysunny.ConsoleLogger
import me.partlysunny.command.CommandArgs
import me.partlysunny.command.annotations.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TestCommand {

    @Command("test", ["tost, tist, tast"], "Tests the test", "test")
    @Cooldown(2000, "Cannot test that fast!")
    @Permission("sunny.admin")
    fun testCommand(args: CommandArgs) {
        ConsoleLogger.console("Hello! This is a test!")
    }

    @Subcommand("test", "spook", ["speek", "spuuk", "spaak"], "Spooook the test", "test spook <player>")
    @Autocomplete
    @ArgLengthBounds(1, 1, "${ChatColor.COLOR_CHAR}4You must specify a player to spook!")
    @Permission("sunny.test.spook", "${ChatColor.COLOR_CHAR}cYou do not have permission to spook the test!")
    @Permission("sunny.admin")
    fun testSpookCommand(args: CommandArgs) {
        if (args.isSenderPlayer()) {
            val sender = args.getSender<Player>()
            sender.sendMessage("${ChatColor.RED}Spooked: ${args.getArguments()[0]}")
            val world = sender.world
            world.players.forEach { player ->
                if (player.name == args.getArguments()[0]) {
                    player.sendMessage("${ChatColor.RED}You have been spooked!")
                }
            }
        } else {
            ConsoleLogger.console("You must be a player to spook the others!")
            ConsoleLogger.console("If you were a player, you would have spooked: ${args.getArguments()[0]}")
        }
    }

    @Completion("test.spook")
    fun testSpookCompletion(sender: CommandSender, argIndex: Int): MutableList<String> {
        val completions = mutableListOf<String>()
        if (sender is Player) {
            sender.world.players.forEach { player ->
                completions.add(player.name)
            }
        }
        return completions
    }


}