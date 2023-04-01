package me.partlysunny.commands;

import me.partlysunny.commands.subcommands.ISubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SCommand implements CommandExecutor {

    public static final String command = "sunny";
    public static Map<String, ISubCommand> subCommands = new HashMap<>();

    public static void registerSubCommand(ISubCommand c) {
        subCommands.put(c.getId(), c);
    }

    public static boolean executeSubCommand(String id, CommandSender exe, String[] args) {
        ISubCommand subCommand = subCommands.get(id);
        if (subCommand == null) {
            return false;
        }
        subCommand.execute(exe, args);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            if (!p.hasPermission(SCommand.command + ".admin")) {
                p.sendMessage(ChatColor.RED + "You cannot use this command!");
                return true;
            }
            if (strings.length == 0) {
                executeSubCommand("help", commandSender, new String[]{});
                return true;
            }
            String subCommand = strings[0];
            ArrayList<String> newArgs = new ArrayList<>(Arrays.asList(strings));
            newArgs.remove(0);
            if (!executeSubCommand(subCommand, commandSender, newArgs.toArray(new String[0]))) {
                p.sendMessage(ChatColor.RED + "That command does not exist!");
            }
        }
        return true;
    }

}
