package me.partlysunny.util

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.CommandMinecart
import java.util.*
import java.util.concurrent.ThreadLocalRandom

//NOT BY ME!!!! https://github.com/ZombieStriker/PsudoCommands/blob/master/src/me/zombie_striker/psudocommands/CommandUtils.java :)) ty
object CommandUtils {
    /**
     * Use this if you are unsure if a player provided the "@a" tag. This will allow
     * multiple entities to be retrieved.
     *
     *
     * This can return a null variable if no tags are included, or if a value for a
     * tag does not exist (I.e if the tag [type=___] contains an entity that does
     * not exist in the specified world)
     *
     *
     * The may also be empty or null values at the end of the array. Once a null
     * value has been reached, you do not need to loop through any of the higher
     * indexes
     *
     *
     * Currently supports the tags:
     *
     * @param arg    the argument that we are testing for
     * @param sender the sender of the command
     * @return The entities that match the criteria
     * @p , @a , @e , @r
     *
     *
     * Currently supports the selectors: [type=] [r=] [rm=] [c=] [w=] [m=]
     * [name=] [l=] [lm=] [h=] [hm=] [rx=] [rxm=] [ry=] [rym=] [team=]
     * [score_---=] [score_---_min=] [x] [y] [z] [limit=] [x_rotation] [y_rotation]
     *
     *
     * All selectors can be inverted.
     */
    fun getTargets(sender: CommandSender, arg: String): Array<Entity?>? {
        val ents: Array<Entity?>
        var loc: Location? = null
        if (sender is Player) {
            loc = sender.location
        } else if (sender is BlockCommandSender) {
            // Center of block.
            loc = sender.block.location.add(0.5, 0.0, 0.5)
        } else if (sender is CommandMinecart) {
            loc = sender.location
        }
        val tags = getTags(arg)

        // prefab fix
        for (s in tags) {
            if (hasTag(SelectorType.X, s)) {
                loc!!.x = getInt(s).toDouble()
            } else if (hasTag(SelectorType.Y, s)) {
                loc!!.y = getInt(s).toDouble()
            } else if (hasTag(SelectorType.Z, s)) {
                loc!!.z = getInt(s).toDouble()
            }
        }
        if (arg.startsWith("@s")) {
            ents = arrayOfNulls(1)
            if (sender is Player) {
                var good = true
                for (b in tags.indices) {
                    if (!canBeAccepted(tags[b], sender as Entity, loc)) {
                        good = false
                        break
                    }
                }
                if (good) {
                    ents[0] = sender as Entity
                }
            } else {
                return null
            }
            return ents
        } else if (arg.startsWith("@a")) {
            // ents = new Entity[maxEnts];
            val listOfValidEntities: MutableList<Entity?> = ArrayList()
            val C = getLimit(arg)
            var usePlayers = true
            for (tag in tags) {
                if (hasTag(SelectorType.TYPE, tag)) {
                    usePlayers = false
                    break
                }
            }
            val ea: MutableList<Entity> = ArrayList(Bukkit.getOnlinePlayers())
            if (!usePlayers) {
                ea.clear()
                for (w in getAcceptedWorldsFullString(loc, arg)) {
                    ea.addAll(w!!.entities)
                }
            }
            for (e in ea) {
                if (listOfValidEntities.size >= C) break
                var isValid = true
                for (b in tags.indices) {
                    if (!canBeAccepted(tags[b], e, loc)) {
                        isValid = false
                        break
                    }
                }
                if (isValid) {
                    listOfValidEntities.add(e)
                }
            }
            ents = listOfValidEntities.toTypedArray<Entity?>()
        } else if (arg.startsWith("@p")) {
            ents = arrayOfNulls(1)
            var closestInt = Double.MAX_VALUE
            var closest: Entity? = null
            for (w in getAcceptedWorldsFullString(loc, arg)) {
                for (e in w!!.players) {
                    if (e === sender) continue
                    var temp = loc
                    if (temp == null) temp = e.world.spawnLocation
                    val distance = e.location.distanceSquared(temp)
                    if (closestInt > distance) {
                        var good = true
                        for (tag in tags) {
                            if (!canBeAccepted(tag, e, temp)) {
                                good = false
                                break
                            }
                        }
                        if (good) {
                            closestInt = distance
                            closest = e
                        }
                    }
                }
            }
            ents[0] = closest
        } else if (arg.startsWith("@e")) {
            val entities: MutableList<Entity?> = ArrayList()
            val C = getLimit(arg)
            for (w in getAcceptedWorldsFullString(loc, arg)) {
                for (e in w!!.entities) {
                    if (entities.size > C) break
                    if (e === sender) continue
                    var valid = true
                    for (tag in tags) {
                        if (!canBeAccepted(tag, e, loc)) {
                            valid = false
                            break
                        }
                    }
                    if (valid) {
                        entities.add(e)
                    }
                }
            }
            ents = entities.toTypedArray<Entity?>()
        } else if (arg.startsWith("@r")) {
            val r: Random = ThreadLocalRandom.current()
            ents = arrayOfNulls(1)
            val validEntities: MutableList<Entity> = ArrayList()
            for (w in getAcceptedWorldsFullString(loc, arg)) {
                if (hasTag(SelectorType.TYPE, arg)) {
                    for (e in w!!.entities) {
                        var good = true
                        for (tag in tags) {
                            if (!canBeAccepted(tag, e, loc)) {
                                good = false
                                break
                            }
                        }
                        if (good) validEntities.add(e)
                    }
                } else {
                    for (e in Bukkit.getOnlinePlayers()) {
                        var good = true
                        for (tag in tags) {
                            if (!canBeAccepted(tag, e, loc)) {
                                good = false
                                break
                            }
                        }
                        if (good) validEntities.add(e)
                    }
                }
            }
            ents[0] = validEntities[r.nextInt(validEntities.size)]
        } else {
            ents = arrayOf(Bukkit.getPlayer(arg))
        }
        return ents
    }

