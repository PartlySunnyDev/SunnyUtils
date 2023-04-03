package me.partlysunny.command.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArgLengthBounds(val minArgs: Int, val maxArgs: Int, val errorMessage: String = "Invalid number of arguments")
