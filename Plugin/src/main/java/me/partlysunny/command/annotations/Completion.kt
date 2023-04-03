package me.partlysunny.command.annotations

/**
 * Annotation to a function to auto-complete a command.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Completion(
    /**
     * The path of the command you are completing
     */
    val path: String
)
