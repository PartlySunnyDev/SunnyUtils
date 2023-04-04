package me.partlysunny.command.annotations

/**
 * Annotate a command with this to add a cooldown
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
    /**
     * Length of cooldown in milliseconds
     */
    val length: Long,
    /**
     * Message to display if cooldown is not over
     */
    val message: String
)
