package me.partlysunny.command

import me.partlysunny.ConsoleLogger
import me.partlysunny.command.annotations.*
import me.partlysunny.util.reflection.JavaAccessor
import org.bukkit.ChatColor
import org.bukkit.command.*
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import java.lang.reflect.Method
import java.util.*
import java.util.stream.Collectors
import me.partlysunny.command.annotations.Command as CustomCommand

@SuppressWarnings("unchecked")
class CommandManager(plugin: Plugin) : CommandExecutor, TabCompleter {


    private val mainPlugin: Plugin = plugin
    private var commandMap: CommandMap? = null

    // We need to initialise the command map using reflection
    // Get the command map field from the plugin manager
    init {
        if (plugin.server.pluginManager !is SimplePluginManager) {
            throw IllegalArgumentException("PluginManager is not an instance of SimplePluginManager!")
        }
        val field = JavaAccessor.getField(SimplePluginManager::class.java, "commandMap")
        if (field != null) {
            field.isAccessible = true
            commandMap = field.get(plugin.server.pluginManager) as CommandMap
        }
    }

    // Maps of commands to their respective methods and instances
    private val commands: MutableMap<Command, Pair<Method, Any>> = mutableMapOf()
    private val subcommands: MutableMap<Command, MutableMap<String, Triple<Command, Method, Any>>> = mutableMapOf()

    // Maps of commands and their argument bounds
    private val commandArgBounds: MutableMap<Command, Triple<Int, Int, String>> = mutableMapOf()
    private val subcommandArgBounds: MutableMap<Command, MutableMap<String, Triple<Int, Int, String>>> = mutableMapOf()

    // Maps of commands and their required permissions
    private val commandPermissions: MutableMap<Command, MutableList<Pair<String, String>>> = mutableMapOf()
    private val subcommandPermissions: MutableMap<Command, MutableMap<String, MutableList<Pair<String, String>>>> =
        mutableMapOf()

    // Subcommand autocompletions, not to be confused with subcommand command completions
    // Subcommand autocompletions are for the subcommand name, not the arguments
    // e.g. /test spook <player> -> spook is the subcommand name, and will be autocompleted
    // e.g. /test spook <player> -> <player> is the subcommand argument, and will be autocompleted by the command completion
    private val subcommandAutocomplete: MutableMap<Command, MutableList<String>> = mutableMapOf()

    // These are the command completions for the arguments of commands / subcommands
    private val subcommandCommandCompletions: MutableMap<Command, MutableMap<String, Pair<Method, Any>>> =
        mutableMapOf()
    private val commandCompletions: MutableMap<Command, Pair<Method, Any>> = mutableMapOf()

    // These are the command cooldown times
    private val commandCooldowns: MutableMap<Command, Pair<Long, String>> = mutableMapOf()
    private val subcommandCooldowns: MutableMap<Command, MutableMap<String, Pair<Long, String>>> = mutableMapOf()

    // These are the command cooldown times currently in use
    private val commandCurrentCooldowns: MutableMap<UUID, MutableMap<Command, Long>> = mutableMapOf()
    private val subcommandCurrentCooldowns: MutableMap<UUID, MutableMap<Command, MutableMap<String, Long>>> =
        mutableMapOf()

