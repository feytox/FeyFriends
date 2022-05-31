package net.feytox.feyfriends.client;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FeyFriendsConfig extends MidnightConfig {
    @Entry
    public static int hudUpdateDelay = 200;
    @Entry
    public static Map<String, Map<String, Object>> categories = genCategories();

    static Map<String, Object> genOnlineStuff() {
        Map<String, Object> onlineplayers = new HashMap<>();

        onlineplayers.put("sound_notif", false);
        onlineplayers.put("sound", 1);
        onlineplayers.put("show_players_list", false);
        onlineplayers.put("x", 5);
        onlineplayers.put("y", 5);
        return onlineplayers;
    }
    private static Map<String, Map<String, Object>> genCategories() {
        Map<String, Map<String, Object>> categories = new HashMap<>();
        List<String> players = new ArrayList<>();
        Map<String, Object> category = genCategory(true, 1, false, 5, 15);
        categories.put("Friends", category);
        categories.put("Online", genOnlineStuff());
        return categories;
    }

    static Map<String, Object> genCategory(List<String> players, boolean sound_notif,
                                            int sound, boolean show_players_list, int x, int y) {
        Map<String, Object> category = new HashMap<>();

        category.put("players", players);
        category.put("sound_notif", sound_notif);
        category.put("sound", sound);
        category.put("show_players_list", show_players_list);
        category.put("x", x);
        category.put("y", y);

        return category;
    }

    static Map<String, Object> genCategory(boolean sound_notif, int sound, boolean show_players_list, int x, int y) {
        List<String> players = new ArrayList<>();
        return genCategory(players, sound_notif, sound, show_players_list, x, y);
    }

    public static SoundEvent getSoundFromDouble(Double sound_num) {
        int num = Math.round(convertToFloat(sound_num));
        return switch (num) {
            case 2 -> SoundEvents.BLOCK_NOTE_BLOCK_BELL;
            case 3 -> SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case 4 -> SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
            case 5 -> SoundEvents.BLOCK_ANVIL_LAND;
            default -> SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL;
        };
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
}

