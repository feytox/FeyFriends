package ru.feytox.feyfriends.client.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ValueArgumentType implements IFeyArgumentType {
    int neededArgIndex;

    private ValueArgumentType(int neededArgIndex) {
        this.neededArgIndex = neededArgIndex;
    }

    public static ValueArgumentType value(int neededArgIndex) {
        return new ValueArgumentType(neededArgIndex);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String valueType = FeyFriendsCommands.parseInput(context, neededArgIndex);
        List<String> valueSuggestions;

        switch (valueType) {
            case "notification_type" -> valueSuggestions = new ArrayList<>(Arrays.asList("OFF", "ON_JOIN", "ON_LEAVE", "BOTH"));
            case "sound" -> valueSuggestions = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
            case "display_players" -> valueSuggestions = new ArrayList<>(Arrays.asList("true", "false"));
            default -> {
                return Suggestions.empty();
            }
        }

        valueSuggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
