package net.feytox.feyfriends.client.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface IFeyArgumentType extends ArgumentType<String>, Serializable {

    <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder);

    @Nullable
    @Override
    default String parse(StringReader reader) {
        int argBeginning = reader.getCursor();
        if(!reader.canRead()){
            reader.skip();
        }
        while(reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        return reader.getString().substring(argBeginning, reader.getCursor());
    }
}
