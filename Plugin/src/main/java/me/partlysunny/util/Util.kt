package me.partlysunny.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import me.partlysunny.SunnySpigotCore
import me.partlysunny.gui.textInput.ChatListener
import me.partlysunny.util.reflection.JavaAccessor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern

object Util {
    val RAND = Random()

    /**
     * With this method you can get a player's head by nickname or a base64 head by base64 code
     *
     * @param type  Determines whether you want to get the head by name or by base64
     * @param value If you want a player's head, then the player's name. If you want base64, then base64 code.
     * @return Head itemStack
     */
    fun convert(type: HeadType, value: String): ItemStack {
        return if (type == HeadType.PLAYER_HEAD) {
            getSkullByTexture(getPlayerHeadTexture(value))
        } else {
            getSkullByTexture(value)
        }
    }

    private fun getSkullByTexture(url: String): ItemStack {
        val head = getAllVersionStack("SKULL_ITEM", "PLAYER_HEAD")
        if (url.isEmpty() || url == "none") return head
        val meta = head.itemMeta as SkullMeta?
        val profile = GameProfile(UUID.randomUUID(), "")
        profile.properties.put("textures", Property("textures", url))
        try {
            JavaAccessor.Companion.setValue(meta, JavaAccessor.Companion.getField(meta!!.javaClass, "profile"), profile)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        head.setItemMeta(meta)
        return head
    }

    private fun getPlayerHeadTexture(username: String): String {
        if (getPlayerId(username) == "none") return "none"
        val url = "https://api.minetools.eu/profile/" + getPlayerId(username)
        return try {
            val userData = readUrl(url)
            val parsedData: Any = JsonParser.parseString(userData)
            val jsonData = parsedData as JsonObject
            val decoded = jsonData["raw"] as JsonObject
            val textures = decoded["properties"] as JsonArray
            val data = textures[0] as JsonObject
            data["value"].toString()
        } catch (ex: Exception) {
            "none"
        }
    }

    @Throws(Exception::class)
    private fun readUrl(urlString: String): String {
        var reader: BufferedReader? = null
        return try {
            val url = URL(urlString)
            reader = BufferedReader(InputStreamReader(url.openStream()))
            val buffer = StringBuilder()
            var read: Int
            val chars = CharArray(1024)
            while (reader.read(chars).also { read = it } != -1) buffer.append(chars, 0, read)
            buffer.toString()
        } finally {
            reader?.close()
        }
    }

    private fun getPlayerId(playerName: String): String {
        return try {
            val url = "https://api.minetools.eu/uuid/$playerName"
            val userData = readUrl(url)
            val parsedData: Any = JsonParser.parseString(userData)
            val jsonData = parsedData as JsonObject
            if (jsonData["id"] != null) jsonData["id"].toString() else ""
        } catch (ex: Exception) {
            "none"
        }
    }

    private fun getAllVersionStack(oldName: String, newName: String): ItemStack {
        var material: Material? = null
        material = try {
            Material.valueOf(oldName)
        } catch (exception: Exception) {
            Material.valueOf(newName)
        }
        return ItemStack(material!!, 1)
    }

    fun getRandomBetween(a: Int, b: Int): Int {
        require(a <= b) { "a must be higher than b" }
        if (a == b) {
            return a
        }
        if (a < 0 && b < 0) {
            return -getRandomBetween(-b, -a)
        }
        return if (a < 0) {
            getRandomBetween(0, -a + b) + a
        } else RAND.nextInt(b - a) + a
    }

    fun processText(text: String?): String {
        return text?.replace('&', ChatColor.COLOR_CHAR) ?: ""
    }

    fun scheduleRepeatingCancelTask(r: Runnable?, delay: Long, repeat: Long, stopAfter: Long) {
        val scheduler = Bukkit.getScheduler()
        val p: JavaPlugin = JavaPlugin.getPlugin(SunnySpigotCore::class.java)
        val t = scheduler.runTaskTimer(p, r!!, delay, repeat)
        scheduler.runTaskLater(p, Runnable { t.cancel() }, stopAfter)
    }

    fun processTexts(texts: List<String?>): List<String> {
        val result: MutableList<String> = ArrayList()
        texts.forEach(Consumer { n: String? -> result.add(processText(n)) })
        return result
    }

    fun <T> getOrDefault(y: ConfigurationSection, key: String?, def: T): T? {
        return if (y.contains(key!!)) {
            y[key] as T?
        } else def
    }

    fun <T> getOrError(y: ConfigurationSection, key: String): T? {
        if (y.contains(key)) {
            return y[key] as T?
        }
        throw IllegalArgumentException("Key " + key + " inside " + y.name + " was not found!")
    }

    fun isInvalidFilePath(path: String?): Boolean {
        val f = File(path)
        return try {
            f.canonicalPath
            false
        } catch (e: IOException) {
            true
        }
    }

    @Throws(IOException::class)
    fun copy(source: String?, destination: File) {
        val stream = SunnySpigotCore::class.java.classLoader.getResourceAsStream(source)
        if (!destination.exists()) {
            Files.copy(stream, destination.toPath())
        }
    }

    @JvmOverloads
    fun splitLoreForLine(
        input: String,
        linePrefix: String = ChatColor.GRAY.toString(),
        lineSuffix: String = "",
        width: Int = 30
    ): List<String> {
        val array = input.toCharArray()
        val out: MutableList<String> = ArrayList()
        var currentColor = ""
        var cachedColor = ""
        var wasColorChar = false
        var currentLine = StringBuilder(linePrefix)
        var currentWord = StringBuilder()
        for (i in array.indices) {
            val c = array[i]
            if (wasColorChar) {
                wasColorChar = false
                cachedColor = currentColor
                val pattern = Pattern.compile("[0-9a-fkmolnr]")
                if (pattern.matcher(c.toString() + "").matches()) {
                    if (c == 'r') {
                        currentColor = ChatColor.COLOR_CHAR.toString() + "r"
                    } else {
                        currentColor += ChatColor.COLOR_CHAR.toString() + "" + c
                    }
                }
                currentWord.append(ChatColor.COLOR_CHAR.toString() + "").append(c)
                continue
            }
            if (c == '\n') {
                currentLine.append(currentWord)
                currentWord = StringBuilder()
                out.add(currentLine.toString() + lineSuffix)
                currentLine = StringBuilder(linePrefix + cachedColor + currentWord)
                cachedColor = currentColor
                continue
            }
            if (c == ' ') {
                if ((currentLine.toString() + currentWord.toString()).replace(
                        "ยง[0-9a-fklmnor]".toRegex(),
                        ""
                    ).length > width
                ) {
                    out.add(currentLine.toString() + lineSuffix)
                    currentLine = StringBuilder("$linePrefix$cachedColor$currentWord ")
                } else {
                    currentLine.append(currentWord).append(" ")
                }
                cachedColor = currentColor
                currentWord = StringBuilder()
                continue
            }
            if (c == ChatColor.COLOR_CHAR) {
                wasColorChar = true
                continue
            }
            currentWord.append(c)
        }
        currentLine.append(currentWord)
        out.add(currentLine.toString() + lineSuffix)
        return out
    }

    fun getAlphabetSorted(values: Array<String?>): Array<String?>? {
        val strings: List<String?> = listOf(*values)
        strings.sortedWith { o1, o2 -> o1!!.compareTo(o2!!) }
        return strings.toTypedArray<String?>()
    }

    fun linspace(min: Double, max: Double, points: Int): DoubleArray {
        val d = DoubleArray(points)
        for (i in 0 until points) {
            d[i] = min + i * (max - min) / (points - 1)
        }
        return d
    }

    fun fakeSpace(points: Int): DoubleArray {
        return when (points) {
            0 -> doubleArrayOf()
            1 -> doubleArrayOf(4.0)
            2 -> doubleArrayOf(3.0, 5.0)
            3 -> doubleArrayOf(2.0, 4.0, 6.0)
            4 -> doubleArrayOf(1.0, 3.0, 5.0, 7.0)
            5 -> doubleArrayOf(2.0, 3.0, 4.0, 5.0, 6.0)
            6 -> doubleArrayOf(1.0, 2.0, 3.0, 5.0, 6.0, 7.0)
            7 -> doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)
            8 -> doubleArrayOf(0.0, 1.0, 2.0, 3.0, 5.0, 6.0, 7.0, 8.0)
            else -> doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
        }
    }

