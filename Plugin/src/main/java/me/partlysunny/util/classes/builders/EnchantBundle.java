package me.partlysunny.util.classes.builders;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;

public enum EnchantBundle {

    BLANK(new HashMap<>()),
    DURABILITY(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.DURABILITY, 3).put(Enchantment.MENDING, 1).build()),
    ARMOR_HELM(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.PROTECTION_ENVIRONMENTAL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.WATER_WORKER, 1).put(Enchantment.OXYGEN, 3).put(Enchantment.MENDING, 1).build()),
    ARMOR_CHESTPLATE(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.PROTECTION_ENVIRONMENTAL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.MENDING, 1).build()),
    ARMOR_LEGGINGS(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.PROTECTION_ENVIRONMENTAL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.MENDING, 1).build()),
    ARMOR_BOOTS(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.PROTECTION_ENVIRONMENTAL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.PROTECTION_FALL, 1).put(Enchantment.DEPTH_STRIDER, 3).put(Enchantment.MENDING, 1).build()),
    WEAPON_FIRE(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.DAMAGE_ALL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.FIRE_ASPECT, 2).put(Enchantment.SWEEPING_EDGE, 3).put(Enchantment.LOOT_BONUS_MOBS, 3).put(Enchantment.MENDING, 1).build()),
    WEAPON(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.DAMAGE_ALL, 4).put(Enchantment.DURABILITY, 3).put(Enchantment.SWEEPING_EDGE, 3).put(Enchantment.LOOT_BONUS_MOBS, 3).put(Enchantment.MENDING, 1).build()),
    PICKAXE_FORTUNE(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.DIG_SPEED, 5).put(Enchantment.DURABILITY, 3).put(Enchantment.LOOT_BONUS_BLOCKS, 3).put(Enchantment.MENDING, 1).build()),
    PICKAXE_SILK(HashMapBuilder.builder(Enchantment.class, Integer.class).put(Enchantment.DIG_SPEED, 5).put(Enchantment.DURABILITY, 3).put(Enchantment.SILK_TOUCH, 3).put(Enchantment.MENDING, 1).build())
    ;

    private final HashMap<Enchantment, Integer> bundle;

    EnchantBundle(HashMap<Enchantment, Integer> bundle) {
        this.bundle = bundle;
    }

    public HashMap<Enchantment, Integer> bundle() {
        return bundle;
    }
}