    /**
     * Returns one entity. Use this if you know the player will not provide the '@a'
     * tag.
     *
     *
     * This can return a null variable if no tags are included, or if a value for a
     * tag does not exist (I.e if the tag [type=___] contains an entity that does
     * not exist in the specified world)
     *
     * @param sender the command sender
     * @param arg    the argument of the target
     * @return The first entity retrieved.
     */
    fun getTarget(sender: CommandSender, arg: String): Entity? {
        val e = getTargets(sender, arg)
        return if (e!!.size == 0) null else e[0]
    }

    /**
     * Returns an integer. Use this to support "~" by providing what it will mean.
     *
     *
     * E.g. rel="x" when ~ should be turn into the entity's X coord.
     *
     *
     * Currently supports "x", "y" and "z".
     *
     * @param arg The target
     * @param rel relative to the X,Y, or Z
     * @param e   The entity to check relative to.
     * @return the int
     */
    fun getIntRelative(arg: String, rel: String, e: Entity): Int {
        var relInt = 0
        if (arg.startsWith("~")) {
            when (rel.lowercase(Locale.getDefault())) {
                "x" -> relInt = e.location.blockX
                "y" -> relInt = e.location.blockY
                "z" -> relInt = e.location.blockZ
            }
            return mathIt(arg, relInt)
        } else if (arg.startsWith("^")) {
            // TODO: Fix code. The currently just acts the same as ~. This should move the
            // entity relative to what its looking at.
            when (rel.lowercase(Locale.getDefault())) {
                "x" -> relInt = e.location.blockX
                "y" -> relInt = e.location.blockY
                "z" -> relInt = e.location.blockZ
            }
            return mathIt(arg, relInt)
        }
        return 0
    }

