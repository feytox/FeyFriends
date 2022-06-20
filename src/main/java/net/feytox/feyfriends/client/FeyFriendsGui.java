package net.feytox.feyfriends.client;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeyFriendsGui extends LightweightGuiDescription {
    public FeyFriendsGui() {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(625, 240);
        root.setInsets(Insets.ROOT_PANEL);

        WDynamicLabel categories_text = new WDynamicLabel(() -> I18n.translate("gui.feyfriends.categories_text", getCategoriesWithPlayers()));

        WLabel category_text = new WLabel(new TranslatableText("gui.feyfriends.category_text"));

        WTextField category_field = new WTextField(new TranslatableText("gui.feyfriends.category_field"));

        WButton addcategory_button = new WButton(new LiteralText("+"));
        addcategory_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (!FeyFriendsConfig.categories.containsKey(category_field_text)) {
                FeyFriendsConfig.categories.put(category_field_text, FeyFriendsConfig.genCategory(
                        FeyFriendsConfig.getNewCategoryY()));
                FeyFriendsConfig.write();
            }
        });

        WButton delcategory_button = new WButton(new LiteralText("-"));
        delcategory_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                FeyFriendsConfig.categories.remove(category_field_text);
                FeyFriendsConfig.write();
            }
        });

        WLabel comment_category = new WLabel(new TranslatableText("gui.feyfriends.comment_category"));

        WLabel changefriend_text = new WLabel(new TranslatableText("gui.feyfriends.changefriendtext"));

        WTextField changefriend_field = new WTextField(new TranslatableText("gui.feyfriends.changefriendfield"));

        WButton addfriend_button = new WButton(new LiteralText("+"));
        addfriend_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (!Objects.equals(category_field_text, "Online") && FeyFriendsConfig.categories.containsKey(category_field_text)) {
                List<String> friends = (List<String>) FeyFriendsConfig.categories.get(category_field_text).get("players");
                friends.add(changefriend_field.getText());
                FeyFriendsConfig.categories.get(category_field_text).put("players", friends);
                FeyFriendsConfig.write();
            }
        });

        WButton delfriend_button = new WButton(new LiteralText("-"));
        delfriend_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (!Objects.equals(category_field_text, "Online") && FeyFriendsConfig.categories.containsKey(category_field_text)) {
                List<String> friends = (List<String>) FeyFriendsConfig.categories.get(category_field_text).get("players");
                friends.remove(changefriend_field.getText());
                FeyFriendsConfig.categories.get(category_field_text).put("players", friends);
                FeyFriendsConfig.write();
            }
        });

        WLabel comment_changefriend = new WLabel(new TranslatableText("gui.feyfriends.comment_changefriend"));

        WToggleButton sound_notif_toggle = new WToggleButton(new TranslatableText("gui.feyfriends.sound_notif_toggle"));
        sound_notif_toggle.setOnToggle(on -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                FeyFriendsConfig.categories.get(category_field_text).put("sound_notif", on);
                FeyFriendsConfig.write();
            }
        });

        WLabel changesound_text = new WLabel(new TranslatableText("gui.feyfriends.changesound_text"));

        WLabeledSlider changesound_slider = new WLabeledSlider(1, 5);

        WButton changesound_button = new WButton(new LiteralText("✓"));
        changesound_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                FeyFriendsConfig.categories.get(category_field_text).put("sound", changesound_slider.getValue());
                FeyFriendsConfig.write();
                MinecraftClient.getInstance().world.playSound(
                        MinecraftClient.getInstance().player.getBlockPos(),
                        FeyFriendsConfig.getSoundFromInt(changesound_slider.getValue()),
                        SoundCategory.BLOCKS,
                        1f,
                        1f,
                        false
                );
            }
        });

        WLabel changeDelay_text = new WLabel(new TranslatableText("gui.feyfriends.changedelay_text"));

        WTextField changeDelay_field = new WTextField(new TranslatableText("gui.feyfriends.changedelay_field"));

        WButton changeDelay_button = new WButton(new LiteralText("✓"));
        changeDelay_button.setOnClick(() -> {
            try {
                int newDelay = Integer.parseInt(changeDelay_field.getText());
                FeyFriendsConfig.hudUpdateDelay = newDelay * 20;
                FeyFriendsConfig.write();
            }
            catch (NumberFormatException ignored) {}
        });

        WDynamicLabel current_delay = new WDynamicLabel(() -> I18n.translate("gui.feyfriends.current_delay",
                FeyFriendsConfig.hudUpdateDelay / 20));

        WToggleButton show_players_toggle = new WToggleButton(new TranslatableText("gui.feyfriends.show_players_toggle"));
        show_players_toggle.setOnToggle(on -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                FeyFriendsConfig.categories.get(category_field_text).put("show_players_list", on);
                FeyFriendsConfig.write();
            }
        });

        WLabel x_text = new WLabel(new TranslatableText("gui.feyfriends.x_text"));

        WTextField x_field = new WTextField(new TranslatableText("gui.feyfriends.x_field"));

        WButton change_x_button = new WButton(new LiteralText("✓"));
        change_x_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                try{
                    Double x = Double.valueOf(x_field.getText());
                    FeyFriendsConfig.categories.get(category_field_text).put("x", x);
                    FeyFriendsConfig.write();
                }
                catch (NumberFormatException ignored){
                }
            }
        });

        WDynamicLabel current_x = new WDynamicLabel(() -> I18n.translate("gui.feyfriends.current_x",
                getCoordOfCategoryOrDefault("x", category_field.getText(), "Error!")));

        WLabel y_text = new WLabel(new TranslatableText("gui.feyfriends.y_text"));

        WTextField y_field = new WTextField(new TranslatableText("gui.feyfriends.y_field"));

        WButton change_y_button = new WButton(new LiteralText("✓"));
        change_y_button.setOnClick(() -> {
            String category_field_text = category_field.getText();
            if (FeyFriendsConfig.categories.containsKey(category_field_text)) {
                try{
                    Double y = Double.valueOf(y_field.getText());
                    FeyFriendsConfig.categories.get(category_field_text).put("y", y);
                    FeyFriendsConfig.write();
                }
                catch (NumberFormatException ignored){
                }
            }
        });

        WDynamicLabel current_y = new WDynamicLabel(() -> I18n.translate("gui.feyfriends.current_y",
                getCoordOfCategoryOrDefault("y", category_field.getText(), "Error!")));

        WButton saveBackup_button = new WButton(new TranslatableText("gui.feyfriends.saveBackup_button"));
        saveBackup_button.setOnClick(() -> {
            Path backup = FabricLoader.getInstance().getConfigDir().resolve("feyfriends_backup.json");
            Path config = FabricLoader.getInstance().getConfigDir().resolve("feyfriends.json");
            try {
                Files.copy(config, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        WButton loadBackup_button = new WButton(new TranslatableText("gui.feyfriends.loadBackup_button"));
        loadBackup_button.setOnClick(() -> FeyFriendsClient.isReloadNeeded = true);

        root.add(categories_text, 0, 0, 5, 1);
        root.add(category_text, 0, 1, 5, 1);
        root.add(category_field, 6, 1, 5, 1);
        root.add(addcategory_button, 11, 1, 1, 1);
        root.add(delcategory_button, 12, 1, 1, 1);
        root.add(comment_category, 14, 1, 5, 1);
        root.add(changefriend_text, 0, 3, 5, 1);
        root.add(changefriend_field, 6, 3, 5, 1);
        root.add(addfriend_button, 11, 3, 1, 1);
        root.add(delfriend_button, 12, 3, 1, 1);
        root.add(comment_changefriend, 14, 3, 5, 1);
        root.add(sound_notif_toggle, 0, 5, 5, 1);
        root.add(changesound_text, 0, 6, 5, 1);
        root.add(changesound_slider, 6, 6, 5, 1);
        root.add(changesound_button, 11, 6, 1, 1);
        root.add(changeDelay_text, 0, 8, 5, 1);
        root.add(changeDelay_field, 8, 8, 5, 1);
        root.add(changeDelay_button, 13, 8, 1, 1);
        root.add(current_delay, 15, 8, 5, 1);
        root.add(show_players_toggle, 0, 9, 5, 1);
        root.add(x_text, 0, 11, 5, 1);
        root.add(x_field, 4, 11, 5, 1);
        root.add(change_x_button, 9, 11, 1, 1);
        root.add(current_x, 11, 11, 5, 1);
        root.add(y_text, 0, 12, 5, 1);
        root.add(y_field, 4, 12, 5, 1);
        root.add(change_y_button, 9, 12, 1, 1);
        root.add(current_y, 11, 12, 5, 1);
        root.add(saveBackup_button, 20, 11, 5, 1);
        root.add(loadBackup_button, 20, 12, 5, 1);

        root.validate(this);
    }

    public static String getCategoriesWithPlayers() {
        List<String> categories = new ArrayList<>();
        for (String category_name: FeyFriendsConfig.categories.keySet()) {
            if (!Objects.equals(category_name, "Online")) {
                List<String> players = (List<String>) FeyFriendsConfig.categories.get(category_name).get("players");
                if (players.size() > 0) {
                    categories.add(category_name + " (" + String.join(", ", players) + ")");
                }
                else {
                    categories.add(category_name);
                }
            }
            else {
                categories.add(category_name);
            }
        }
        return String.join(", ", categories);
    }

    protected static Object getCoordOfCategoryOrDefault(String coord_type, String category_name, Object default_value) {
        if (FeyFriendsConfig.categories.containsKey(category_name)) {
            if (Objects.equals(coord_type, "x")) {
                return FeyFriendsConfig.categories.get(category_name).get("x");
            }
            else if (Objects.equals(coord_type, "y")) {
                return FeyFriendsConfig.categories.get(category_name).get("y");
            }
        }
        return default_value;
    }

    private static void copyFiles(File source, File dest) throws IOException {
        Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}