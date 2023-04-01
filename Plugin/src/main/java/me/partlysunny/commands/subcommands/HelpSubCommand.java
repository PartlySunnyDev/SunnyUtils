package me.partlysunny.commands.subcommands;

import me.partlysunny.commands.SCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class HelpSubCommand implements ISubCommand {
    @Override
    public String getId() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get a list of all commands.";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public void execute(CommandSender executor, String[] args) {
        Collection<ISubCommand> commands = SCommand.subCommands.values();
        executor.sendMessage(ChatColor.YELLOW + "List of commands (run with /sbl <command>):");
        for (ISubCommand c : commands) {
            executor.sendMessage(ChatColor.WHITE + c.getId() + c.getUsage() + ": " + c.getDescription());
        }
    }
}
