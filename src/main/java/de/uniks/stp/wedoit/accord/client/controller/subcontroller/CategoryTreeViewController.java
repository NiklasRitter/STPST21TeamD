package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.view.ChannelTreeView;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryTreeViewController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final ServerScreenController controller;
    private final Map<String, Channel> channelMap = new HashMap<>();
    private Channel channel;

    private TreeView<Object> tvServerChannels;
    private TreeItem<Object> tvServerChannelsRoot;

    public final PropertyChangeListener userListViewListener = this::changeUserList;
    private final PropertyChangeListener channelReadListener = this::handleChannelReadChange;
    private final PropertyChangeListener categoriesListener = this::handleCategoryChange;
    private final PropertyChangeListener channelListener = this::handleChannelChange;

    public CategoryTreeViewController(Parent view, LocalUser model, Editor editor, Server server, ServerScreenController controller){
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.controller = controller;
        this.channel = new Channel();
    }

    public void init() {
        this.tvServerChannels = (TreeView<Object>) view.lookup("#tvServerChannels");

        this.tvServerChannelsRoot = new TreeItem<>();
        ChannelTreeView channelTreeView = new ChannelTreeView(editor.getStageManager());
        this.tvServerChannels.setCellFactory(channelTreeView);
        this.tvServerChannels.setShowRoot(false);
        this.tvServerChannels.setRoot(tvServerChannelsRoot);
        this.tvServerChannels.setContextMenu(createContextMenuCategory());

        this.tvServerChannels.setOnMouseReleased(this::tvServerChannelsOnDoubleClicked);

        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_CATEGORIES, this.categoriesListener);
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);
        this.channel.listeners().addPropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
    }

    public void stop() {
        this.tvServerChannels.setOnMouseReleased(null);
        for (Channel channel : channelMap.values()) {
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_READ, channelReadListener);
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, this.channelListener);
        }
        for(Category category : server.getCategories()){
            category.listeners().removePropertyChangeListener(Category.PROPERTY_NAME, categoriesListener);
            category.listeners().addPropertyChangeListener(Category.PROPERTY_CHANNELS, categoriesListener);
        }
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_CATEGORIES, this.categoriesListener);
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);
        this.channel.listeners().removePropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
    }

    // Channel and Category init
    /**
     * initialize channel List view
     * gets Categories from server and calls loadCategoryChannels()
     */
    public void initCategoryChannelList() {
        editor.getRestManager().getCategories(localUser, server, this);
    }

    public void handleGetCategories(List<Category> categoryList) {
        if (categoryList == null) {
            System.err.println("Error while loading categories from server");
            Platform.runLater(editor.getStageManager()::showLoginScreen);
        }
    }

    /**
     * load the channels of a category
     *
     * @param category of which the channels should be loaded
     */
    private void loadCategoryChannels(Category category, TreeItem<Object> categoryItem) {
        editor.getRestManager().getChannels(localUser, server, category, categoryItem, this);
    }

    public void handleGetChannels(List<Channel> channelList, TreeItem<Object> categoryItem) {
        if (channelList == null) {
            System.err.println("Error while loading channels from server");
            Platform.runLater(editor.getStageManager()::showLoginScreen);
        }
    }

    /**
     * initChannelChat when channel is clicked twice
     *
     * @param mouseEvent occurs when a listItem is clicked
     */
    private void tvServerChannelsOnDoubleClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

            if (tvServerChannels.getSelectionModel().getSelectedItem() != null) {
                if (((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue() instanceof Channel) {
                    channel = (Channel) ((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue();
                    controller.initChannelChat(channel);
                    Platform.runLater(() -> controller.refreshLvUsers(channel));
                }
            }
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

    private void changeUserList(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            if(propertyChangeEvent.getSource() instanceof Channel){
                Platform.runLater(() -> controller.refreshLvUsers((Channel) propertyChangeEvent.getSource()));
            }
            else{
                Platform.runLater(controller::refreshLvUsers);
            }
        }
    }

    private void handleCategoryChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            if(propertyChangeEvent.getPropertyName().equals(Server.PROPERTY_CATEGORIES)){
                updateCategoryTreeView((Category) propertyChangeEvent.getOldValue(), (Category) propertyChangeEvent.getNewValue());
            }
            else if(propertyChangeEvent.getPropertyName().equals(Category.PROPERTY_CHANNELS)){
                updateChannelTreeView((Channel) propertyChangeEvent.getOldValue(), (Channel) propertyChangeEvent.getNewValue());
            }
            this.tvServerChannels.refresh();
        });
    }

    private void handleChannelChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            if(propertyChangeEvent.getPropertyName().equals(Channel.PROPERTY_NAME)){
                this.tvServerChannels.refresh();
            }
        });
    }

    private void updateCategoryTreeView(Category oldValue, Category newValue){
        if(oldValue == null && newValue != null){
            TreeItem<Object> categoryItem = new TreeItem<>(newValue);
            categoryItem.setExpanded(true);
            tvServerChannelsRoot.getChildren().add(categoryItem);
            loadCategoryChannels(newValue, categoryItem);
            newValue.listeners().addPropertyChangeListener(Category.PROPERTY_NAME, categoriesListener);
            newValue.listeners().addPropertyChangeListener(Category.PROPERTY_CHANNELS, categoriesListener);
        }
        else if(oldValue != null && newValue == null){
            TreeItem<Object> categoryItem = getTreeItemCategory(oldValue);
            if(categoryItem != null){
                tvServerChannelsRoot.getChildren().remove(categoryItem);
            }
        }
    }

    private void updateChannelTreeView(Channel oldValue, Channel newValue){
        if(oldValue == null && newValue != null) {
            TreeItem<Object> categoryItem = getTreeItemCategory(newValue.getCategory());
            if (categoryItem != null) {
                addChannelToTreeView(newValue, categoryItem);
            }
        }
        else if(oldValue != null && newValue == null){
            for(TreeItem<Object> categoryItem : this.tvServerChannelsRoot.getChildren()){
                for(TreeItem<Object> channelItem : categoryItem.getChildren()){
                    Channel channel = (Channel) channelItem.getValue();
                    if(channel.getId().equals(oldValue.getId())){
                        categoryItem.getChildren().remove(channelItem);
                        break;
                    }
                }
            }
        }
    }

    public void addChannelToTreeView(Channel channel, TreeItem<Object> categoryItem){
        channelMap.put(channel.getId(), channel);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_READ, this.channelReadListener);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_NAME, this.channelListener);
        TreeItem<Object> channelItem = new TreeItem<>(channel);
        categoryItem.getChildren().add(channelItem);
    }

    private TreeItem<Object> getTreeItemCategory(Category category){
        for (TreeItem<Object> categoryItem : tvServerChannelsRoot.getChildren()) {
            Category currentCategory = (Category) categoryItem.getValue();
            if (currentCategory.getId().equals(category.getId())) {
                return categoryItem;
            }
        }
        return null;
    }

    private ContextMenu createContextMenuCategory() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addCategory = new MenuItem("- add category");
        contextMenu.getItems().add(addCategory);
        addCategory.setOnAction((event) -> editor.getStageManager().showCreateCategoryScreen());
        return contextMenu;
    }

    public Map<String, Channel> getChannelMap(){
        return channelMap;
    }
}
