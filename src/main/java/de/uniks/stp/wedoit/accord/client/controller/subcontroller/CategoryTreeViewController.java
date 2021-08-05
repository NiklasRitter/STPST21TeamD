package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.view.ChannelTreeView;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.AUDIO;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;

public class CategoryTreeViewController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private ServerScreenController controller;
    private final Map<String, Channel> channelMap = new HashMap<>();

    private TreeView<Object> tvServerChannels;
    private TreeItem<Object> tvServerChannelsRoot;
    private MenuItem addCategory;

    private PropertyChangeListener channelReadListener = this::handleChannelReadChange;
    private PropertyChangeListener categoriesListener = this::handleCategoryChange;
    private PropertyChangeListener channelListener = this::handleChannelChange;
    private PropertyChangeListener audioMemberListener = this::handleChannelAudioMemberChange;
    private PropertyChangeListener userListViewListener = this::changeUserList;
    private MenuButton serverMenuButton;

    public CategoryTreeViewController(Parent view, LocalUser model, Editor editor, Server server, ServerScreenController controller) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.controller = controller;
    }

    public void init() {
        this.tvServerChannels = (TreeView<Object>) view.lookup("#tvServerChannels");
        this.serverMenuButton = (MenuButton) view.lookup("#serverMenuButton");
        addServerMenu();
        this.tvServerChannelsRoot = new TreeItem<>();
        initContextMenu();

        this.tvServerChannels.setOnMouseReleased(this::tvServerChannelsOnDoubleClicked);
        initTvServerChannels();
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_CATEGORIES, this.categoriesListener);
    }

    /**
     * adds new menu items to the server menu button of the server screen
     */
    private void addServerMenu() {
        MenuItem createCategory = new MenuItem(LanguageResolver.getString("ADD_CATEGORY"));
        serverMenuButton.getItems().add(createCategory);
        createCategory.setStyle("-fx-font-size:12");
        createCategory.setOnAction((event) -> editor.getStageManager().initView(ControllerEnum.CREATE_CATEGORY_SCREEN, null, null));
    }

    public void initContextMenu() {
        ChannelTreeView channelTreeView = new ChannelTreeView(editor.getStageManager());
        this.tvServerChannels.setCellFactory(channelTreeView);
        this.tvServerChannels.setShowRoot(false);
        this.tvServerChannels.setRoot(tvServerChannelsRoot);
        this.tvServerChannels.setContextMenu(createContextMenuCategory());
    }

    private void initTvServerChannels() {
        for (Category category : server.getCategories()) {
            TreeItem<Object> categoryItem = new TreeItem<>(category);
            categoryItem.setExpanded(true);
            for (Channel channel : category.getChannels()) {
                addChannelToTreeView(channel, categoryItem);
            }
            tvServerChannelsRoot.getChildren().add(categoryItem);
        }
    }

    public void stop() {
        this.tvServerChannels.setOnMouseReleased(null);
        this.tvServerChannels = null;
        this.tvServerChannelsRoot = null;
        this.controller = null;
        for (Channel channel : channelMap.values()) {
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_READ, this.channelReadListener);
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, this.channelListener);
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, this.audioMemberListener);
        }
        for (Category category : server.getCategories()) {
            category.listeners().removePropertyChangeListener(Category.PROPERTY_NAME, this.categoriesListener);
            category.listeners().addPropertyChangeListener(Category.PROPERTY_CHANNELS, this.categoriesListener);
        }
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_CATEGORIES, this.categoriesListener);
        this.categoriesListener = null;
        this.channelListener = null;
        this.userListViewListener = null;
        this.audioMemberListener = null;
        this.channelReadListener = null;
        this.serverMenuButton = null;
        this.addCategory.setOnAction(null);
    }

    // Channel and Category init

    /**
     * initialize channel List view
     * gets Categories from server and calls loadCategoryChannels()
     */
    public void initCategoryChannelList() {
        editor.getRestManager().getCategories(localUser, server, this);
    }


    /**
     * handles the categories of a server in the view
     */
    public void handleGetCategories(List<Category> categoryList) {
        if (categoryList == null) {
            System.err.println("Error while loading categories from server");
            Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, true, null));
        }
    }

    /**
     * load the channels of a category
     *
     * @param category of which the channels should be loaded
     */
    private void loadCategoryChannels(Category category) {
        editor.getRestManager().getChannels(localUser, server, category, this);
    }


    /**
     * handles the channels of a server in the view
     */
    public void handleGetChannels(List<Channel> channelList) {
        if (channelList == null) {
            System.err.println("Error while loading channels from server");
            Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, true, null));
        }
    }

    /**
     * initChannelChat when channel is clicked twice
     *
     * @param mouseEvent occurs when a listItem is clicked
     */
    private void tvServerChannelsOnDoubleClicked(MouseEvent mouseEvent) {
        if (tvServerChannels.getSelectionModel().getSelectedItem() != null) {
            if (((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue() instanceof Channel) {
                Channel channel = (Channel) ((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue();
                if (mouseEvent.getClickCount() == 1 && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (channel.getType().equals(TEXT)) {
                        channel = (Channel) ((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue();
                        controller.getServerChatController().initChannelChat(channel);
                    }
                    controller.refreshLvUsers(channel);
                } else if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (channel.getType().equals(AUDIO)) {
                        handleAudioDoubleClicked(channel);
                    }
                }
            }
        }
    }

    /**
     * initAudioConnection when audio channel is clicked twice
     *
     * @param channel selected channel
     */
    public void handleAudioDoubleClicked(Channel channel) {
        if (localUser.getAudioChannel() == null) {
            editor.getRestManager().joinAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, this);
        } else if (localUser.getAudioChannel().getId().equals(channel.getId())) {
            editor.getRestManager().leaveAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, this);
        } else {
            editor.getRestManager().leaveAndJoinNewAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), localUser.getAudioChannel().getCategory(), channel.getCategory(), localUser.getAudioChannel(), channel, this);
        }
    }

    /**
     * Listen for changes in channel read for unread message markings.
     *
     * @param propertyChangeEvent The event of the property change.
     */
    private void handleChannelReadChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            Platform.runLater(() -> tvServerChannels.refresh());
        }
    }

    /**
     * rebuilds the user list
     */
    private void changeUserList(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            if (propertyChangeEvent.getSource() instanceof Channel) {
                Platform.runLater(() -> controller.refreshLvUsers((Channel) propertyChangeEvent.getSource()));
            }
        }
    }

    /**
     * handles a changed category
     */
    private void handleCategoryChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            if (propertyChangeEvent.getPropertyName().equals(Server.PROPERTY_CATEGORIES)) {
                updateCategoryTreeView((Category) propertyChangeEvent.getOldValue(), (Category) propertyChangeEvent.getNewValue());
            } else if (propertyChangeEvent.getPropertyName().equals(Category.PROPERTY_CHANNELS)) {
                updateChannelTreeView((Channel) propertyChangeEvent.getOldValue(), (Channel) propertyChangeEvent.getNewValue());
            }
            this.tvServerChannels.refresh();
        });
    }

    /**
     * handles a changed channel
     */
    private void handleChannelChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            if (propertyChangeEvent.getPropertyName().equals(Channel.PROPERTY_NAME)) {
                this.tvServerChannels.refresh();
            }
        });
    }

    /**
     * handles a change in the audio members of a channel
     */
    private void handleChannelAudioMemberChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            if (propertyChangeEvent.getSource() instanceof Channel) {
                updateAudioChannelMembers((Channel) propertyChangeEvent.getSource(), (User) propertyChangeEvent.getOldValue(), (User) propertyChangeEvent.getNewValue());
            }
        });
    }

    /**
     * creates a category tree item or deletes a old one
     *
     * @param oldValue added category item which should removed in the view
     * @param newValue which should be added to the view
     */
    private void updateCategoryTreeView(Category oldValue, Category newValue) {
        if (oldValue == null && newValue != null) {
            TreeItem<Object> categoryItem = new TreeItem<>(newValue);
            categoryItem.setExpanded(true);
            tvServerChannelsRoot.getChildren().add(categoryItem);
            loadCategoryChannels(newValue);
            newValue.listeners().addPropertyChangeListener(Category.PROPERTY_NAME, this.categoriesListener);
            newValue.listeners().addPropertyChangeListener(Category.PROPERTY_CHANNELS, this.categoriesListener);
        } else if (oldValue != null && newValue == null) {
            TreeItem<Object> categoryItem = getTreeItemCategory(oldValue);
            if (categoryItem != null) {
                tvServerChannelsRoot.getChildren().remove(categoryItem);
            }
        }
    }

    /**
     * creates a channel tree item or deletes a old one
     *
     * @param oldValue added channel item which should removed in the view
     * @param newValue which should be added to the view
     */
    private void updateChannelTreeView(Channel oldValue, Channel newValue) {
        if (oldValue == null && newValue != null) {
            TreeItem<Object> categoryItem = getTreeItemCategory(newValue.getCategory());
            if (categoryItem != null) {
                addChannelToTreeView(newValue, categoryItem);
            }
        } else if (oldValue != null && newValue == null) {
            for (TreeItem<Object> categoryItem : this.tvServerChannelsRoot.getChildren()) {
                for (TreeItem<Object> channelItem : categoryItem.getChildren()) {
                    Channel channel = (Channel) channelItem.getValue();
                    if (channel.getId().equals(oldValue.getId())) {
                        categoryItem.getChildren().remove(channelItem);
                        break;
                    }
                }
            }
            controller.resetLbChannelName();
        }
    }

    private void updateAudioChannelMembers(Channel channel, User oldValue, User newValue) {
        TreeItem<Object> channelItem = getTreeItemChannel(channel);
        if (channelItem != null) {
            if (oldValue == null && newValue != null) {
                channelItem.setExpanded(true);
                if (localUser.isAllMuted() && !newValue.getName().equals(localUser.getName())) {
                    editor.getAudioManager().muteUser(newValue);
                }
                addAudioMemberToTreeView(newValue, channelItem);
            } else if (oldValue != null && newValue == null) {
                channelItem.getChildren().removeIf(objectTreeItem -> objectTreeItem.getValue().equals(oldValue));
            }
        }
    }

    /**
     * adds a channel to the tree view and add property change listener
     *
     * @param channel      which should be added
     * @param categoryItem the item where the channel is child of
     */
    public void addChannelToTreeView(Channel channel, TreeItem<Object> categoryItem) {
        channelMap.put(channel.getId(), channel);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_READ, this.channelReadListener);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_NAME, this.channelListener);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, this.audioMemberListener);
        TreeItem<Object> channelItem = new TreeItem<>(channel);
        if (!channel.getAudioMembers().isEmpty()) {
            channelItem.setExpanded(true);
            addAudioMembersToTreeView(channel, channelItem);
        }
        for (TreeItem<Object> item : categoryItem.getChildren()) {
            Channel treeChannel = (Channel) item.getValue();
            if (treeChannel.getId().equals(channel.getId())) {
                return;
            }
        }
        categoryItem.getChildren().add(channelItem);
    }

    private void addAudioMembersToTreeView(Channel channel, TreeItem<Object> channelItem) {
        for (User user : channel.getAudioMembers()) {
            addAudioMemberToTreeView(user, channelItem);
        }
    }

    private void addAudioMemberToTreeView(User user, TreeItem<Object> channelItem) {
        TreeItem<Object> audioMemberItem = new TreeItem<>(user);
        channelItem.getChildren().add(audioMemberItem);
    }

    /**
     * @param category which is value of a certain tree item
     * @return the correct tree item for a given category
     */
    private TreeItem<Object> getTreeItemCategory(Category category) {
        for (TreeItem<Object> categoryItem : tvServerChannelsRoot.getChildren()) {
            Category currentCategory = (Category) categoryItem.getValue();
            if (category != null && currentCategory.getId().equals(category.getId())) {
                return categoryItem;
            }
        }
        return null;
    }

    /**
     * @param channel which is value of a certain tree item
     * @return the correct tree item for a given channel
     */
    private TreeItem<Object> getTreeItemChannel(Channel channel) {
        TreeItem<Object> categoryItem = getTreeItemCategory(channel.getCategory());
        if (categoryItem != null) {
            for (TreeItem<Object> channelItem : categoryItem.getChildren()) {
                Channel currentChannel = (Channel) channelItem.getValue();
                if (currentChannel.getId().equals(channel.getId())) {
                    return channelItem;
                }
            }
        }
        return null;
    }

    /**
     * create a context menu for category with the menu "add category"
     *
     * @return contextmenu
     */
    private ContextMenu createContextMenuCategory() {
        ContextMenu contextMenu = new ContextMenu();
        addCategory = new MenuItem("- " + LanguageResolver.getString("ADD_CATEGORY"));
        contextMenu.getItems().add(addCategory);
        addCategory.setOnAction((event) -> editor.getStageManager().initView(ControllerEnum.CREATE_CATEGORY_SCREEN, null, null));
        return contextMenu;
    }

    /**
     * @return channelMap which includes all channel with the id as key and the channel as value
     */
    public Map<String, Channel> getChannelMap() {
        return channelMap;
    }

    public void handleJoinAudioChannel(Channel channel) {
        if (channel.getCategory() != null) {
            loadCategoryChannels(channel.getCategory());
        } else {
            System.err.println("Join Problem");
        }
    }

    public void handleLeaveAudioChannel(Category category) {
        if (category == null) {
            System.err.println("Leave Problem");
        }
    }

    public TreeView<Object> getTvServerChannels() {
        return tvServerChannels;
    }

    public ServerScreenController getController() {
        return controller;
    }
}
