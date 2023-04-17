package me.partlysunny.sunnyutils

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

object ConsoleLogger {
    private val log = JavaPlugin.getPlugin(SunnyUtilsCore.getInstance().plugin::class.java).logger

    @JvmStatic
    fun console(msg: String?) {
        ConsoleLogger.log.info(msg)
    }

    fun console(vararg msg: String?) {
        for (s in msg) {
            ConsoleLogger.log.info(s)
        }
    }

    @JvmStatic
    fun error(msg: String?) {
        ConsoleLogger.log.log(Level.SEVERE, msg)
    }

    fun error(vararg msg: String?) {
        for (s in msg) {
            ConsoleLogger.log.log(Level.SEVERE, s)
        }
    }

    fun warn(msg: String?) {
        ConsoleLogger.log.warning(msg)
    }

    fun warn(vararg msg: String?) {
        for (s in msg) {
            ConsoleLogger.log.warning(s)
        }
    }
}
