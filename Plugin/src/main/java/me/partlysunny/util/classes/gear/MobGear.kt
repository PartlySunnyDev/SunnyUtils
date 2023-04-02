package me.partlysunny.util.classes.gear;

import com.google.common.base.Preconditions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MobGear {

    private ItemStack offHand = null;
    private ItemStack[] armor = new ItemStack[4];

    private MobGear() {
        Arrays.fill(armor, null);
    }

    public void equip(LivingEntity e) {
        Preconditions.checkArgument(armor != null, "Armor is null");
        Preconditions.checkArgument(armor.length == 4, "Armor is not length 4");

        EntityEquipment equipment = e.getEquipment();
        equipment.setArmorContents(armor);
        equipment.setItemInOffHand(offHand);
    }

    public static final class Builder {

        private final MobGear internal;

        public Builder() {
            internal = new MobGear();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder setOffHand(ItemStack i) {
            internal.offHand = i;
            return this;
        }

        public Builder setArmor(ItemStack... i) {
            internal.armor = i;
            return this;
        }

        public MobGear build() {
            return internal;
        }

    }

}
