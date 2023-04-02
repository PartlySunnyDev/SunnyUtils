package me.partlysunny

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

object ConsoleLogger {
    private val log = JavaPlugin.getPlugin(SunnySpigotCore::class.java).logger

    @JvmStatic
    fun console(msg: String?) {
        log.info(msg)
    }

    fun console(vararg msg: String?) {
        for (s in msg) {
            log.info(s)
        }
    }

    @JvmStatic
    fun error(msg: String?) {
        log.log(Level.SEVERE, msg)
    }

    fun error(vararg msg: String?) {
        for (s in msg) {
            log.log(Level.SEVERE, s)
        }
    }

    fun warn(msg: String?) {
        log.warning(msg)
    }

    fun warn(vararg msg: String?) {
        for (s in msg) {
            log.warning(s)
        }
    }
}
