package name.uwu.feytox.feyfriends;

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import java.util.*;

@Environment(EnvType.CLIENT)
public class FeyFriendsClient implements ModInitializer {
	public static Map<String, List<String>> categoryStorage = new HashMap<>();
	public static int ticks = 0;

	public static boolean isReloadNeeded = false;

	@Override
	public void onInitialize(ModContainer mod) {
		FeyFriendsConfig.init("feyfriends", FeyFriendsConfig.class);
		FeyFriendsConfig.checkUpdates();

		KeyBind configguikeybind = KeyBindingHelper.registerKeyBinding(new KeyBind("key.feyfriends.configguikeybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.feyfriends"));
		KeyBind onlinekeybind = KeyBindingHelper.registerKeyBinding(new KeyBind("key.feyfriends.onlinekeybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.feyfriends"));
		KeyBind configplayers_keybind = KeyBindingHelper.registerKeyBinding(new KeyBind("key.feyfriends.configplayers_keybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category.feyfriends"));

		ClientTickEvents.END.register(client -> {
			if (!categoryStorage.isEmpty() && client.player == null) {
				categoryStorage.clear();
			}
			ticks += 1;
			if (ticks != FeyFriendsConfig.hudUpdateDelay && client.player == null) {
				ticks = FeyFriendsConfig.hudUpdateDelay;
			}

			while (configplayers_keybind.wasPressed()) {
				if (client.player != null & client.world != null && client.getNetworkHandler() != null) {
					Map<String, Map<String, Object>> categories = FeyFriendsConfig.categories;
					for (String category_name: categories.keySet()) {
						if (!Objects.equals(category_name, "Online")) {
							Map<String, Object> category = categories.get(category_name);
							String msg = category_name + ": " + String.join(", ", (List<String>) category.get("players"));
							client.player.sendMessage(new LiteralText(msg), false);
						}
					}
				}
			}

			while (onlinekeybind.wasPressed()) {
				if (client.player != null & client.world != null && client.getNetworkHandler() != null) {
					List<String> playerslist = getPlayers();
					String players = String.join(", ", playerslist);
					int playercount = playerslist.size();
					client.player.sendMessage(new LiteralText("Players: " + players), false);
					client.player.sendMessage(new LiteralText("Online = " + playercount), false);
				}
			}
			while (configguikeybind.wasPressed()) {
				MinecraftClient.getInstance().setScreen(new GuiScreen(new FeyFriendsGui()));
			}
		});
	}

	public static List<String> getPlayers() {
		var playerListEntries = MinecraftClient.getInstance().getNetworkHandler().getPlayerList();
		List<String> playerslist = new ArrayList<>();
		for (var player: playerListEntries) {
			String nick = player.getProfile().getName();
			playerslist.add(nick);
		}
		return playerslist;
	}

	public static Map<String, List<String>> getNewCategoryStorage(List<String> playerslist) {
		if (ticks >= FeyFriendsConfig.hudUpdateDelay) {
			ticks = 0;
			Map<String, List<String>> categoryPlayers = new HashMap<>();
			// перебор категорий и игроков из конфига в новый Map
			for (String category : FeyFriendsConfig.categories.keySet()) {
				categoryPlayers.put(category, new ArrayList<>());
				List<String> players = new ArrayList<>();
				List<String> players_config = (List<String>) FeyFriendsConfig.categories.get(category).get("players");
				if (players_config != null) {
					for (String player_name : players_config) {
						players.add(player_name.toLowerCase());
					}
				}
				if (!Objects.equals(category, "Online") && players_config != null) {
					categoryPlayers.get(category).addAll(players);
				}
			}
			Map<String, List<String>> newCategoryStorage = new HashMap<>();
			// перебор играющих игроков и нового Map
			for (String category : categoryPlayers.keySet()) {
				newCategoryStorage.put(category, new ArrayList<>());
				if (!Objects.equals(category, "Online")) {
					for (String player : playerslist) {
						if (categoryPlayers.get(category).contains(player.toLowerCase())) {
							newCategoryStorage.get(category).add(player);
						}
					}
				} else {
					newCategoryStorage.get(category).addAll(playerslist);
				}
			}
			return newCategoryStorage;
		}
		else {
			return categoryStorage;
		}
	}

	public static void playNotification(int soundNum) {
		MinecraftClient.getInstance().world.playSound(
				MinecraftClient.getInstance().player.getBlockPos(),
				FeyFriendsConfig.getSoundFromInt(soundNum),
				SoundCategory.BLOCKS,
				1f,
				1f,
				false
		);
	}
}
