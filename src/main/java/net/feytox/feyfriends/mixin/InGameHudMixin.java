package net.feytox.feyfriends.mixin;

import net.feytox.feyfriends.client.FeyFriendsClient;
import net.feytox.feyfriends.client.FeyFriendsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)

    public void onRender (MatrixStack matrices, float tickDelta, CallbackInfo info) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            List<String> playerslist = FeyFriendsClient.getPlayers();
            Map<String, List<String>> newCategoryStorage = FeyFriendsClient.getNewCategoryStorage(playerslist);
            // перебор нового хранилища
            Map<String, List<String>> categoryStorage = FeyFriendsClient.categoryStorage;
            for (String category: newCategoryStorage.keySet()) {
                Map<String, Object> categoryConfig = FeyFriendsConfig.categories.get(category);
                if (!categoryStorage.isEmpty() && !Objects.equals(newCategoryStorage.get(category), categoryStorage.get(category)) &&
                        (boolean) categoryConfig.get("sound_notif")) {
                    MinecraftClient.getInstance().world.playSound(
                            MinecraftClient.getInstance().player.getBlockPos(),
                            FeyFriendsConfig.getSoundFromInt(FeyFriendsConfig.convertToInt(categoryConfig.get("sound"))),
                            SoundCategory.BLOCKS,
                            1f,
                            1f,
                            false
                    );
                }

                int players = Objects.equals(category, "Online") ? playerslist.size() : newCategoryStorage.get(category).size();
                String hudPlayersList = String.join(", ", newCategoryStorage.get(category));
                String playersCount = (boolean) categoryConfig.get("show_players_list") && players > 0 ?
                        players + " (" + hudPlayersList + ")" :
                        String.valueOf(players);
                MinecraftClient.getInstance().textRenderer.draw(matrices, category + ": " + playersCount,
                        FeyFriendsConfig.convertToInt(categoryConfig.get("x")),
                        FeyFriendsConfig.convertToInt(categoryConfig.get("y")), -1);
            }
            FeyFriendsClient.categoryStorage = newCategoryStorage;
        }
    }
}
