package net.feytox.feyfriends.client;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.*;


public class FeyFriendsConfig extends MidnightConfig {

    @Entry
    public static boolean showHUD = true;

    @Entry
    public static int hudUpdateDelay = 200;

    @Entry
    public static Map<String, Map<String, Object>> categories = genCategories();

    static Map<String, Object> genOnlineStuff() {
        Map<String, Object> onlineplayers = new HashMap<>();

        onlineplayers.put("notif_type", NotificationType.OFF.getNotifName());
        onlineplayers.put("sound", 1);
        onlineplayers.put("show_players_list", false);
        onlineplayers.put("x", 5);
        onlineplayers.put("y", 5);
        return onlineplayers;
    }
    private static Map<String, Map<String, Object>> genCategories() {
        Map<String, Map<String, Object>> categories = new HashMap<>();
        List<String> players = new ArrayList<>();
        Map<String, Object> category = genCategory(15);
        categories.put("Friends", category);
        categories.put("Online", genOnlineStuff());
        return categories;
    }

    static Map<String, Object> genCategory(List<String> players,
                                           int y) {
        Map<String, Object> category = new HashMap<>();

        category.put("players", players);
        category.put("notif_type", NotificationType.ON_JOIN.getNotifName());
        category.put("sound", 1);
        category.put("show_players_list", false);
        category.put("x", 5);
        category.put("y", y);

        return category;
    }

    static Map<String, Object> genCategory(int y) {
        List<String> players = new ArrayList<>();
        return genCategory(players, y);
    }

    public static SoundEvent getSoundFromInt(int sound_num) {
        return switch (sound_num) {
            case 2 -> SoundEvents.BLOCK_NOTE_BLOCK_BELL;
            case 3 -> SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case 4 -> SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
            case 5 -> SoundEvents.BLOCK_ANVIL_LAND;
            default -> SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL;
        };
    }

    public static Float convertToFloat(Double doubleValue) {
        return doubleValue == null ? null : doubleValue.floatValue();
    }

    public static int convertToInt(Object num) {
        if (num instanceof Double) {
            return (int) Math.round((Double) num);
        } else {
            return (int) num;
        }
    }

    public static void write() {
        write("feyfriends");
        FeyFriendsClient.ticks = FeyFriendsConfig.hudUpdateDelay;
    }

    public static int getNewCategoryY() {
        String lastCategoryName = null;
        for (String category_name: categories.keySet()) {
            lastCategoryName = category_name;
        }

        Map<String, Object> default_category = new HashMap<>();
        default_category.put("y", 5);

        return (convertToInt(categories.getOrDefault(lastCategoryName, default_category).get("y"))) + 10;
    }

    public enum NotificationType {
        OFF("OFF"),
        ON_JOIN("ON_JOIN"),
        ON_LEAVE("ON_LEAVE"),
        BOTH("BOTH");

        private final String notifName;
        NotificationType(String notifName) {
            this.notifName = notifName;
        }
        public String getNotifName() { return notifName; }
    }

    public static List<String> notificationTypes = new ArrayList<>(Arrays.asList("OFF", "ON_JOIN", "ON_LEAVE", "BOTH"));

    public static void checkUpdates() {
        // 0.2.1 -> 0.3.0
        for (String category_name: categories.keySet()) {
            if (!categories.get(category_name).containsKey("notif_type")) {
                if ((boolean) categories.get(category_name).get("sound_notif")) {
                    categories.get(category_name).put("notif_type", NotificationType.ON_JOIN.getNotifName());
                }
                else {
                    categories.get(category_name).put("notif_type", NotificationType.OFF.getNotifName());
                }
                categories.get(category_name).remove("sound_notif");
                write();
            }
        }
    }
}

