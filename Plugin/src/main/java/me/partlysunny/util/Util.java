package me.partlysunny.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.partlysunny.SunnySpigotBaseCore;
import me.partlysunny.gui.textInput.ChatListener;
import me.partlysunny.util.classes.Pair;
import me.partlysunny.util.reflection.JavaAccessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public final class Util {

    public static final Random RAND = new Random();

    /**
     * With this method you can get a player's head by nickname or a base64 head by base64 code
     *
     * @param type  Determines whether you want to get the head by name or by base64
     * @param value If you want a player's head, then the player's name. If you want base64, then base64 code.
     * @return Head itemStack
     */
    public static ItemStack convert(HeadType type, String value) {
        if (type.equals(HeadType.PLAYER_HEAD)) {
            return getSkullByTexture(getPlayerHeadTexture(value));
        } else {
            return getSkullByTexture(value);
        }
    }

    private static ItemStack getSkullByTexture(String url) {
        ItemStack head = getAllVersionStack("SKULL_ITEM", "PLAYER_HEAD");
        if (url.isEmpty() || url.equals("none")) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", url));
        try {
            JavaAccessor.setValue(meta, JavaAccessor.getField(meta.getClass(), "profile"), profile);
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    private static String getPlayerHeadTexture(String username) {
        if (getPlayerId(username).equals("none")) return "none";
        String url = "https://api.minetools.eu/profile/" + getPlayerId(username);
        try {
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            JSONObject jsonData = (JSONObject) parsedData;
            JSONObject decoded = (JSONObject) jsonData.get("raw");
            JSONArray textures = (JSONArray) decoded.get("properties");
            JSONObject data = (JSONObject) textures.get(0);

            return data.get("value").toString();
        } catch (Exception ex) {
            return "none";
        }
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }

    private static String getPlayerId(String playerName) {
        try {
            String url = "https://api.minetools.eu/uuid/" + playerName;
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            JSONObject jsonData = (JSONObject) parsedData;

            if (jsonData.get("id") != null) return jsonData.get("id").toString();
            return "";
        } catch (Exception ex) {
            return "none";
        }
    }

    private static ItemStack getAllVersionStack(String oldName, String newName) {
        Material material = null;
        try {
            material = Material.valueOf(oldName);
        } catch (Exception exception) {
            material = Material.valueOf(newName);
        }
        return new ItemStack(material, 1);
    }

    public static int getRandomBetween(int a, int b) {
        if (a > b) {
            throw new IllegalArgumentException("a must be higher than b");
        }
        if (a == b) {
            return a;
        }
        if (a < 0 && b < 0) {
            return -getRandomBetween(-b, -a);
        }
        if (a < 0) {
            return getRandomBetween(0, -a + b) + a;
        }
        return RAND.nextInt(b - a) + a;
    }

    public static String processText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('&', ChatColor.COLOR_CHAR);
    }

    public static void scheduleRepeatingCancelTask(Runnable r, long delay, long repeat, long stopAfter) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        JavaPlugin p = JavaPlugin.getPlugin(SunnySpigotBaseCore.class);
        BukkitTask t = scheduler.runTaskTimer(p, r, delay, repeat);
        scheduler.runTaskLater(p, t::cancel, stopAfter);
    }

    public static List<String> processTexts(List<String> texts) {
        List<String> result = new ArrayList<>();
        texts.forEach(n -> result.add(processText(n)));
        return result;
    }

    public static <T> T getOrDefault(ConfigurationSection y, String key, T def) {
        if (y.contains(key)) {
            return (T) y.get(key);
        }
        return def;
    }

    public static <T> T getOrError(ConfigurationSection y, String key) {
        if (y.contains(key)) {
            return (T) y.get(key);
        }
        throw new IllegalArgumentException("Key " + key + " inside " + y.getName() + " was not found!");
    }

    public static boolean isInvalidFilePath(String path) {
        File f = new File(path);
        try {
            f.getCanonicalPath();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public static void copy(String source, File destination) throws IOException {
        InputStream stream = SunnySpigotBaseCore.class.getClassLoader().getResourceAsStream(source);
        if (!destination.exists()) {
            Files.copy(stream, destination.toPath());
        }
    }

    public static List<String> splitLoreForLine(String input, String linePrefix, String lineSuffix, int width) {
        char[] array = input.toCharArray();
        List<String> out = new ArrayList<>();
        String currentColor = "";
        String cachedColor = "";
        boolean wasColorChar = false;
        StringBuilder currentLine = new StringBuilder(linePrefix);
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if (wasColorChar) {
                wasColorChar = false;
                cachedColor = currentColor;
                Pattern pattern = Pattern.compile("[0-9a-fkmolnr]");
                if (pattern.matcher(c + "").matches()) {
                    if (c == 'r') {
                        currentColor = ChatColor.COLOR_CHAR + "r";
                    } else {
                        currentColor += ChatColor.COLOR_CHAR + "" + c;
                    }
                }
                currentWord.append(ChatColor.COLOR_CHAR + "").append(c);
                continue;
            }
            if (c == '\n') {
                currentLine.append(currentWord);
                currentWord = new StringBuilder();
                out.add(currentLine + lineSuffix);
                currentLine = new StringBuilder(linePrefix + cachedColor + currentWord);
                cachedColor = currentColor;
                continue;
            }
            if (c == ' ') {
                if ((currentLine + currentWord.toString()).replaceAll("ยง[0-9a-fklmnor]", "").length() > width) {
                    out.add(currentLine + lineSuffix);
                    currentLine = new StringBuilder(linePrefix + cachedColor + currentWord + " ");
                } else {
                    currentLine.append(currentWord).append(" ");
                }
                cachedColor = currentColor;
                currentWord = new StringBuilder();
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                wasColorChar = true;
                continue;
            }
            currentWord.append(c);
        }
        currentLine.append(currentWord);
        out.add(currentLine + lineSuffix);
        return out;
    }

    public static String[] getAlphabetSorted(String[] values) {
        List<String> strings = new ArrayList<>(List.of(values));
        Collections.sort(strings);
        return strings.toArray(new String[0]);
    }

    public static List<String> splitLoreForLine(String input) {
        return splitLoreForLine(input, ChatColor.GRAY.toString(), "", 30);
    }

    public static double[] linspace(double min, double max, int points) {
        double[] d = new double[points];
        for (int i = 0; i < points; i++) {
            d[i] = min + i * (max - min) / (points - 1);
        }
        return d;
    }

    public static double[] fakeSpace(int points) {
        return switch (points) {
            case 0 -> new double[]{};
            case 1 -> new double[]{4};
            case 2 -> new double[]{3, 5};
            case 3 -> new double[]{2, 4, 6};
            case 4 -> new double[]{1, 3, 5, 7};
            case 5 -> new double[]{2, 3, 4, 5, 6};
            case 6 -> new double[]{1, 2, 3, 5, 6, 7};
            case 7 -> new double[]{1, 2, 3, 4, 5, 6, 7};
            case 8 -> new double[]{0, 1, 2, 3, 5, 6, 7, 8};
            default -> new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        };
    }

    public static void invalid(String message, Player p) {
        p.sendMessage(ChatColor.RED + message);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    }

    public static void addLoreLine(ItemStack s, String... lines) {
        ItemMeta m = s.getItemMeta();
        List<String> lore = m.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.addAll(List.of(lines));
        m.setLore(lore);
        s.setItemMeta(m);
    }

    public static void setName(ItemStack i, String name) {
        ItemMeta m = i.getItemMeta();
        if (m == null) {
            return;
        }
        m.setDisplayName(name);
        i.setItemMeta(m);
    }

    public static Integer getTextInputAsInt(Player pl) {
        String input = ChatListener.getCurrentInput(pl);
        if (input.equals("cancel")) {
            return null;
        }
        int currentInput;
        try {
            currentInput = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            pl.sendMessage(ChatColor.RED + "Invalid number!");
            return null;
        }
        if (currentInput < 1) {
            pl.sendMessage("Must be greater than 1!");
            return null;
        }
        return currentInput;
    }

    public static Double getTextInputAsDouble(Player pl) {
        String input = ChatListener.getCurrentInput(pl);
        if (input.equals("cancel")) {
            return null;
        }
        double currentInput;
        try {
            currentInput = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            pl.sendMessage(ChatColor.RED + "Invalid number!");
            return null;
        }
        if (currentInput < 0) {
            pl.sendMessage("Must be greater than 0!");
            return null;
        }
        return currentInput;
    }

    public static void setLore(ItemStack i, List<String> lore) {
        ItemMeta m = i.getItemMeta();
        if (m == null) {
            return;
        }
        m.setLore(lore);
        i.setItemMeta(m);
    }

    public static PotionType asType(PotionEffectType t) {
        if (t == null) {
            return PotionType.WATER;
        }
        PotionType asType = PotionType.WATER;
        for (PotionType type : PotionType.values()) {
            if (t.equals(type.getEffectType())) asType = type;
        }
        return asType;
    }

    public static void deleteFile(File f) {
        if (f.exists() && !f.isDirectory()) {
            f.delete();
        }
    }

    /**
     * Generation head type enum
     */
    public enum HeadType {
        PLAYER_HEAD,
        BASE64
    }
}
