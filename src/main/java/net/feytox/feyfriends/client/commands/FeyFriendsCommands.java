package net.feytox.feyfriends.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.feytox.feyfriends.client.FeyFriendsClient;
import net.feytox.feyfriends.client.FeyFriendsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FeyFriendsCommands {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("feyfriends")
                .then(literal("addGroup")
                        .then(argument("group name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String groupName = parseLast(context);
                                    if (!FeyFriendsConfig.categories.containsKey(groupName)) {
                                        FeyFriendsConfig.categories.put(groupName, FeyFriendsConfig.genCategory(
                                                FeyFriendsConfig.getNewCategoryY()));
                                        FeyFriendsConfig.write();
                                        sendTranslatableText("feyfriends.addgroup.success");
                                    } else {
                                        sendTranslatableText("feyfriends.addgroup.fail");
                                    }
                                    return 1;
                                })))
                .then(literal("delGroup")
                        .then(argument("group name", GroupArgumentType.group())
                                .executes(context -> {
                                    String groupName = parseLast(context);
                                    if (FeyFriendsConfig.categories.containsKey(groupName)) {
                                        FeyFriendsConfig.categories.remove(groupName);
                                        FeyFriendsConfig.write();
                                        sendTranslatableText("feyfriends.delgroup.success");
                                    } else {
                                        sendTranslatableText("feyfriends.delgroup.fail");
                                    }
                                    return 1;
                                })))
                .then(literal("groupList")
                        .executes(context -> {
                            Map<String, Map<String, Object>> categories = FeyFriendsConfig.categories;
                            for (String category_name: categories.keySet()) {
                                if (!Objects.equals(category_name, "Online")) {
                                    Map<String, Object> category = categories.get(category_name);
                                    String msg = category_name + ": " + String.join(", ", (List<String>) category.get("players"));
                                    sendMessage(Text.literal(msg));
                                }
                            }
                            return 1;
                        }))
                .then(literal("addPlayer")
                        .then(argument("group name", GroupArgumentType.groupWithoutOnline())
                                .then(argument("nickname", EntityArgumentType.player())
                                        .executes(context -> {
                                            String groupName = parseInput(context, 2);
                                            String nick = parseInput(context, 3);

                                            if (!Objects.equals(groupName, "Online") && FeyFriendsConfig.categories.containsKey(groupName)) {
                                                List<String> friends = (List<String>) FeyFriendsConfig.categories.get(groupName).get("players");
                                                if (!friends.contains(nick)) {
                                                    friends.add(nick);
                                                    FeyFriendsConfig.categories.get(groupName).put("players", friends);
                                                    FeyFriendsConfig.write();

                                                    sendFormattedText("feyfriends.addplayer.success", nick);
                                                } else {
                                                    sendTranslatableText("feyfriends.addplayer.already");
                                                }
                                            } else {
                                                sendTranslatableText("feyfriends.delgroup.fail");
                                            }
                                            return 1;
                                        }))))
                .then(literal("delPlayer")
                        .then(argument("group name", GroupArgumentType.groupWithoutOnline())
                                .then(argument("nickname", FriendArgumentType.friend(2))
                                        .executes(context -> {
                                            String groupName = parseInput(context, 2);
                                            String nick = parseInput(context, 3);

                                            if (!Objects.equals(groupName, "Online") && FeyFriendsConfig.categories.containsKey(groupName)) {
                                                List<String> friends = (List<String>) FeyFriendsConfig.categories.get(groupName).get("players");
                                                if (friends.contains(nick)) {
                                                    friends.remove(nick);
                                                    FeyFriendsConfig.categories.get(groupName).put("players", friends);
                                                    FeyFriendsConfig.write();

                                                    sendFormattedText("feyfriends.delplayer.success", nick);
                                                } else {
                                                    sendTranslatableText("feyfriends.delplayer.failplayer");
                                                }
                                            } else {
                                                sendTranslatableText("feyfriends.delgroup.fail");
                                            }

                                            return 1;
                                        }))))
                .then(literal("modifyGroup")
                        .then(argument("group name", GroupArgumentType.group())
                                .then(argument("setting name", GroupSettingsArgumentType.groupSettings(2))
                                        .executes(context -> {
                                            String groupName = parseInput(context, 2);
                                            String settingName = parseInput(context, 3);

                                            if (FeyFriendsConfig.categories.containsKey(groupName)) {
                                                String configName = settingToConfigName(settingName);
                                                if (configName != null) {
                                                    sendFormattedText("feyfriends.modifygroup.show.success",
                                                            String.valueOf(FeyFriendsConfig.categories.get(groupName).get(configName)));
                                                } else {
                                                    sendTranslatableText("feyfriends.modifygroup.show.failsetting");
                                                }

                                            } else {
                                                sendTranslatableText("feyfriends.delgroup.fail");
                                            }

                                            return 1;
                                        })
                                        .then(argument("new value", ValueArgumentType.value(3))
                                                .executes(context -> {
                                                    String groupName = parseInput(context, 2);
                                                    String settingName = parseInput(context, 3);
                                                    String inputValue = parseInput(context, 4);

                                                    Object newValue = null;


                                                    switch (settingName) {
                                                        case "notification_type" -> newValue = FeyFriendsConfig.notificationTypes.contains(inputValue) ? inputValue : null;
                                                        case "sound" -> {
                                                            try {
                                                                int intInput = Integer.parseInt(inputValue);
                                                                newValue = intInput >= 1 && intInput <= 5 ? intInput : null;
                                                            } catch (NumberFormatException ignored) { }
                                                        }
                                                        case "display_players" -> {
                                                            if (inputValue.equals("true")) {
                                                                newValue = true;
                                                            } else if (inputValue.equals("false")) {
                                                                newValue = false;
                                                            }
                                                        }
                                                        case "x", "y" -> {
                                                            try {
                                                                newValue = Integer.parseInt(inputValue);
                                                            } catch (IllegalArgumentException ignored) { }
                                                        }
                                                    }

                                                    if (newValue != null && FeyFriendsConfig.categories.containsKey(groupName)) {
                                                        String configName = settingToConfigName(settingName);

                                                        String oldValue = String.valueOf(FeyFriendsConfig.categories.get(groupName).get(configName));
                                                        FeyFriendsConfig.categories.get(groupName).put(configName, newValue);

                                                        if (configName.equals("sound") && newValue instanceof Integer) {
                                                            FeyFriendsClient.playNotification((Integer) newValue);
                                                        }

                                                        sendFormattedText("feyfriends.modifygroup.success", oldValue);
                                                    } else {
                                                        sendTranslatableText("feyfriends.modifygroup.fail");
                                                    }

                                                    return 1;
                                                })))))
                .then(literal("loadBackup")
                        .executes(context -> {
                            sendTranslatableText("feyfriends.loadbackup.idk");
                            FeyFriendsClient.isReloadNeeded = true;
                            return 1;
                        }))
                .then(literal("saveBackup")
                        .executes(context -> {
                            Path backup = FabricLoader.getInstance().getConfigDir().resolve("feyfriends_backup.json");
                            Path config = FabricLoader.getInstance().getConfigDir().resolve("feyfriends.json");
                            try {
                                Files.copy(config, backup, StandardCopyOption.REPLACE_EXISTING);
                                sendTranslatableText("feyfriends.savebackup.success");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sendTranslatableText("feyfriends.savebackup.fail");
                            }
                            return 1;
                        })))));
    }

    protected static <S> String parseInput(CommandContext<S> context, int argIndex) {
        String[] inputSplitted = context.getInput().split(" ");
        return inputSplitted[argIndex];
    }

    private static <S> String parseLast(CommandContext<S> context) {
        String[] inputSplitted = context.getInput().split(" ");
        return inputSplitted[inputSplitted.length-1];
    }

    private static void sendFormattedText(String key, Object formatObj) {
        sendMessage(Text.literal(I18n.translate(key, formatObj)));
    }

    private static void sendTranslatableText(String key) {
        sendMessage(Text.translatable(key));
    }

    private static void sendMessage(Text message) {
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }

    @Nullable
    private static String settingToConfigName(String settingName) {
        String configName = null;

        switch (settingName) {
            case "notification_type" -> configName = "notif_type";
            case "display_players" -> configName = "show_players_list";
            case "x", "sound", "y" -> configName = settingName;
        }

        return configName;
    }

}
