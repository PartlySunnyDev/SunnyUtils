package me.partlysunny.util.classes;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Gear {

    private ItemStack[] hotbar = new ItemStack[9];
    private ItemStack offHand = null;
    private ItemStack[] armor = new ItemStack[4];

    private Gear() {
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

    public static final class GearBuilder {

        private final Gear internal;

        public GearBuilder() {
            internal = new Gear();
        }

        public static GearBuilder builder() {
            return new GearBuilder();
        }

        public GearBuilder setOffHand(ItemStack i) {
            internal.offHand = i;
            return this;
        }

        public GearBuilder setArmor(ItemStack... i) {
            internal.armor = i;
            return this;
        }

        public GearBuilder setHotbar(ItemStack... i) {
            internal.hotbar = i;
            return this;
        }

        public Gear build() {
            return internal;
        }

    }
}