    private fun canBeAccepted(arg: String?, e: Entity, loc: Location?): Boolean {
        if (hasTag(SelectorType.X_ROTATION, arg) && isWithinYaw(arg, e)) return true
        if (hasTag(SelectorType.Y_ROTATION, arg) && isWithinPitch(arg, e)) return true
        if (hasTag(SelectorType.TYPE, arg) && isType(arg, e)) return true
        if (hasTag(SelectorType.NAME, arg) && isName(arg, e)) return true
        if (hasTag(SelectorType.TEAM, arg) && isTeam(arg, e)) return true
        if (hasTag(SelectorType.SCORE_FULL, arg) && isScore(arg, e)) return true
        if (hasTag(SelectorType.SCORE_MIN, arg) && isScoreMin(arg, e)) return true
        if (hasTag(SelectorType.SCORE_13, arg) && isScoreWithin(arg, e)) return true
        if (hasTag(SelectorType.DISTANCE, arg) && isWithinDistance(arg, loc, e)) return true
        if (hasTag(SelectorType.LEVEL, arg) && isWithinLevel(arg, e)) return true
        if (hasTag(SelectorType.TAG, arg) && isHasTags(arg, e)) return true
        if (hasTag(SelectorType.RYM, arg) && isRYM(arg, e)) return true
        if (hasTag(SelectorType.RXM, arg) && isRXM(arg, e)) return true
        if (hasTag(SelectorType.HM, arg) && isHM(arg, e)) return true
        if (hasTag(SelectorType.RY, arg) && isRY(arg, e)) return true
        if (hasTag(SelectorType.RX, arg) && isRX(arg, e)) return true
        if (hasTag(SelectorType.RM, arg) && isRM(arg, loc, e)) return true
        if (hasTag(SelectorType.LMax, arg) && isLM(arg, e)) return true
        if (hasTag(SelectorType.L, arg) && isL(arg, e)) return true
        if (hasTag(SelectorType.m, arg) && isM(arg, e)) return true
        if (hasTag(SelectorType.H, arg) && isH(arg, e)) return true
        if (hasTag(SelectorType.World, arg) && isW(arg, loc, e)) return true
        if (hasTag(SelectorType.R, arg) && isR(arg, loc, e)) return true
        if (hasTag(SelectorType.X, arg)) return true
        return if (hasTag(SelectorType.Y, arg)) true else hasTag(SelectorType.Z, arg)
    }

