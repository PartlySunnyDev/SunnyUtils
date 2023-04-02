package me.partlysunny.util.classes

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class ConfigManager(var plugin: JavaPlugin) {
    var fileName: String? = null
    fun createConfig(name: String): YamlConfiguration {
        var name = name
        if (!name.endsWith(".yml")) {
            name = "$name.yml"
        }
        val file = File(plugin.dataFolder, name)
        if (!file.exists()) {
            plugin.dataFolder.mkdir()
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return YamlConfiguration.loadConfiguration(file) // returns the newly created configuration object.
    }

    fun saveConfig(name: String, config: FileConfiguration) {
        var name = name
        if (!name.endsWith(".yml")) {
            name = "$name.yml"
        }
        val file = File(plugin.dataFolder, name)
        try {
            config.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getConfig(name: String): YamlConfiguration {
        var name = name
        if (!name.endsWith(".yml")) {
            name = "$name.yml"
        }
        createConfig(name)
        val file = File(plugin.dataFolder, name)
        return YamlConfiguration.loadConfiguration(file) // file found, load into config and return it.
    }
}