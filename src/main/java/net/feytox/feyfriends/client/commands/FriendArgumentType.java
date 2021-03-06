package net.feytox.feyfriends.client.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.feytox.feyfriends.client.FeyFriendsConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendArgumentType implements IFeyArgumentType {
    private FriendArgumentType() {}

    public static FriendArgumentType friend() {
        return new FriendArgumentType();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String categoryName = FeyFriendsCommands.parseInput(context);
        if (FeyFriendsConfig.categories.containsKey(categoryName) && !categoryName.equals("Online")) {
            List<String> players = (List<String>) FeyFriendsConfig.categories.get(categoryName).get("players");
            players.forEach(builder::suggest);
            return builder.buildFuture();
        }
        return Suggestions.empty();
    }
}
