package me.partlysunny.util.classes.gear;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class PlayerGear {

    private ItemStack[] hotbar = new ItemStack[9];
    private ItemStack offHand = null;
    private ItemStack[] armor = new ItemStack[4];

    private PlayerGear() {
        Arrays.fill(hotbar, null);
        Arrays.fill(armor, null);
    }

    public void equip(Player p) {
        Preconditions.checkArgument(hotbar != null, "Hotbar is null");
        Preconditions.checkArgument(armor != null, "Armor is null");
        Preconditions.checkArgument(hotbar.length == 9, "Hotbar is not length 9");
        Preconditions.checkArgument(armor.length == 4, "Armor is not length 4");

        EntityEquipment equipment = p.getEquipment();
        equipment.setArmorContents(armor);
        equipment.setItemInOffHand(offHand);
        for (int i = 0; i < 9; i++) {
            p.getInventory().setItem(i, hotbar[i]);
        }
    }

    public static final class Builder {

        private final PlayerGear internal;

        public Builder() {
            internal = new PlayerGear();
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

        public Builder setHotbar(ItemStack... i) {
            internal.hotbar = i;
            return this;
        }

        public PlayerGear build() {
            return internal;
        }

    }
}
