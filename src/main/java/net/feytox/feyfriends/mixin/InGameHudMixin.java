package net.feytox.feyfriends.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.feytox.feyfriends.client.FeyFriendsClient;
import net.feytox.feyfriends.client.FeyFriendsConfig;
import net.feytox.feyfriends.client.NotificationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))

    public void onRender (MatrixStack matrices, float tickDelta, CallbackInfo info) {
        int change;
        if (MinecraftClient.getInstance().getNetworkHandler() != null && FeyFriendsConfig.showHUD) {
            List<String> playerslist = FeyFriendsClient.getPlayers();
            Map<String, List<String>> newCategoryStorage = FeyFriendsClient.getNewCategoryStorage(playerslist);
            // перебор нового хранилища
            Map<String, List<String>> categoryStorage = FeyFriendsClient.categoryStorage;
            for (String category: newCategoryStorage.keySet()) {
                Map<String, Object> categoryConfig = FeyFriendsConfig.categories.get(category);
                if (categoryConfig != null && categoryStorage != null && categoryStorage.containsKey(category)) {
                    if (!Objects.equals(newCategoryStorage.get(category), categoryStorage.get(category)) && !Objects.equals(categoryConfig.get("notif_type"), NotificationType.OFF.getNotifName())) {
                        change = newCategoryStorage.get(category).size() - categoryStorage.get(category).size();
                        if (change != 0 && Objects.equals(categoryConfig.get("notif_type"),
                                NotificationType.BOTH.getNotifName())) {
                            FeyFriendsClient.playNotification(FeyFriendsConfig.convertToInt(categoryConfig.get("sound")));
                        }
                        else if (change < 0 && Objects.equals(categoryConfig.get("notif_type"),
                                NotificationType.ON_LEAVE.getNotifName())) {
                            FeyFriendsClient.playNotification(FeyFriendsConfig.convertToInt(categoryConfig.get("sound")));
                        } else if (change > 0 && Objects.equals(categoryConfig.get("notif_type"),
                                NotificationType.ON_JOIN.getNotifName())) {
                            FeyFriendsClient.playNotification(FeyFriendsConfig.convertToInt(categoryConfig.get("sound")));
                        }
                    }

                    int players = Objects.equals(category, "Online") ? playerslist.size() : newCategoryStorage.get(category).size();
                    String hudPlayersList = String.join(", ", newCategoryStorage.get(category));
                    String playersCount = (boolean) categoryConfig.get("show_players_list") && players > 0 ?
                            players + " (" + hudPlayersList + ")" :
                            String.valueOf(players);
                    MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, category + ": " + playersCount,
                            FeyFriendsConfig.convertToInt(categoryConfig.get("x")),
                            FeyFriendsConfig.convertToInt(categoryConfig.get("y")), -1);
                }
            }
            FeyFriendsClient.categoryStorage = newCategoryStorage;
        }
        if (FeyFriendsClient.isReloadNeeded) {
            Path new_config = FabricLoader.getInstance().getConfigDir().resolve("feyfriends_backup.json");
            Path old_config = FabricLoader.getInstance().getConfigDir().resolve("feyfriends.json");
            try {
                Files.copy(new_config, old_config, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FeyFriendsConfig.init("feyfriends", FeyFriendsConfig.class);
            FeyFriendsConfig.checkUpdates();
            FeyFriendsClient.isReloadNeeded = false;
        }
    }
}
