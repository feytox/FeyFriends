package net.feytox.feyfriends.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Environment(EnvType.CLIENT)
public class FeyFriendsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("feyfriends");
	public static Map<String, List<String>> categoryStorage = new HashMap<>();
	public static int ticks = 0;

	@Override
	public void onInitializeClient() {
		FeyFriendsConfig.init("feyfriends", FeyFriendsConfig.class);

		KeyBinding configguikeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.feyfriends.configguikeybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.feyfriends"));
		KeyBinding onlinekeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.feyfriends.onlinekeybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.feyfriends"));
		KeyBinding configplayers_keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.feyfriends.configplayers_keybind",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category.feyfriends"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
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

}


