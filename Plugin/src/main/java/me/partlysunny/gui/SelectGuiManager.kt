package me.partlysunny.gui;

import me.partlysunny.gui.guis.common.EnchantmentSelectGui;
import me.partlysunny.gui.guis.common.EntityTypeSelectGui;
import me.partlysunny.gui.guis.common.PotionEffectTypeSelectGui;
import me.partlysunny.gui.guis.common.item.ItemMakerSelectGui;
import me.partlysunny.gui.guis.common.item.enchant.EnchantCreationSelectGui;
import me.partlysunny.gui.guis.common.item.enchant.EnchantModifierSelectGui;

import java.util.HashMap;
import java.util.Map;

public class SelectGuiManager {

    private static final Map<String, SelectGui<?>> selectGuis = new HashMap<>();

    public static void registerSelectGui(String id, SelectGui<?> selectGui) {
        selectGuis.put(id, selectGui);
        GuiManager.registerGui(id + "Select", selectGui);
    }

    public static SelectGui<?> getSelectGui(String id) {
        return selectGuis.get(id);
    }

    public static void unregisterSelectGui(String id) {
        selectGuis.remove(id);
    }

    public static void init() {
        registerSelectGui("enchantment", new EnchantmentSelectGui());
        registerSelectGui("entityType", new EntityTypeSelectGui());
        registerSelectGui("potionEffectType", new PotionEffectTypeSelectGui());
        registerSelectGui("itemMaker", new ItemMakerSelectGui());
        registerSelectGui("enchantModifier", new EnchantModifierSelectGui());
        registerSelectGui("enchantCreation", new EnchantCreationSelectGui());
    }

}
