package me.partlysunny.version

import java.util.*
import java.util.regex.Pattern

//From https://github.com/iSach/UltraCosmetics/blob/master/core/src/main/java/be/isach/ultracosmetics/Version.java NOT BY ME!!!
/**
 * Version.
 *
 * @author iSach
 */
class Version(version: String?) : Comparable<Version> {
    private val version: String
    private val versionString: String

    init {
        requireNotNull(version) { "Version can not be null" }
        val matcher = VERSION_PATTERN.matcher(version)
        require(matcher.find()) { "Could not parse version string: '$version'" }
        this.version = matcher.group()
        versionString = version
    }

    fun get(): String {
        return version
    }

    override fun compareTo(otherVersion: Version): Int {
        val thisParts = this.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val thatParts = otherVersion.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = Math.max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) thisParts[i].toInt() else 0
            val thatPart = if (i < thatParts.size) thatParts[i].toInt() else 0
            val cmp = Integer.compare(thisPart, thatPart)
            if (cmp != 0) {
                return cmp
            }
        }
        // release > dev build of same version
        return java.lang.Boolean.compare(isRelease, otherVersion.isRelease)
    }

    val isDev: Boolean
        get() = versionString.lowercase(Locale.getDefault()).contains("dev")
    val isRelease: Boolean
        get() = !isDev

    override fun equals(that: Any?): Boolean {
        return this === that || that != null && this.javaClass == that.javaClass && this.compareTo(that as Version) == 0
    }

    override fun hashCode(): Int {
        return version.hashCode()
    }

    companion object {
        private val VERSION_PATTERN = Pattern.compile("(?:\\d+\\.)+\\d+")
    }
}
