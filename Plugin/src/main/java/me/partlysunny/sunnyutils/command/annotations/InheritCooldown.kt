package me.partlysunny.sunnyutils.command.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class InheritCooldown(val shareCooldown: Boolean = true)
