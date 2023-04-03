package me.partlysunny.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class CommandArgs(
    private var commandSender: CommandSender?,
    private var command: Command?,
    private var label: String?,
    arguments: Array<out String>
) {

    private var arguments: Array<String> = arrayOf()

    init {
        this.arguments = arrayOf(*arguments)
    }

    /**
     * Do not try to cast objects except subclasses of [CommandSender]
     * otherwise [ClassCastException] will occur.
     *
     * @return sender of command as type specified
     */
    fun <T : CommandSender?> getSender(): T {
        return commandSender as T
    }

    /**
     * @return The command in question
     */
    fun getCommand(): Command? {
        return command
    }

    /**
     * @return The label of the command
     */
    fun getLabel(): String {
        return label!!
    }

    /**
     * @return Array of args
     */
    fun getArguments(): Array<String> {
        return arguments
    }

    /**
     * @param index Index of the arguments to query
     * @return indexed element or null if index out of bounds
     */
    fun getArgument(index: Int): String? {
        return if (arguments.size > index && index >= 0) arguments[index] else null
    }

    /**
     * @param index Index of the arguments to query
     * @return Integer if indexed element is primitive type of int
     * or 0 if element is null.
     */
    fun getArgumentAsInt(index: Int): Int {
        return getArgument(index)?.toInt() ?: 0
    }

    /**
     * @param index Index of the arguments to query
     * @return Double if indexed element is primitive type of double
     * or 0 if element is null.
     */
    fun getArgumentAsDouble(index: Int): Double {
        return getArgument(index)?.toDouble() ?: 0.0
    }

    /**
     * @param index Index of the arguments to query
     * @return Float if indexed element is primitive type of float
     * or 0 if element is null.
     */
    fun getArgumentAsFloat(index: Int): Float {
        return getArgument(index)?.toFloat() ?: 0f
    }

    /**
     * @param index Index of the arguments to query
     * @return Long if indexed element is primitive type of long
     * or 0 if element is null.
     */
    fun getArgumentAsLong(index: Int): Long {
        return getArgument(index)?.toLong() ?: 0
    }

    /**
     * @param index Index of the arguments to query
     * @return Boolean if indexed element is primitive type of boolean
     * or 0 if element is null.
     */
    fun getArgumentAsBoolean(index: Int): Boolean {
        val arg = getArgument(index)
        return arg != null && arg.equals("true", ignoreCase = true)
    }

    /**
     * @return true if there are no arguments otherwise false
     */
    fun isArgumentsEmpty(): Boolean {
        return arguments.isEmpty()
    }

    /**
     * Sends message to the command sender.
     *
     * @param message The message to send
     */
    fun sendMessage(message: String?) {
        if (message == null) return
        commandSender!!.sendMessage(message)
    }

    /**
     * Check if command sender is console.
     *
     * @return true if sender is console otherwise false
     */
    fun isSenderConsole(): Boolean {
        return !isSenderPlayer()
    }

    /**
     * Checks if command sender is player.
     *
     * @return true if sender is player otherwise false
     */
    fun isSenderPlayer(): Boolean {
        return commandSender is Player
    }

    /**
     * Checks if command sender has specified permission.
     *
     * @param permission to check
     * @return true if sender has permission otherwise false
     */
    fun hasPermission(permission: String?): Boolean {
        return commandSender!!.hasPermission(permission!!)
    }

    /**
     * @return length of the arguments
     */
    fun getArgumentsLength(): Int {
        return arguments.size
    }

}