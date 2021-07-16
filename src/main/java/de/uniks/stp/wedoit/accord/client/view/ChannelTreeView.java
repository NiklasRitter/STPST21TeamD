package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioReceive;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUP_STAGE;

public class ChannelTreeView implements javafx.util.Callback<TreeView<Object>, TreeCell<Object>> {

    private final StageManager stageManager;
    private final Controller controller;

    public ChannelTreeView(StageManager stageManager, Controller controller) {
        this.stageManager = stageManager;
        this.controller = controller;
    }

    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ChannelTreeCell();
    }

    private class ChannelTreeCell extends TreeCell<Object> {
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            this.getStyleClass().remove("newMessage");
            this.getStyleClass().remove("item-selected");
            if (!empty) {
                if (item instanceof Category) {
                    this.setText(((Category) item).getName());
                    this.setContextMenu(addContextMenuCategory((Category) item));
                }
                if (item instanceof Channel) {
                    Channel channel = (Channel) item;
                    handleChannel(channel);
                }
                if (item instanceof User) {
                    User user = (User) item;
                    handleUser(user);
                }
            } else {
                this.setText(null);
                this.setContextMenu(null);
                this.setGraphic(null);
            }
        }

        private void handleChannel(Channel channel) {
            ImageView icon;
            if (channel.getType().equals(TEXT)) {
                icon = addIconText();
            } else {
                icon = addIconAudio();
            }
            if (isSelected()) {
                this.getStyleClass().add("item-selected");
            }
            this.setGraphic(icon);
            this.setText(channel.getName());
            this.setContextMenu(addContextMenuChannel(channel));
            if (!channel.isRead()) {
                this.getStyleClass().add("newMessage");
            }
        }

        private void handleUser(User user) {
            this.setText(user.getName());
            if (stageManager.getEditor().getLocalUser().getAudioChannel() != null && stageManager.getEditor().getLocalUser().getAudioChannel().getId().equals(user.getAudioChannel().getId())) {
                if (!user.getId().equals(stageManager.getEditor().getLocalUser().getId())) {
                    if (user.isMuted()) {
                        this.setContextMenu(addContextMenuUnMute(user, this));
                        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound-off-red.png"))));
                        icon.setFitHeight(13);
                        icon.setFitWidth(13);
                        this.setGraphic(icon);
                    } else {
                        this.setContextMenu(addContextMenuMute(user, this));
                        this.setGraphic(null);
                    }
                } else {
                    this.setContextMenu(addContextMenuLocalUser(stageManager.getEditor().getLocalUser(), this));
                }
            }
        }

        private ImageView addIconText() {
            ImageView icon;
            if (!stageManager.getModel().getOptions().isDarkmode()) {
                icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/edit.png"))));
            } else {
                icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/edit_dark.png"))));
            }
            icon.setFitHeight(13);
            icon.setFitWidth(13);
            return icon;
        }

        private ImageView addIconAudio() {
            ImageView icon;
            if (!stageManager.getModel().getOptions().isDarkmode()) {
                icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound.png"))));
            } else {
                icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound_dark.png"))));
            }
            icon.setFitHeight(15);
            icon.setFitWidth(15);
            return icon;
        }
    }

    private ContextMenu addContextMenuChannel(Channel item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("ADD_CATEGORY"));
        MenuItem menuItem2 = new MenuItem("- " + LanguageResolver.getString("ADD_CHANNEL"));
        MenuItem menuItem3 = new MenuItem("- " + LanguageResolver.getString("EDIT_CHANNEL"));
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> this.stageManager.initView(ControllerEnum.CREATE_CATEGORY_SCREEN, null, null));
        menuItem2.setOnAction((event) -> this.stageManager.initView(ControllerEnum.CREATE_CHANNEL_SCREEN, item.getCategory(), null));
        menuItem3.setOnAction((event) -> this.stageManager.initView(ControllerEnum.EDIT_CHANNEL_SCREEN, item, null));

        return contextMenu;
    }

    private ContextMenu addContextMenuCategory(Category item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("ADD_CATEGORY"));
        MenuItem menuItem2 = new MenuItem("- " + LanguageResolver.getString("EDIT_CATEGORY"));
        MenuItem menuItem3 = new MenuItem("- " + LanguageResolver.getString("ADD_CHANNEL"));
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> this.stageManager.initView(ControllerEnum.CREATE_CATEGORY_SCREEN, null, null));
        menuItem2.setOnAction((event) -> this.stageManager.initView(ControllerEnum.EDIT_CATEGORY_SCREEN, item, null));
        menuItem3.setOnAction((event) -> this.stageManager.initView(ControllerEnum.CREATE_CHANNEL_SCREEN, item, null));

        return contextMenu;
    }

    public ContextMenu addContextMenuMute(User user, TreeCell<Object> cell) {
        if (!user.getName().equals(stageManager.getEditor().getLocalUser().getName())) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("- " + LanguageResolver.getString("MUTE"));
            menuItem.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().muteUser(user);
                cell.getTreeView().refresh();
            });
            contextMenu.getItems().add(menuItem);
            return contextMenu;
        }
        return null;
    }

    public ContextMenu addContextMenuUnMute(User user, TreeCell<Object> cell) {
        if (!user.getName().equals(stageManager.getEditor().getLocalUser().getName())) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("- " + LanguageResolver.getString("UNMUTE"));
            menuItem.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().unmuteUser(user);
                cell.getTreeView().refresh();
            });
            contextMenu.getItems().add(menuItem);
            return contextMenu;
        }
        return null;
    }

    public ContextMenu addContextMenuLocalUser(LocalUser localUser, TreeCell<Object> cell) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1;
        MenuItem menuItem2;
        MenuItem menuItem3;
        if (!localUser.isMuted()) {
            menuItem1 = new MenuItem("- " + LanguageResolver.getString("MUTE"));
            menuItem1.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().muteYourself(localUser);
                cell.getTreeView().refresh();
            });

        } else {
            menuItem1 = new MenuItem("- " + LanguageResolver.getString("UNMUTE"));
            menuItem1.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().unmuteYourself(localUser);
                cell.getTreeView().refresh();
            });
        }
        if (!localUser.isAllMuted()) {
            menuItem2 = new MenuItem("- " + LanguageResolver.getString("MUTE_ALL"));
            menuItem2.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().muteAllUsers(localUser.getAudioChannel().getAudioMembers());
                cell.getTreeView().refresh();
            });
        } else {
            menuItem2 = new MenuItem("- " + LanguageResolver.getString("UNMUTE_ALL"));
            menuItem2.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().unMuteAllUsers(localUser.getAudioChannel().getAudioMembers());
                cell.getTreeView().refresh();
            });
        }
        menuItem3 = new MenuItem("- " + LanguageResolver.getString("CLOSE_CONNECTION"));
        menuItem3.setOnAction((event) -> {
            Channel channel = localUser.getAudioChannel();
            this.stageManager.getEditor().getRestManager().leaveAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, (CategoryTreeViewController) controller);
            cell.getTreeView().refresh();
        });
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);
        return contextMenu;
    }

}
