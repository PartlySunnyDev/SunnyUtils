package me.partlysunny

import me.partlysunny.command.CommandManager
import me.partlysunny.command.commands.TestCommand
import me.partlysunny.gui.SelectGuiManager
import me.partlysunny.gui.textInput.ChatListener
import me.partlysunny.util.Util
import me.partlysunny.version.Version
import me.partlysunny.version.VersionManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

class SunnySpigotCore : JavaPlugin() {

    private var versionManager: VersionManager? = null
    private var commandManager: CommandManager? = null

    private fun reload() {

    }

    override fun onEnable() {
        //Get version
        val v = Version(this.server.version)
        ConsoleLogger.console("Enabling SunnySpigotCore...")
        //Load modules
        versionManager = VersionManager(this)
        versionManager!!.checkServerVersion()
        try {
            versionManager!!.load()
        } catch (e: ReflectiveOperationException) {
            ConsoleLogger.error(
                "This version (" + v.get() + ") is not supported by SunnySpigotBase!",
                "Shutting down plugin..."
            )
            isEnabled = false
            return
        }
        versionManager!!.enable()
        //Copy in default files if not existent
        //Copy in default files if not existent
        try {
            initDefaults()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        //Register command
        registerCommands()
        registerListeners()
        reload()
        registerGuis()
        ConsoleLogger.console("Enabled SunnySpigotBase on version " + v.get())
    }

    override fun onDisable() {
        ConsoleLogger.console("Disabling SunnySpigotBase...")
        versionManager?.disable()
    }

    private fun registerGuis() {
        SelectGuiManager.init()
    }

    private fun registerCommands() {
        commandManager = CommandManager(this)
        commandManager!!.register(TestCommand())
    }

    @Throws(IOException::class)
    private fun initDefaults() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        //Initialise default config
        copyFileWithName("config.yml")
    }

    @Throws(IOException::class)
    private fun copyFileWithName(key: String) {
        val f = dataFolder
        if (!f.exists()) {
            f.mkdir()
        }
        val src = javaClass.protectionDomain.codeSource
        if (src != null) {
            val jar = src.location
            val zip = ZipInputStream(jar.openStream())
            while (true) {
                val e = zip.nextEntry ?: break
                val name = e.name
                if (name == key) {
                    val destination = File("$f/$key")
                    Util.copy(name, destination)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFolderFromInToOut(key: String) {
        val f = File(dataFolder, key)
        if (!f.exists()) {
            f.mkdir()
        }
        val src = javaClass.protectionDomain.codeSource
        if (src != null) {
            val jar = src.location
            val zip = ZipInputStream(jar.openStream())
            while (true) {
                val e = zip.nextEntry ?: break
                val name = e.name
                if (name.startsWith("$key/") && name != "$key/") {
                    val destination = File(f.toString() + "/" + name.substring(key.length + 1))
                    Util.copy(name, destination)
                }
            }
        }
    }

    private fun registerListeners() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(ChatListener(), this)
    }
}