package me.partlysunny.command.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subcommand(
    /**
     * Name of the parent command
     */
    val parent: String,
    /**
     * Name of the subcommand
     */
    val commandName: String,
    /**
     * Aliases of the subcommand
     */
    val aliases: Array<String> = [],
    /**
     * Description of the subcommand
     */
    val desc: String = "",
    /**
     * Usage of the subcommand
     */
    val usage: String = ""
)