package me.partlysunny.command.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    /**
     * Name of the command in question
     */
    val commandName: String,
    /**
     * Aliases to the command.
     * The original name does not need to be included
     * The original name is the subcommand or command in the path
     */
    val aliases: Array<String> = [],
    /**
     * Description of the command, will appear in /help
     */
    val desc: String = "",
    /**
     * Usage of the command, will appear in /help
     */
    val usage: String = "",
    /**
     * Who can send this command
     */
    val sender: SenderType = SenderType.BOTH
)