package me.partlysunny.util;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.partlysunny.ConsoleLogger;
import me.partlysunny.gui.GuiManager;
import me.partlysunny.gui.SelectGui;
import me.partlysunny.gui.SelectGuiManager;
import me.partlysunny.gui.textInput.ChatListener;
import me.partlysunny.util.classes.ItemBuilder;
import me.partlysunny.util.classes.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

import static me.partlysunny.util.Util.fakeSpace;

public final class IFUtil {

    public static void setClickSoundTo(Sound s, Gui gui) {
        gui.setOnGlobalClick(event -> {
            if (event.getWhoClicked() instanceof Player a) {
                a.playSound(a.getLocation(), s, 1, 1);
            }
            event.setCancelled(true);
        });
    }

    public static void changePage(PaginatedPane p, int amount) {
        int current = p.getPage();
        int newAmount = current + amount;
        if (newAmount < 0) p.setPage(0);
        else p.setPage(Math.min(newAmount, p.getPages() - 1));
    }

    public static void addReturnButton(StaticPane pane, Player p, String returnTo, int x, int y) {
        pane.addItem(new GuiItem(ItemBuilder.builder(Material.ARROW).setName(ChatColor.GREEN + "Back").build(), item -> {
            GuiManager.openInventory(p, returnTo);
        }), x, y);
    }

    public static void addSelectionLink(StaticPane pane, Player p, String currentGui, String selectionLink, ItemStack toShow, int x, int y) {
        pane.addItem(new GuiItem(toShow, item -> {
            SelectGuiManager.getSelectGui(selectionLink.substring(0, selectionLink.length() - 6)).setReturnTo(p.getUniqueId(), currentGui);
            p.closeInventory();
            GuiManager.openInventory(p, selectionLink);
        }), x, y);
    }

    public static void addTextInputLink(StaticPane pane, Player p, String currentGui, String message, ItemStack toShow, int x, int y, Consumer<Player> toDo) {
        pane.addItem(new GuiItem(toShow, item -> {
            ChatListener.startChatListen(p, currentGui, message, toDo);
            p.closeInventory();
        }), x, y);
    }

    @SafeVarargs
    public static ChestGui getGeneralSelectionMenu(String title, Player p, Pair<String, ItemStack>... items) {
        if (items.length > 9) {
            ConsoleLogger.error("Too many items! (Max supported 9)");
        }
        double[] linspace = fakeSpace(items.length);
        ChestGui ui = new ChestGui(3, title);
        StaticPane pane = new StaticPane(0, 0, 9, 3);
        pane.fillWith(ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE).build());
        setClickSoundTo(Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, ui);
        int count = 0;
        for (double d : linspace) {
            int finalCount = count;
            pane.addItem(new GuiItem(
                    items[count].b(),
                    (item) -> {
                        GuiManager.openInventory(p, items[finalCount].a());
                    }
            ), (int) Math.round(d), 1);
            count++;
        }
        ui.addPane(pane);
        return ui;
    }

    public static void addListPages(PaginatedPane pane, Player p, SelectGui<?> from, int x, int y, int width, int height, String[] a, ChestGui gui) {
        pane.setOnClick(event -> {
            if (event.getWhoClicked() instanceof Player pp) {
                pp.playSound(pp.getLocation(), Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, 1, 1);
            }
            event.setCancelled(true);
        });
        int displaySize = width * height;
        if (displaySize < 1) {
            return;
        }
        int numPages = (int) Math.ceil(a.length / (displaySize * 1f));
        if (numPages == 0) {
            numPages = 1;
        }
        int count = 0;
        for (int i = 0; i < numPages; i++) {
            StaticPane border = new StaticPane(0, 0, 9, 5, Pane.Priority.HIGH);
            StaticPane items = new StaticPane(x, y, width, height, Pane.Priority.HIGHEST);
            addPageNav(pane, numPages, i, border, gui);
            items.fillWith(ItemBuilder.builder(Material.GRAY_STAINED_GLASS_PANE).setName("").build());
            for (int j = count; j < count + displaySize; j++) {
                if (j > a.length - 1) {
                    break;
                }
                String itemName = a[j];
                items.addItem(new GuiItem(ItemBuilder.builder(Material.PAPER).setName(ChatColor.GRAY + itemName).build(), item -> {
                    from.update(p.getUniqueId(), itemName);
                    from.returnTo(p);
                }), (j - count) % width, (j - count) / width);
            }
            count += displaySize;
            addReturnButton(border, p, from.getReturnTo(p), 0, 4);
            pane.addPane(i, border);
            pane.addPane(i, items);
        }
    }

    public static void addPageNav(PaginatedPane pane, int numPages, int i, StaticPane border, ChestGui gui) {
        border.fillWith(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).setName("").build());
        if (i != 0) {
            border.addItem(new GuiItem(ItemBuilder.builder(Material.ARROW).setName(ChatColor.GRAY + "Page Back").setLore(ChatColor.GREEN + "Right click for 5 pages", ChatColor.RED + "Shift Click for 15 pages").build(), item -> {
                if (item.isShiftClick()) changePage(pane, -15);
                else if (item.isLeftClick()) changePage(pane, -1);
                else if (item.isRightClick()) changePage(pane, -5);
                gui.update();
            }), 0, 2);
        }
        if (i != numPages - 1) {
            border.addItem(new GuiItem(ItemBuilder.builder(Material.ARROW).setName(ChatColor.GRAY + "Page Forward").setLore(ChatColor.GREEN + "Right click for 5 pages", ChatColor.RED + "Shift Click for 15 pages").build(), item -> {
                if (item.isShiftClick()) changePage(pane, 15);
                else if (item.isLeftClick()) changePage(pane, 1);
                else if (item.isRightClick()) changePage(pane, 5);
                gui.update();
            }), 8, 2);
        }
    }

}
