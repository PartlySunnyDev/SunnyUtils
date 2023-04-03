package me.partlysunny.command.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Permission(val permission: String, val errorMessage: String = "You do not have permission to use this command")
