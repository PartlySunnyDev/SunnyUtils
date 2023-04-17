package me.partlysunny.sunnyutils

import me.partlysunny.sunnyutils.version.Version
import me.partlysunny.sunnyutils.version.VersionManager

class SunnyUtilsCore(val plugin: SunnyPlugin) {

    // Singleton
    companion object {
        private var instance: SunnyUtilsCore? = null

        fun getInstance(): SunnyUtilsCore {
            return instance!!
        }

        fun init(plugin: SunnyPlugin) {
            instance = SunnyUtilsCore(plugin)
        }
    }

    private var versionManager: VersionManager? = null

    fun loadModules() {
        val v = Version(plugin.server.version)
        //Load modules
        versionManager = VersionManager(plugin)
        versionManager!!.checkServerVersion()
        try {
            versionManager!!.load()
        } catch (e: ReflectiveOperationException) {
            ConsoleLogger.error(
                "This version (${v.get()}) is not supported by SunnyUtils!",
                "The parent plugin ${plugin.pluginName} may not work as intended!"
            )
            return
        }
        versionManager!!.enable()
    }

}