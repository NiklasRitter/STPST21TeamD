package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.AudioChannelSubViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerListController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.OnlineUsersCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.json.JsonArray;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class ServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final ServerListController serverListController;

    // View Elements
    private Label lbServerName, lblServerUsers, lbChannelName;

    private ListView<User> lvServerUsers;
    private MenuItem menuItemLeaveServer;

    // Websockets
    private WSCallback chatWSCallback;
    private WSCallback serverWSCallback;

    // PropertyChangeListener
    private PropertyChangeListener userListViewListener = this::changeUserList;
    private PropertyChangeListener serverNameListener = this::handleServerNameChange;
    private PropertyChangeListener audioChannelChange = this::handleAudioChannelChange;
    private PropertyChangeListener languageRefreshed = this::refreshStage;

    private CategoryTreeViewController categoryTreeViewController;
    private ServerChatController serverChatController;
    private VBox audioChannelSubViewContainer;
    private AudioChannelSubViewController audioChannelSubViewController;
    private PropertyChangeListener userDescriptionListener = this::userDescriptionChanged;
    private MenuButton serverMenuButton;


    /**
     * Create a new Controller
     *  @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public ServerScreenController(Parent view, LocalUser model, Editor editor, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        categoryTreeViewController = new CategoryTreeViewController(view, model, editor, server, this);
        serverChatController = new ServerChatController(view, model, editor, server, this);
        this.serverWSCallback = (msg) -> editor.getWebSocketManager().handleServerMessage(msg, server);
        this.chatWSCallback = serverChatController::handleChatMessage;
        this.serverListController = new ServerListController(view, editor.getStageManager().getModel(), editor);
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     * Add necessary webSocketClients
     */
    public void init() {
        // Load all view references
        this.editor.setCurrentServer(server);

        TextArea tfInputMessage = (TextArea) view.lookup("#tfInputMessage");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");
        this.serverMenuButton = (MenuButton) view.lookup("#serverMenuButton");

        this.audioChannelSubViewContainer = (VBox) view.lookup("#audioChannelSubViewContainer");
        this.audioChannelSubViewContainer.getChildren().clear();

        this.categoryTreeViewController.init();
        this.serverChatController.init();
        this.serverListController.init();


        //this.setComponentsText();

        if (server.getName() != null && !server.getName().equals("")) {
            this.lbServerName.setText(server.getName());
        }
        tfInputMessage.setEditable(false);

        // Add server websocket
        editor.getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
        // Add chat server web socket
        editor.getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.editor.getWebSocketManager().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWSCallback);

        this.lbServerName.setContextMenu(createContextMenuLeaveServer());

        // get members of this server
        editor.getRestManager().getExplicitServerInformation(localUser, server, this);

        if (localUser.getAudioChannel() != null) {
            initAudioChannelSubView(localUser.getAudioChannel());
        }

        // add PropertyChangeListener
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_AUDIO_CHANNEL, this.audioChannelChange);
        this.editor.getStageManager().getModel().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_LANGUAGE, this.languageRefreshed);
        this.localUser.getAccordClient().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, this::onDarkmodeChanged);

    }

    private void onDarkmodeChanged(PropertyChangeEvent propertyChangeEvent) {
        lvServerUsers.refresh();
    }


    /**
     * If audio channel is clicked, then the audioChannelSubView is dynamically added to ServerScreen.
     * then calls AudioChannelSubViewController:
     * You can then take actions in an audio channel.
     */
    public void initAudioChannelSubView(Channel channel) {
        if (!this.audioChannelSubViewContainer.getChildren().isEmpty()) {
            this.audioChannelSubViewContainer.getChildren().clear();
        }
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/subview/AudioChannelSubView.fxml")), LanguageResolver.getLanguage());
            if (channel.getCategory().getServer() == server) {
                audioChannelSubViewController = new AudioChannelSubViewController(localUser, view, editor, categoryTreeViewController, channel);
            } else {
                audioChannelSubViewController = new AudioChannelSubViewController(localUser, view, editor, null, channel);
            }
            audioChannelSubViewController.init();

            Platform.runLater(() -> this.audioChannelSubViewContainer.getChildren().add(view));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {

        this.localUser.getAccordClient().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_DARKMODE, this::onDarkmodeChanged);
        for (User user : server.getMembers()) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_DESCRIPTION, this.userDescriptionListener);
        }
        for (MenuItem i: serverMenuButton.getItems()) {
            i.setOnAction(null);
        }
        this.serverMenuButton = null;
        this.menuItemLeaveServer.setOnAction(null);

        this.editor.getWebSocketManager().withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        this.editor.getWebSocketManager().withOutWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + this.server.getId());

        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_AUDIO_CHANNEL, this.audioChannelChange);
        this.editor.getStageManager().getModel().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_LANGUAGE, this.languageRefreshed);
        this.serverNameListener = null;
        this.userListViewListener = null;
        this.languageRefreshed = null;
        this.audioChannelChange = null;
        this.chatWSCallback = null;
        this.serverWSCallback = null;

        this.categoryTreeViewController.stop();
        this.serverChatController.stop();
        this.categoryTreeViewController = null;
        this.serverChatController = null;
        this.editor.setCurrentServer(null);

        if (audioChannelSubViewController != null) {
            this.audioChannelSubViewController.stop();
        }
        this.audioChannelSubViewContainer = null;
    }

    // ActionEvent Methods

    /**
     * opens the AttentionLeaveServerScreen
     */
    private void leaveServerAttention(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.ATTENTION_LEAVE_SERVER_SCREEN, server, null);
    }

    // PropertyChangeEvent Methods

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.EDIT_SERVER_SCREEN, server, null);
    }

    /**
     * rebuilds the user list
     */
    private void changeUserList(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            Platform.runLater(() -> this.refreshLvUsers(null));
        }
    }

    private void handleAudioChannelChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() == null) {
            this.audioChannelSubViewController.stop();
            this.audioChannelSubViewController = null;
            Platform.runLater(() -> this.audioChannelSubViewContainer.getChildren().clear());
        } else {
            this.initAudioChannelSubView((Channel) propertyChangeEvent.getNewValue());
        }
    }

    /**
     * sets the name of a server in the server name label
     */
    private void handleServerNameChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> this.lbServerName.setText(this.server.getName()));
    }

    /**
     * handles the explicit server information in the view
     */
    public void handleGetExplicitServerInformation(JsonArray members) {
        if (members != null) {
            // create users which are member in the server and load user list view
            Platform.runLater(() -> lbServerName.setText(server.getName()));

            createUserListView(members);
        } else {
            Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, true, null));
        }
        if (this.localUser.getId().equals(this.server.getOwner())) {
            this.lbServerName.getContextMenu().getItems().get(0).setVisible(false);
            addServerMenu(true);
        } else {
            addServerMenu(false);
        }

    }

    /**
     * adds new menu items depending on the owner to the server menu button of the server screen
     */
    private void addServerMenu(boolean isOwner) {
        if (isOwner) {
            MenuItem serverSettings = new MenuItem(LanguageResolver.getString("SERVER_SETTINGS"));
            serverSettings.setStyle("-fx-font-size:12");
            serverMenuButton.getItems().add(0, serverSettings);
            serverSettings.setOnAction(this::editButtonOnClick);
        } else {
            MenuItem leaverServer = new MenuItem(LanguageResolver.getString("LEAVE_SERVER"));
            leaverServer.setStyle("-fx-font-size:12");
            serverMenuButton.getItems().add(0, leaverServer);
            leaverServer.setOnAction(this::leaveServerAttention);
        }
    }

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param jsonMembers JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JsonArray jsonMembers) {
        List<User> members = JsonUtil.parseUserArray(jsonMembers);
        editor.serverWithMembers(members, server);
        // load categories
        categoryTreeViewController.initCategoryChannelList();

        // load list view
        lvServerUsers.setCellFactory(new OnlineUsersCellFactory(this.editor.getStageManager(), this.server));
        this.refreshLvUsers(new Channel());
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);
        for (User user : server.getMembers()) {
            user.listeners().addPropertyChangeListener(User.PROPERTY_DESCRIPTION, this.userDescriptionListener);
        }
    }

    // Helping Methods

    /**
     * creates a context menu to leave a server
     *
     * @return the created context menu
     */
    private ContextMenu createContextMenuLeaveServer() {
        ContextMenu contextMenu = new ContextMenu();
        menuItemLeaveServer = new MenuItem(LanguageResolver.getString("LEAVE_SERVER"));
        contextMenu.getItems().add(menuItemLeaveServer);
        menuItemLeaveServer.setOnAction(this::leaveServerAttention);
        return contextMenu;
    }

    /**
     * Updates ServerListView (list of displayed members).
     * Queries whether a channel is privileged:
     * If yes, it shows only members assigned to this channel.
     * If no, it shows all users of the server
     */
    public void refreshLvUsers(Channel channel) {
        List<User> users;
        if (channel != null) {
            if (channel.isPrivileged()) {
                users = channel.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus)).collect(Collectors.toList());
            } else {
                users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus)).collect(Collectors.toList());
            }
        } else {
            users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus)).collect(Collectors.toList());
        }
        Collections.reverse(users);
        this.lvServerUsers.getItems().removeAll();
        this.lvServerUsers.setItems(FXCollections.observableList(users));
        this.lvServerUsers.refresh();
    }

    /**
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage(PropertyChangeEvent propertyChangeEvent) {
        this.editor.getStageManager().initView(ControllerEnum.SERVER_SCREEN, this.server, null);
    }

    public CategoryTreeViewController getCategoryTreeViewController() {
        return categoryTreeViewController;
    }

    public ServerChatController getServerChatController() {
        return serverChatController;
    }

    public void resetLbChannelName() {
        this.lbChannelName.setText(LanguageResolver.getString("SELECT_A_CHANNEL"));
    }

    private void userDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        this.lvServerUsers.refresh();
    }
}