    fun register(objInst: Any) {
        // Loop through all methods in the class
        JavaAccessor.getMethods(objInst.javaClass).forEach { method ->
            // Check if the method has the @Command annotation
            if (method.isAnnotationPresent(CustomCommand::class.java)) {
                // Get the annotation
                val command = method.getAnnotation(CustomCommand::class.java)
                // Create a new PluginCommand
                val pluginCommand = JavaAccessor.instance(
                    JavaAccessor.getConstructor(
                        PluginCommand::class.java,
                        String::class.java,
                        Plugin::class.java
                    ), command.commandName, mainPlugin
                ) as PluginCommand
                // Set the description, usage, and aliases
                pluginCommand.description = command.desc
                pluginCommand.usage = command.usage
                pluginCommand.aliases = command.aliases.toList()
                // Set the executor and tab completer
                pluginCommand.setExecutor(this)
                pluginCommand.tabCompleter = this
                // Add the command to the commands map
                commands[pluginCommand] = Pair(method, objInst)
                commandMap?.register(mainPlugin.description.name, pluginCommand)
                // Check if the method has the @ArgLengthBounds annotation
                // If it does, add it to the commandArgBounds map
                if (method.isAnnotationPresent(ArgLengthBounds::class.java)) {
                    val bounds = method.getAnnotation(ArgLengthBounds::class.java)
                    commandArgBounds[pluginCommand] = Triple(bounds.minArgs, bounds.maxArgs, bounds.errorMessage)
                }
                // Check if the method has the @Permission annotation
                // If it does, add it to the commandPermissions map
                if (method.isAnnotationPresent(Permission::class.java)) {
                    val permission = method.getAnnotationsByType(Permission::class.java)
                    commandPermissions[pluginCommand] = Arrays.stream(permission)
                        .map { Pair(it.permission, it.errorMessage) }
                        .toList() as MutableList<Pair<String, String>>
                }
                // Check if the method has the @Cooldown annotation
                // If it does, add it to the commandCooldowns map
                if (method.isAnnotationPresent(Cooldown::class.java)) {
                    val cooldown = method.getAnnotation(Cooldown::class.java)
                    commandCooldowns[pluginCommand] = Pair(cooldown.length, cooldown.message)
                }
            }
            // Check if the method has the @Subcommand annotation
            else if (method.isAnnotationPresent(Subcommand::class.java)) {
                // Get the annotation
                val subcommand = method.getAnnotation(Subcommand::class.java)
                // Create a new PluginCommand
                val subcommandObj = JavaAccessor.instance(
                    JavaAccessor.getConstructor(
                        PluginCommand::class.java,
                        String::class.java,
                        Plugin::class.java
                    ), subcommand.commandName, mainPlugin
                ) as PluginCommand
                // Set the description, usage, and aliases
                subcommandObj.description = subcommand.desc
                subcommandObj.usage = subcommand.usage
                subcommandObj.setAliases(subcommand.aliases.toList())
                // Find the parent command (which should be registered)
                val parentCommand = mainPlugin.server.getPluginCommand(subcommand.parent)
                // If parent command is null, throw an exception
                if (parentCommand != null) {
                    // Add the subcommand to the subcommands map
                    if (subcommands.containsKey(subcommandObj)) {
                        subcommands[parentCommand]!![subcommand.commandName] = Triple(subcommandObj, method, objInst)
                    } else {
                        subcommands[parentCommand] =
                            mutableMapOf(subcommand.commandName to Triple(subcommandObj, method, objInst))
                    }
                    // Check if the method has the @ArgLengthBounds annotation
                    // If it does, add it to the subcommandArgBounds map
                    if (method.isAnnotationPresent(ArgLengthBounds::class.java)) {
                        val bounds = method.getAnnotation(ArgLengthBounds::class.java)
                        if (subcommandArgBounds.containsKey(parentCommand)) {
                            subcommandArgBounds[parentCommand]!![subcommand.commandName] =
                                Triple(bounds.minArgs, bounds.maxArgs, bounds.errorMessage)
                        } else {
                            subcommandArgBounds[parentCommand] =
                                mutableMapOf(
                                    subcommand.commandName to Triple(
                                        bounds.minArgs,
                                        bounds.maxArgs,
                                        bounds.errorMessage
                                    )
                                )
                        }
                    }
                    // Check if the method has the @Permission annotation
                    // If it does, add it to the subcommandPermissions map
                    if (method.isAnnotationPresent(Permission::class.java)) {
                        val permission = method.getAnnotationsByType(Permission::class.java)
                        if (subcommandPermissions.containsKey(parentCommand)) {
                            subcommandPermissions[parentCommand]!![subcommand.commandName] =
                                Arrays.stream(permission)
                                    .map { Pair(it.permission, it.errorMessage) }
                                    .toList() as MutableList<Pair<String, String>>
                        } else {
                            subcommandPermissions[parentCommand] =
                                mutableMapOf(subcommand.commandName to Arrays.stream(permission)
                                    .map { Pair(it.permission, it.errorMessage) }
                                    .toList() as MutableList<Pair<String, String>>)
                        }
                    }
                    // Check if the method has the @Autocomplete annotation
                    // If it does, add it to the subcommandAutocomplete map
                    if (method.isAnnotationPresent(Autocomplete::class.java)) {
                        if (subcommandAutocomplete.containsKey(parentCommand)) {
                            subcommandAutocomplete[parentCommand]!!.add(subcommand.commandName)
                        } else {
                            subcommandAutocomplete[parentCommand] = mutableListOf(subcommand.commandName)
                        }
                    }
                    // Check if the method has the @Cooldown annotation
                    // If it does, add it to the subcommandCooldowns map
                    if (method.isAnnotationPresent(Cooldown::class.java)) {
                        val cooldown = method.getAnnotation(Cooldown::class.java)
                        if (subcommandCooldowns.containsKey(parentCommand)) {
                            subcommandCooldowns[parentCommand]!![subcommand.commandName] =
                                Pair(cooldown.length, cooldown.message)
                        } else {
                            subcommandCooldowns[parentCommand] =
                                mutableMapOf(subcommand.commandName to Pair(cooldown.length, cooldown.message))
                        }
                    } else if (!method.isAnnotationPresent(InheritCooldown::class.java)) {
                        if (subcommandCooldowns.containsKey(parentCommand)) {
                            subcommandCooldowns[parentCommand]!![subcommand.commandName] =
                                Pair(0, "")
                        } else {
                            subcommandCooldowns[parentCommand] =
                                mutableMapOf(subcommand.commandName to Pair(0, ""))
                        }
                    }
                } else {
                    ConsoleLogger.error("Issue registering subcommand ${subcommand.commandName}! Parent command ${subcommand.parent} does not exist!")
                    ConsoleLogger.error("Possible fix: Make sure the parent command is registered before the subcommand!")
                }
            }

        }
    }