    private fun getTags(arg: String): Array<String?> {
        if (!arg.contains("[")) return arrayOfNulls(0)
        val tags = arg.split("\\[".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("\\]".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return tags.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun mathIt(args: String, relInt: Int): Int {
        var total = 0
        var mode: Int = 0
        val arg = args.replace("~", relInt.toString())
        var intString = ""
        for (i in arg.indices) {
            if (arg[i] == '+' || arg[i] == '-' || arg[i] == '*' || arg[i] == '/') {
                try {
                    when (mode) {
                        0 -> total += intString.toInt()
                        1 -> total -= intString.toInt()
                        2 -> total *= intString.toInt()
                        3 -> total /= intString.toInt()
                    }
                    mode =
                        (if (arg[i] == '+') 0 else if (arg[i] == '-') 1 else if (arg[i] == '*') 2 else if (arg[i] == '/') 3 else -1)
                } catch (e: Exception) {
                    Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!")
                }
            } else if (args.length == i || arg[i] == ' ' || arg[i] == ',' || arg[i] == ']') {
                try {
                    when (mode) {
                        0 -> total = total + intString.toInt()
                        1 -> total = total - intString.toInt()
                        2 -> total = total * intString.toInt()
                        3 -> total = total / intString.toInt()
                    }
                } catch (e: Exception) {
                    Bukkit.getLogger().severe("There has been an issue with a plugin using the CommandUtils class!")
                }
                break
            } else {
                intString += arg[i]
            }
        }
        return total
    }

    private fun getLimit(arg: String): Int {
        if (hasTag(SelectorType.LIMIT, arg)) for (s in getTags(arg)) {
            if (hasTag(SelectorType.LIMIT, s)) {
                return getInt(s)
            }
        }
        if (hasTag(SelectorType.C, arg)) for (s in getTags(arg)) {
            if (hasTag(SelectorType.C, s)) {
                return getInt(s)
            }
        }
        return Int.MAX_VALUE
    }

    private fun getType(arg: String?): String {
        return if (hasTag(SelectorType.TYPE, arg)) arg!!.lowercase(Locale.getDefault()).split("=".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace("!", "") else "Player"
    }

    private fun getName(arg: String?): String? {
        val reparg = arg!!.replace(" ", "_")
        return reparg.replace("!", "").split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    private fun getW(arg: String?): World? {
        return Bukkit.getWorld(getString(arg))
    }

    private fun getScoreMinName(arg: String?): String {
        return arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(
            0,
            arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].length - 1 - 4
        ).replace("score_", "")
    }

    private fun getScoreName(arg: String?): String {
        return arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].replace("score_", "")
    }

    private fun getTeam(arg: String?): String {
        return arg!!.lowercase(Locale.getDefault()).replace("!", "").split("=".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1]
    }

    private fun getValueAsFloat(arg: String?): Float {
        return arg!!.replace("!", "").split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toFloat()
    }

    private fun getValueAsInteger(arg: String?): Int {
        return arg!!.replace("!", "").split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
    }

    private fun getM(arg: String?): GameMode? {
        val split =
            arg!!.replace("!", "").lowercase(Locale.getDefault()).split("=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val returnType = split[1]
        if (returnType.equals("0", ignoreCase = true) || returnType.equals("s", ignoreCase = true)
            || returnType.equals("survival", ignoreCase = true)
        ) return GameMode.SURVIVAL
        if (returnType.equals("1", ignoreCase = true) || returnType.equals("c", ignoreCase = true)
            || returnType.equals("creative", ignoreCase = true)
        ) return GameMode.CREATIVE
        if (returnType.equals("2", ignoreCase = true) || returnType.equals("a", ignoreCase = true)
            || returnType.equals("adventure", ignoreCase = true)
        ) return GameMode.ADVENTURE
        return if (returnType.equals("3", ignoreCase = true) || returnType.equals("sp", ignoreCase = true)
            || returnType.equals("spectator", ignoreCase = true)
        ) GameMode.SPECTATOR else null
    }

    private fun getAcceptedWorldsFullString(loc: Location?, fullString: String): List<World?> {
        var string: String? = null
        for (tag in getTags(fullString)) {
            if (hasTag(SelectorType.World, tag)) {
                string = tag
                break
            }
        }
        if (string == null) {
            val worlds: MutableList<World?> = ArrayList()
            if (loc == null || loc.world == null) {
                worlds.addAll(Bukkit.getWorlds())
            } else {
                worlds.add(loc.world)
            }
            return worlds
        }
        return getAcceptedWorlds(string)
    }

    private fun getAcceptedWorlds(string: String?): List<World?> {
        val worlds: MutableList<World?> = ArrayList(Bukkit.getWorlds())
        if (isInverted(string)) {
            worlds.remove(getW(string))
        } else {
            worlds.clear()
            worlds.add(getW(string))
        }
        return worlds
    }

    private fun isTeam(arg: String?, e: Entity): Boolean {
        if (e !is Player) return false
        for (t in Bukkit.getScoreboardManager()!!.mainScoreboard.teams) {
            if (t.name.equals(getTeam(arg), ignoreCase = true) != isInverted(arg)) {
                if (t.entries.contains(e.getName()) != isInverted(arg)) return true
            }
        }
        return false
    }

    private fun isWithinPitch(arg: String?, e: Entity): Boolean {
        val pitch = getValueAsFloat(arg)
        return isWithinDoubleValue(isInverted(arg), arg, e.location.pitch.toDouble())
    }

    private fun isWithinYaw(arg: String?, e: Entity): Boolean {
        val pitch = getValueAsFloat(arg)
        return isWithinDoubleValue(isInverted(arg), arg, e.location.yaw.toDouble())
    }

    private fun isWithinDistance(arg: String?, start: Location?, e: Entity): Boolean {
        var distanceMin = 0.0
        var distanceMax = Double.MAX_VALUE
        val distance = arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        if (e.location.world !== start!!.world) return false
        return if (distance.contains("..")) {
            val temp = distance.split("\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (!temp[0].isEmpty()) {
                distanceMin = temp[0].toInt().toDouble()
            }
            if (temp.size > 1 && !temp[1].isEmpty()) {
                distanceMax = temp[1].toDouble()
            }
            val actDis = start!!.distanceSquared(e.location)
            actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis
        } else {
            var mult = distance.toInt()
            mult *= mult
            start!!.distanceSquared(e.location).toInt() == mult
        }
    }

    private fun isWithinLevel(arg: String?, e: Entity): Boolean {
        if (e !is Player) return false
        var distanceMin = 0.0
        var distanceMax = Double.MAX_VALUE
        val distance = arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        return if (distance.contains("..")) {
            val temp: Array<String?> = distance.split("..".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (!temp[0]!!.isEmpty()) {
                distanceMin = temp[0]!!.toInt().toDouble()
            }
            if (temp[1] != null && !temp[1]!!.isEmpty()) {
                distanceMax = temp[1]!!.toDouble()
            }
            val actDis = e.expToLevel.toDouble()
            actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis
        } else {
            e.expToLevel == distance.toInt()
        }
    }

    private fun isScore(arg: String?, e: Entity): Boolean {
        if (e !is Player) return false
        for (o in Bukkit.getScoreboardManager()!!.mainScoreboard.objectives) {
            if (o.name.equals(getScoreName(arg), ignoreCase = true)) {
                if (o.getScore(e.getName()).score <= getValueAsInteger(arg) != isInverted(arg)) return true
            }
        }
        return false
    }

    private fun isScoreWithin(arg: String?, e: Entity): Boolean {
        if (e !is Player) return false
        val scores =
            arg!!.split("\\{".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("\\}".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (i in scores.indices) {
            val s = scores[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val name = s[0]
            for (o in Bukkit.getScoreboardManager()!!.mainScoreboard.objectives) {
                if (o.name.equals(name, ignoreCase = true)) {
                    if (!isWithinDoubleValue(
                            isInverted(arg),
                            s[1],
                            o.getScore(e.getName()).score.toDouble()
                        )
                    ) return false
                }
            }
        }
        return true
    }

    private fun isHasTags(arg: String?, e: Entity): Boolean {
        return if (e !is Player) false else isInverted(arg) != e.getScoreboardTags().contains(getString(arg))
    }

    private fun isScoreMin(arg: String?, e: Entity): Boolean {
        if (e !is Player) return false
        for (o in Bukkit.getScoreboardManager()!!.mainScoreboard.objectives) {
            if (o.name.equals(getScoreMinName(arg), ignoreCase = true)) {
                if (o.getScore(e.getName()).score >= getValueAsInteger(arg) != isInverted(arg)) return true
            }
        }
        return false
    }

    private fun isRM(arg: String?, loc: Location?, e: Entity): Boolean {
        return if (loc!!.world !== e.world) false else isGreaterThan(arg, loc!!.distance(e.location))
    }

    private fun isR(arg: String?, loc: Location?, e: Entity): Boolean {
        return if (loc!!.world !== e.world) false else isLessThan(arg, loc!!.distance(e.location))
    }

    private fun isRXM(arg: String?, e: Entity): Boolean {
        return isLessThan(arg, e.location.yaw.toDouble())
    }

    private fun isRX(arg: String?, e: Entity): Boolean {
        return isGreaterThan(arg, e.location.yaw.toDouble())
    }

    private fun isRYM(arg: String?, e: Entity): Boolean {
        return isLessThan(arg, e.location.pitch.toDouble())
    }

    private fun isRY(arg: String?, e: Entity): Boolean {
        return isGreaterThan(arg, e.location.pitch.toDouble())
    }

    private fun isL(arg: String?, e: Entity): Boolean {
        if (e is Player) {
            isLessThan(arg, e.totalExperience.toDouble())
        }
        return false
    }

    private fun isLM(arg: String?, e: Entity): Boolean {
        return if (e is Player) {
            isGreaterThan(arg, e.totalExperience.toDouble())
        } else false
    }

    private fun isH(arg: String?, e: Entity): Boolean {
        return if (e is Damageable) isGreaterThan(arg, e.health) else false
    }

    private fun isHM(arg: String?, e: Entity): Boolean {
        return if (e is Damageable) isLessThan(arg, e.health) else false
    }

    private fun isM(arg: String?, e: Entity): Boolean {
        if (getM(arg) == null) return true
        return if (e is HumanEntity) {
            isInverted(arg) != (getM(arg) == e.gameMode)
        } else false
    }

    private fun isW(arg: String?, loc: Location?, e: Entity): Boolean {
        return if (getW(arg) == null) {
            true
        } else isInverted(arg) != getAcceptedWorlds(arg).contains(getW(arg))
    }

    private fun isName(arg: String?, e: Entity): Boolean {
        return if (getName(arg) == null) true else isInverted(arg) == (e.customName == null) && isInverted(arg) != ((getName(
            arg
        )
                == e.customName!!.replace(" ", "_")) || e is Player && e.getName().replace(" ", "_")
            .equals(getName(arg), ignoreCase = true))
    }

    private fun isType(arg: String?, e: Entity): Boolean {
        val invert = isInverted(arg)
        val type = getType(arg)
        return invert != e.type.name.equals(type, ignoreCase = true)
    }

    private fun isInverted(arg: String?): Boolean {
        return arg!!.lowercase(Locale.getDefault()).split("!".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray().size != 1
    }

    private fun getInt(arg: String?): Int {
        return arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray<String>()[1].toInt()
    }

    private fun getString(arg: String?): String {
        return arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace("!".toRegex(), "")
    }

    private fun isLessThan(arg: String?, value: Double): Boolean {
        val inverted = isInverted(arg)
        val mult = arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toDouble()
        return value < mult != inverted
    }

    private fun isGreaterThan(arg: String?, value: Double): Boolean {
        val inverted = isInverted(arg)
        val mult = arg!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toDouble()
        return value > mult != inverted
    }

    private fun isWithinDoubleValue(inverted: Boolean, arg: String?, value: Double): Boolean {
        var min = -Double.MAX_VALUE
        var max = Double.MAX_VALUE
        return if (arg!!.contains("..")) {
            val temp = arg.split("\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (temp[0].isNotEmpty()) {
                min = temp[0].toInt().toDouble()
            }
            if (temp.size > 1 && temp[1].isNotEmpty()) {
                max = temp[1].toDouble()
            }
            (value <= max * max && min * min <= value) != inverted
        } else {
            val mult = arg.toDouble()
            value == mult != inverted
        }
    }

    private fun hasTag(type: SelectorType, arg: String?): Boolean {
        return arg!!.lowercase(Locale.getDefault()).startsWith(type.stringName)
    }

    internal enum class SelectorType(var stringName: String) {
        LEVEL("level="),
        DISTANCE("distance="),
        TYPE("type="),
        NAME("name="),
        TEAM("team="),
        LMax("lm="),
        L(
            "l="
        ),
        World("w="),
        m("m="),
        C("c="),
        HM("hm="),
        H("h="),
        RM("rm="),
        RYM("rym="),
        RX("rx="),
        SCORE_FULL(
            "score="
        ),
        SCORE_MIN("score_min"),
        SCORE_13(
            "scores="
        ),
        R("r="),
        RXM("rxm="),
        RY("ry="),
        TAG("tag="),
        X("x="),
        Y("y="),
        Z("z="),
        LIMIT("limit="),
        Y_ROTATION("y_rotation"),
        X_ROTATION("x_rotation")

    }
}