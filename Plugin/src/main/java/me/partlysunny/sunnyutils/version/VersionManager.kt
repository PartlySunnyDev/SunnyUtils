package me.partlysunny.sunnyutils.version

import me.partlysunny.sunnyutils.ConsoleLogger.console
import me.partlysunny.sunnyutils.api.IModule
import me.partlysunny.sunnyutils.util.classes.ServerVersion
import me.partlysunny.sunnyutils.util.reflection.JavaAccessor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

class VersionManager(private val p: JavaPlugin, private val packageName: String = "me.partlysunny") {
    private var serverVersion: ServerVersion? = null
    private var module: IModule? = null

    @Throws(ReflectiveOperationException::class)
    fun load() {
        if (serverVersion == null) {
            throw ClassNotFoundException("Server version not found!")
        }
        module = loadModule<IModule>("Module")
    }

    @Throws(ReflectiveOperationException::class)
    private fun <T> loadModule(name: String): T? {
        return JavaAccessor.instance(Class.forName("$packageName.$serverVersion.$name")) as T
    }

    fun serverVersion(): ServerVersion? {
        return serverVersion
    }

    fun module(): IModule? {
        return module
    }

    fun enable() {
        module!!.enable(p)
    }

    fun disable() {
        module!!.disable(p)
    }

    fun checkServerVersion() {
        val versionString = Bukkit.getServer().javaClass.getPackage().name
        val mcVersion: String = try {
            versionString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]
        } catch (e: ArrayIndexOutOfBoundsException) {
            return
        }
        try {
            serverVersion = ServerVersion.valueOf(mcVersion)
        } catch (exc: IllegalArgumentException) {
            console("This NMS version isn't supported. ($mcVersion)!")
        }
    }

    fun getWorldMinHeight(world: World): Int {
        return try {
            world.minHeight
        } catch (ex: NoSuchMethodError) {
            0
        }
    }

    fun getWorldMaxHeight(world: World): Int {
        return try {
            world.maxHeight
        } catch (ex: NoSuchMethodError) {
            255
        }
    }
}
