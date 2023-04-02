package me.partlysunny.util.classes.gear

import com.google.common.base.Preconditions
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class PlayerGear private constructor() {
    private var hotbar: Array<ItemStack?>? = arrayOfNulls(9)
    private var offHand: ItemStack? = null
    private var armor: Array<ItemStack?>? = arrayOfNulls(4)

    init {
        Arrays.fill(hotbar, null)
        Arrays.fill(armor, null)
    }

    fun equip(p: Player) {
        Preconditions.checkArgument(hotbar != null, "Hotbar is null")
        Preconditions.checkArgument(armor != null, "Armor is null")
        Preconditions.checkArgument(hotbar!!.size == 9, "Hotbar is not length 9")
        Preconditions.checkArgument(armor!!.size == 4, "Armor is not length 4")
        val equipment = p.equipment
        equipment!!.armorContents = armor!!
        equipment.setItemInOffHand(offHand)
        for (i in 0..8) {
            p.inventory.setItem(i, hotbar!![i])
        }
    }

    class Builder {
        private val internal: PlayerGear

        init {
            internal = PlayerGear()
        }

        fun setOffHand(i: ItemStack?): Builder {
            internal.offHand = i
            return this
        }

        fun setArmor(vararg i: ItemStack?): Builder {
            internal.armor = arrayOf(*i)
            return this
        }

        fun setHotbar(vararg i: ItemStack?): Builder {
            internal.hotbar = arrayOf(*i)
            return this
        }

        fun build(): PlayerGear {
            return internal
        }

        companion object {
            fun builder(): Builder {
                return Builder()
            }
        }
    }
}
