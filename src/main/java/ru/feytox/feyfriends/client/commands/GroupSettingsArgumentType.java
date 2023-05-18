package ru.feytox.feyfriends.client.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ru.feytox.feyfriends.client.FeyFriendsConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroupSettingsArgumentType implements IFeyArgumentType {
    int neededArgIndex;

    private GroupSettingsArgumentType(int neededArgIndex) {
        this.neededArgIndex = neededArgIndex;
    }

    public static GroupSettingsArgumentType groupSettings(int neededArgIndex) {
        return new GroupSettingsArgumentType(neededArgIndex);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String categoryName = FeyFriendsCommands.parseInput(context, this.neededArgIndex);
        if (FeyFriendsConfig.categories.containsKey(categoryName)) {
            List<String> settings = new ArrayList<>(Arrays.asList(
                    "notification_type", "sound", "display_players", "x", "y"));
            settings.forEach(builder::suggest);
            return builder.buildFuture();
        }

        return Suggestions.empty();
    }
}
