package me.partlysunny.sunnyutils

import me.partlysunny.sunnyutils.command.CommandManager
import me.partlysunny.sunnyutils.command.commands.TestCommand
import me.partlysunny.sunnyutils.gui.SelectGuiManager
import me.partlysunny.sunnyutils.gui.textInput.ChatListener
import me.partlysunny.sunnyutils.util.Util
import me.partlysunny.sunnyutils.version.Version
import me.partlysunny.sunnyutils.version.VersionManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

abstract class SunnyPlugin(val pluginName: String) : JavaPlugin() {
    //\w*(?<!package )me\.partlysunny\.(?<!sunnyutils)
    protected var versionManager: VersionManager? = null
    protected var commandManager: CommandManager? = null

    protected fun reload() {

    }

    override fun onLoad() {
        SunnyUtilsCore.init(this)
    }

    override fun onEnable() {
        //Get version
        val v = Version(this.server.version)
        ConsoleLogger.console("Loading $pluginName...")
        //Load modules
        SunnyUtilsCore.getInstance().loadModules()
        versionManager = VersionManager(this)
        versionManager!!.checkServerVersion()
        try {
            versionManager!!.load()
        } catch (e: ReflectiveOperationException) {
            ConsoleLogger.error(
                "This version (${v.get()}) is not supported by $pluginName!",
                "Shutting down plugin..."
            )
            isEnabled = false
            return
        }
        versionManager!!.enable()
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
        ConsoleLogger.console("Enabled $pluginName on version " + v.get())
    }

    override fun onDisable() {
        ConsoleLogger.console("Disabling $pluginName...")
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