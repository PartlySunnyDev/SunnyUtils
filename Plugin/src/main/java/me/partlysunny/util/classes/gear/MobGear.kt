package me.partlysunny.util.classes.gear

import com.google.common.base.Preconditions
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.*

class MobGear private constructor() {
    private var offHand: ItemStack? = null
    private var armor: Array<ItemStack?>? = arrayOfNulls(4)

    init {
        armor?.let { Arrays.fill(it, null) }
    }

    fun equip(e: LivingEntity) {
        Preconditions.checkArgument(armor != null, "Armor is null")
        Preconditions.checkArgument(armor!!.size == 4, "Armor is not length 4")
        val equipment = e.equipment
        equipment!!.armorContents = armor!!
        equipment.setItemInOffHand(offHand)
    }

    class Builder {
        private val internal: MobGear = MobGear()

        fun setOffHand(i: ItemStack?): Builder {
            internal.offHand = i
            return this
        }

        fun setArmor(vararg i: ItemStack?): Builder {
            internal.armor = arrayOf(*i)
            return this
        }

        fun build(): MobGear {
            return internal
        }

        companion object {
            fun builder(): Builder {
                return Builder()
            }
        }
    }
}
