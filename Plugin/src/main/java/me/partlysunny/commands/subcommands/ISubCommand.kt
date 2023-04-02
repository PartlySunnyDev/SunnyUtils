package me.partlysunny.commands.subcommands;

import org.bukkit.command.CommandSender;

public interface ISubCommand {

    String getId();

    String getDescription();

    String getUsage();

    void execute(CommandSender executor, String[] args);

}