    private fun permissionCheck(sender: CommandSender, perms: MutableList<Pair<String, String>>): Boolean {
        // Checks if the sender has all the permissions if not, print respective error message
        if (sender is Player) {
            for (permission in perms) {
                if (!sender.hasPermission(permission.first)) {
                    sender.sendMessage("${ChatColor.RED}${permission.second}")
                    return false
                }
            }
        }
        return true
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        ConsoleLogger.console("Command cooldowns: $commandCurrentCooldowns")
        ConsoleLogger.console("Subcommand cooldowns: $subcommandCurrentCooldowns")
        // If the command has subcommands then check if the first argument is a subcommand
        if (subcommands.containsKey(command)) {
            if (args.isNotEmpty()) {
                // This means a subcommand was used, find the subcommand and execute it
                if (subcommands[command]!!.containsKey(args[0])) {
                    val subcommand = subcommands[command]!![args[0]]!!
                    //
                    // ----------- COOLDOWN LOGIC ------------
                    //
                    // Denotes whether this subcommand has its own cooldown or not
                    // If it doesn't, it will inherit the cooldown from the parent command
                    var doesHaveSubcommandCooldown = subcommandCooldowns.containsKey(command)
                    if (doesHaveSubcommandCooldown) {
                        val cooldown = subcommandCooldowns[command]!![args[0]]
                        // Check if the sender is a player
                        if (sender is Player) {
                            if (cooldown != null) {
                                //Get the player cooldowns
                                if (!subcommandCurrentCooldowns.containsKey(sender.uniqueId)) {
                                    subcommandCurrentCooldowns[sender.uniqueId] = mutableMapOf(command to mutableMapOf())
                                }
                                val playerCooldowns = subcommandCurrentCooldowns[sender.uniqueId]!![command]!!
                                // Check if the player has a cooldown
                                if (playerCooldowns.containsKey(subcommand.first.name)) {
                                    // Check if the cooldown has expired
                                    if (playerCooldowns[subcommand.first.name]!! > System.currentTimeMillis()) {
                                        sender.sendMessage("${ChatColor.RED}${cooldown.second}")
                                        return true
                                    }
                                }
                                // If the player doesn't have a cooldown, add one
                                playerCooldowns[subcommand.first.name] = System.currentTimeMillis() + cooldown!!.first
                            } else {
                                doesHaveSubcommandCooldown = false
                            }
                        }
                    }
                    // If not then inherit the parent's cooldown
                    if (!doesHaveSubcommandCooldown && commandCooldowns.containsKey(command) && sender is Player) {
                        val parentCooldown = commandCooldowns[command]!!
                        // Check if the sender is a player
                        // Get the player cooldowns
                        if (!commandCurrentCooldowns.containsKey(sender.uniqueId)) {
                            commandCurrentCooldowns[sender.uniqueId] = mutableMapOf(command to 0)
                        }
                        val playerCooldowns = commandCurrentCooldowns[sender.uniqueId]!!
                        // Check if the player has a cooldown
                        if (playerCooldowns.containsKey(command)) {
                            // Check if the cooldown has expired
                            if (playerCooldowns[command]!! > System.currentTimeMillis()) {
                                // If not, send the player a message and return
                                sender.sendMessage("${ChatColor.RED}${parentCooldown.second}")
                                return true
                            }
                        }
                        // If the player doesn't have a cooldown, add one
                        playerCooldowns[command] = System.currentTimeMillis() + parentCooldown.first
                    }

                    // Check for arg bounds if the wrong amount of arguments was provided
                    if (subcommandArgBounds.containsKey(command)) {
                        if (subcommandArgBounds[command]!!.containsKey(args[0])) {
                            val bounds = subcommandArgBounds[command]!![args[0]]!!
                            if (args.size - 1 < bounds.first || args.size - 1 > bounds.second) {
                                sender.sendMessage("${ChatColor.RED}${bounds.third}")
                                return true
                            }
                        }
                    }
                    val commandArgs =
                        CommandArgs(sender, subcommand.first, args[0], args.slice(1 until args.size).toTypedArray())
                    // Check permissions
                    if (
                        !subcommandPermissions.containsKey(command) ||
                        !subcommandPermissions[command]!!.containsKey(subcommand.first.name) ||
                        permissionCheck(sender, subcommandPermissions[command]!![subcommand.first.name]!!)
                    ) {
                        // Execute the subcommand
                        subcommand.second.invoke(subcommand.third, commandArgs)
                    }
                } else {
                    sender.sendMessage("${ChatColor.RED}Invalid subcommand!")
                }
            }
            // This means that a subcommand was not used, so execute the parent command normally
            else {
                val commandArgs = CommandArgs(sender, command, label, args)
                val commandInfo = commands[command]!!
                // Check for cooldowns
                if (commandCooldowns.containsKey(command)) {
                    val cooldown = commandCooldowns[command]!!
                    // Check if the sender is a player
                    if (sender is Player) {
                        //Get the player cooldowns
                        if (!commandCurrentCooldowns.containsKey(sender.uniqueId)) {
                            commandCurrentCooldowns[sender.uniqueId] = mutableMapOf(command to 0)
                        }
                        val playerCooldowns = commandCurrentCooldowns[sender.uniqueId]!!
                        // Check if the player has a cooldown
                        if (playerCooldowns.containsKey(command)) {
                            // Check if the cooldown has expired
                            if (playerCooldowns[command]!! > System.currentTimeMillis()) {
                                // If not, send the player a message and return
                                sender.sendMessage("${ChatColor.RED}${cooldown.second}")
                                return true
                            }
                        }
                        // If the player doesn't have a cooldown, add one
                        playerCooldowns[command] = System.currentTimeMillis() + cooldown.first
                    }
                }
                // Check for arg bounds if the wrong amount of arguments was provided
                if (commandArgBounds.containsKey(command)) {
                    val bounds = commandArgBounds[command]!!
                    if (args.size < bounds.first || args.size > bounds.second) {
                        sender.sendMessage("${ChatColor.RED}${bounds.third}")
                        return true
                    }
                }
                // Check permissions
                if (!commandPermissions.containsKey(command) || permissionCheck(
                        sender,
                        commandPermissions[command]!!
                    )
                ) {
                    JavaAccessor.invoke(commandInfo.second, commandInfo.first, commandArgs)
                }
            }
        } else {
            // If the command does not have subcommands, execute it normally, but with arguments
            if (commands.containsKey(command)) {
                // Create arguments and check for arg bounds if the wrong amount of arguments was provided
                val commandArgs = CommandArgs(sender, command, label, args)
                val commandInfo = commands[command]!!
                // Check for cooldowns
                if (commandCooldowns.containsKey(command)) {
                    val cooldown = commandCooldowns[command]!!
                    // Check if the sender is a player
                    if (sender is Player) {
                        //Get the player cooldowns
                        val playerCooldowns = commandCurrentCooldowns[sender.uniqueId]!!
                        // Check if the player has a cooldown
                        if (playerCooldowns.containsKey(command)) {
                            // Check if the cooldown has expired
                            if (playerCooldowns[command]!! > System.currentTimeMillis()) {
                                // If not, send the player a message and return
                                sender.sendMessage("${ChatColor.RED}${cooldown.second}")
                                return true
                            }
                        }
                        // If the player doesn't have a cooldown, add one
                        playerCooldowns[command] = System.currentTimeMillis() + cooldown.first
                    }
                }
                if (commandArgBounds.containsKey(command)) {
                    val bounds = commandArgBounds[command]!!
                    if (args.size < bounds.first || args.size > bounds.second) {
                        sender.sendMessage("${ChatColor.RED}${bounds.third}")
                        return true
                    }
                }
                // Check permissions
                if (!commandPermissions.containsKey(command) || permissionCheck(
                        sender,
                        commandPermissions[command]!!
                    )
                ) {
                    JavaAccessor.invoke(commandInfo.second, commandInfo.first, commandArgs)
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        // Check if the command has subcommands
        if (subcommands.containsKey(command)) {
            // Check if you are trying to tab complete a subcommand
            if (args.size == 1) {
                // If you are, return a list of all subcommands
                // Stream is used to filter out subcommands that the sender does not have permission for
                return subcommandAutocomplete[command]!!.stream().filter { subcommand ->
                    if (subcommandPermissions.containsKey(command)) {
                        if (subcommandPermissions[command]!!.containsKey(subcommand)) {
                            permissionCheck(sender, subcommandPermissions[command]!![subcommand]!!)
                        } else {
                            true
                        }
                    } else {
                        true
                    }
                }.collect(Collectors.toList())
            } else {
                // If you are not, use the subcommand's tab completer
                if (subcommandCommandCompletions.containsKey(command)) {
                    if (subcommandCommandCompletions[command]!!.containsKey(args[0])) {
                        // Get the tab completer and execute it
                        // Filter out any results that do not start with the current input
                        val completer = subcommandCommandCompletions[command]!![args[0]]!!
                        return (completer.first.invoke(
                            completer.second,
                            sender,
                            args.size - 1
                        ) as MutableList<String>).stream().filter { t -> t.startsWith(args.last()) }
                            .collect(Collectors.toList())
                    }
                }
            }
        } else {
            // If the command does not have subcommands, check if it has a tab completer
            if (commandCompletions.containsKey(command)) {
                val completer = commandCompletions[command]!!
                // Execute the tab completer and filter out any results that do not start with the current input
                return (completer.first.invoke(
                    completer.second,
                    sender,
                    args.size - 1
                ) as MutableList<String>).stream().filter { t -> t.startsWith(args.last()) }
                    .collect(Collectors.toList())
            }
        }
        return mutableListOf()
    }

}