    fun invalid(message: String, p: Player) {
        p.sendMessage(ChatColor.RED.toString() + message)
        p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
    }

    fun addLoreLine(s: ItemStack, vararg lines: String?) {
        val m = s.itemMeta
        var lore = m!!.lore
        if (lore == null) {
            lore = ArrayList()
        }
        lore.addAll(java.util.List.of(*lines))
        m.lore = lore
        s.setItemMeta(m)
    }

    fun setName(i: ItemStack?, name: String?) {
        val m = i!!.itemMeta ?: return
        m.setDisplayName(name)
        i.setItemMeta(m)
    }

    fun getTextInputAsInt(pl: Player): Int? {
        val input: String? = ChatListener.getCurrentInput(pl)
        if (input == "cancel") {
            return null
        }
        val currentInput: Int
        if (input != null) {
            currentInput = try {
                input.toInt()
            } catch (e: NumberFormatException) {
                pl.sendMessage(ChatColor.RED.toString() + "Invalid number!")
                return null
            }
            if (currentInput < 1) {
                pl.sendMessage("Must be greater than 1!")
                return null
            }
            return currentInput
        }
        return null
    }

    fun getTextInputAsDouble(pl: Player): Double? {
        val input: String? = ChatListener.getCurrentInput(pl)
        if (input == "cancel") {
            return null
        }
        val currentInput: Double
        if (input != null) {
            currentInput = try {
                input.toDouble()
            } catch (e: NumberFormatException) {
                pl.sendMessage(ChatColor.RED.toString() + "Invalid number!")
                return null
            }
            if (currentInput < 0) {
                pl.sendMessage("Must be greater than 0!")
                return null
            }
            return currentInput
        }
        return null
    }

    fun setLore(i: ItemStack?, lore: List<String?>?) {
        val m = i!!.itemMeta ?: return
        m.lore = lore
        i.setItemMeta(m)
    }

    fun asType(t: PotionEffectType?): PotionType {
        if (t == null) {
            return PotionType.WATER
        }
        var asType = PotionType.WATER
        for (type in PotionType.values()) {
            if (t == type.effectType) asType = type
        }
        return asType
    }

    fun deleteFile(f: File) {
        if (f.exists() && !f.isDirectory) {
            f.delete()
        }
    }

    fun hasAllPerms(p: Player, vararg perms: String): Boolean {
        for (perm in perms) {
            if (!p.hasPermission(perm)) {
                return false
            }
        }
        return true
    }

    /**
     * Generation head type enum
     */
    enum class HeadType {
        PLAYER_HEAD,
        BASE64
    }
}
