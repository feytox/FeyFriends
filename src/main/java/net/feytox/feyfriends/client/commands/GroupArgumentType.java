package net.feytox.feyfriends.client.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.feytox.feyfriends.client.FeyFriendsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroupArgumentType implements IFeyArgumentType {
    boolean withOnline;

    private GroupArgumentType(boolean withOnline) {
        this.withOnline = withOnline;
    }

    public static GroupArgumentType group() {
        return new GroupArgumentType(true);
    }

    public static GroupArgumentType groupWithoutOnline() {
        return new GroupArgumentType(false);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> groups = new ArrayList<>(FeyFriendsConfig.categories.keySet().stream().toList());
        if (!this.withOnline) {
            groups.remove("Online");
        }

        groups.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
