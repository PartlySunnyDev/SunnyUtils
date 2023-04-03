package me.partlysunny.command

import me.partlysunny.ConsoleLogger
import me.partlysunny.command.annotations.ArgLengthBounds
import me.partlysunny.command.annotations.Permission
import me.partlysunny.command.annotations.Subcommand
import me.partlysunny.util.reflection.JavaAccessor
import org.bukkit.ChatColor
import org.bukkit.command.*
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import java.lang.reflect.Method
import java.util.*
import me.partlysunny.command.annotations.Command as CustomCommand

class CommandManager(plugin: Plugin) : CommandExecutor, TabCompleter {


    private val mainPlugin: Plugin = plugin
    private var commandMap: CommandMap? = null

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

    private val commands: MutableMap<Command, Pair<Method, Any>> = mutableMapOf()
    private val subcommands: MutableMap<Command, MutableMap<String, Triple<Command, Method, Any>>> = mutableMapOf()
    private val commandArgBounds: MutableMap<Command, Triple<Int, Int, String>> = mutableMapOf()
    private val subcommandArgBounds: MutableMap<Command, MutableMap<String, Triple<Int, Int, String>>> = mutableMapOf()
    private val commandPermissions: MutableMap<Command, MutableList<Pair<String, String>>> = mutableMapOf()
    private val subcommandPermissions: MutableMap<Command, MutableMap<String, MutableList<Pair<String, String>>>> =
        mutableMapOf()
    private val subcommandAutocomplete: MutableMap<Command, MutableList<String>> = mutableMapOf()
    private val subcommandCommandCompletions: MutableMap<Command, MutableMap<String, Pair<Method, Any>>> = mutableMapOf()
    private val commandCompletions: MutableMap<Command, Pair<Method, Any>> = mutableMapOf()

    fun register(objInst: Any) {
        JavaAccessor.getMethods(objInst.javaClass).forEach { method ->
            if (method.isAnnotationPresent(CustomCommand::class.java)) {
                val command = method.getAnnotation(CustomCommand::class.java)
                val pluginCommand = JavaAccessor.instance(
                    JavaAccessor.getConstructor(
                        PluginCommand::class.java,
                        String::class.java,
                        Plugin::class.java
                    ), command.commandName, mainPlugin
                ) as PluginCommand
                pluginCommand.description = command.desc
                pluginCommand.usage = command.usage
                pluginCommand.aliases = command.aliases.toList()
                pluginCommand.setExecutor(this)
                pluginCommand.tabCompleter = this
                commands[pluginCommand] = Pair(method, objInst)
                commandMap?.register(mainPlugin.description.name, pluginCommand)
                if (method.isAnnotationPresent(ArgLengthBounds::class.java)) {
                    val bounds = method.getAnnotation(ArgLengthBounds::class.java)
                    commandArgBounds[pluginCommand] = Triple(bounds.minArgs, bounds.maxArgs, bounds.errorMessage)
                }
                if (method.isAnnotationPresent(Permission::class.java)) {
                    val permission = method.getAnnotationsByType(Permission::class.java)
                    commandPermissions[pluginCommand] = Arrays.stream(permission)
                        .map { Pair(it.permission, it.errorMessage) }
                        .toList() as MutableList<Pair<String, String>>
                }
            } else if (method.isAnnotationPresent(Subcommand::class.java)) {
                val subcommand = method.getAnnotation(Subcommand::class.java)
                val subcommandObj = JavaAccessor.instance(
                    JavaAccessor.getConstructor(
                        PluginCommand::class.java,
                        String::class.java,
                        Plugin::class.java
                    ), subcommand.commandName, mainPlugin
                ) as PluginCommand
                subcommandObj.description = subcommand.desc
                subcommandObj.usage = subcommand.usage
                subcommandObj.setAliases(subcommand.aliases.toList())
                val parentCommand = mainPlugin.server.getPluginCommand(subcommand.parent)
                if (parentCommand != null) {
                    if (subcommands.containsKey(subcommandObj)) {
                        subcommands[parentCommand]!![subcommand.commandName] = Triple(subcommandObj, method, objInst)
                    } else {
                        subcommands[parentCommand] =
                            mutableMapOf(subcommand.commandName to Triple(subcommandObj, method, objInst))
                    }
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
                } else {
                    ConsoleLogger.error("Issue registering subcommand ${subcommand.commandName}! Parent command ${subcommand.parent} does not exist!")
                    ConsoleLogger.error("Possible fix: Make sure the parent command is registered before the subcommand!")
                }
            }

        }
    }

    private fun permissionCheck(sender: CommandSender, perms: MutableList<Pair<String, String>>): Boolean {
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
        if (subcommands.containsKey(command)) {
            if (args.isNotEmpty()) {
                if (subcommands[command]!!.containsKey(args[0])) {
                    val subcommand = subcommands[command]!![args[0]]!!
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
                    if (
                        !subcommandPermissions.containsKey(command) ||
                        !subcommandPermissions[command]!!.containsKey(subcommand.first.name) ||
                        permissionCheck(sender, subcommandPermissions[command]!![subcommand.first.name]!!)
                    ) {
                        subcommand.second.invoke(subcommand.third, commandArgs)
                    }
                }
            } else {
                val commandArgs = CommandArgs(sender, command, label, args)
                val commandInfo = commands[command]!!
                if (commandArgBounds.containsKey(command)) {
                    val bounds = commandArgBounds[command]!!
                    if (args.size < bounds.first || args.size > bounds.second) {
                        sender.sendMessage("${ChatColor.RED}${bounds.third}")
                        return true
                    }
                }
                if (!commandPermissions.containsKey(command) || permissionCheck(sender, commandPermissions[command]!!)) {
                    JavaAccessor.invoke(commandInfo.second, commandInfo.first, commandArgs)
                }
            }
        } else {
            if (commands.containsKey(command)) {
                val commandArgs = CommandArgs(sender, command, label, args)
                val commandInfo = commands[command]!!
                if (commandArgBounds.containsKey(command)) {
                    val bounds = commandArgBounds[command]!!
                    if (args.size < bounds.first || args.size > bounds.second) {
                        sender.sendMessage("${ChatColor.RED}${bounds.third}")
                        return true
                    }
                }
                if (!commandPermissions.containsKey(command) || permissionCheck(sender, commandPermissions[command]!!)) {
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
        return null
    }

}