package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.AudioChannelSubViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.MemberListSubViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import javax.json.JsonArray;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class ServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;

    // View Elements
    private Button btnOptions;
    private Button btnHome;
    private Button btnEdit;
    private Label lbServerName, lblServerUsers, lbChannelName;
    private TextField tfInputMessage;
    private ListView<User> lvServerUsers;

    // Websockets
    private WSCallback chatWSCallback;
    private WSCallback serverWSCallback;

    // PropertyChangeListener
    private final PropertyChangeListener userListViewListener = this::changeUserList;
    private final PropertyChangeListener serverNameListener = (propertyChangeEvent) -> this.handleServerNameChange();

    private final CategoryTreeViewController categoryTreeViewController;
    private final ServerChatController serverChatController;
    private VBox audioChannelSubViewContainer;
    private AudioChannelSubViewController audioChannelSubViewController;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
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
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnEdit = (Button) view.lookup("#btnEdit");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lblServerUsers = (Label) view.lookup("#lblServerUsers");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");

        this.audioChannelSubViewContainer = (VBox) view.lookup("#audioChannelSubViewContainer");
        this.audioChannelSubViewContainer.getChildren().clear();

        categoryTreeViewController.init();
        serverChatController.init();

        this.setComponentsText();

        if (server.getName() != null && !server.getName().equals("")) {
            this.lbServerName.setText(server.getName());
        }

        // Add server websocket
        editor.getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
        // Add chat server web socket
        editor.getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.editor.getWebSocketManager().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWSCallback);

        this.lbServerName.setContextMenu(createContextMenuLeaveServer());
        this.btnEdit.setVisible(false);

        // get members of this server
        editor.getRestManager().getExplicitServerInformation(localUser, server, this);

        // add OnActionListeners
        addActionListener();

        initTooltips();

        // add PropertyChangeListener
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);

        this.editor.getStageManager().getPopupStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setComponentsText();
                initTooltips();
                editor.getStageManager().getStage().setTitle(LanguageResolver.getString("SERVER"));
                lbServerName.setContextMenu(createContextMenuLeaveServer());
                serverChatController.initToolTip();
                serverChatController.addMessageContextMenu();
                categoryTreeViewController.initContextMenu();
            }
        });
    }

    private void setComponentsText() {
        this.lblServerUsers.setText(LanguageResolver.getString("SERVER_USERS"));
        this.lbChannelName.setText(LanguageResolver.getString("SELECT_A_CHANNEL"));
        this.tfInputMessage.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"));
    }

    /**
     * adds action listener
     */
    public void addActionListener() {
        // Add action listeners
        this.btnOptions.setOnAction(this::optionsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.btnEdit.setOnAction(this::editButtonOnClick);
    }

    /**
     * Initializes the Tooltips for the Buttons
     */
    private void initTooltips() {
        Tooltip homeButton = new Tooltip();
        homeButton.setText(LanguageResolver.getString("HOME"));
        btnHome.setTooltip(homeButton);

        Tooltip optionsButton = new Tooltip();
        optionsButton.setText(LanguageResolver.getString("OPTIONS"));
        btnOptions.setTooltip(optionsButton);

        Tooltip editButton = new Tooltip();
        editButton.setText(LanguageResolver.getString("EDIT_SERVER"));
        editButton.setStyle("-fx-font-size: 10");
        btnEdit.setTooltip(editButton);
    }

    /**
     * If audio channel is clicked, then the audioChannelSubView is dynamically added to ServerScreen.
     * then calls AudioChannelSubViewController:
     * You can then take actions in an audio channel.
     */
    public void initAudioChannelSubView(Channel channel) {
        this.audioChannelSubViewContainer.getChildren().clear();
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/subview/AudioChannelSubView.fxml")));

            audioChannelSubViewController = new AudioChannelSubViewController(localUser, view, this, channel);
            audioChannelSubViewController.init();

            this.audioChannelSubViewContainer.getChildren().add(view);
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
        this.btnOptions.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnEdit.setOnAction(null);

        this.editor.getWebSocketManager().withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        this.editor.getWebSocketManager().withOutWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + this.server.getId());

        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);

        this.chatWSCallback = null;
        this.serverWSCallback = null;

        this.categoryTreeViewController.stop();
        this.serverChatController.stop();
        this.editor.setCurrentServer(null);
        deleteCurrentServer();

        if (audioChannelSubViewController != null) {
            this.audioChannelSubViewController.stop();
        }
    }

    // ActionEvent Methods

    /**
     * opens the AttentionLeaveServerScreen
     */
    private void leaveServerAttention(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("ATTENTION"), "AttentionLeaveServerScreen", ATTENTION_LEAVE_SERVER_SCREEN_CONTROLLER, false, server, null);
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null);
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("OPTIONS"), "OptionsScreen", OPTIONS_SCREEN_CONTROLLER, false, null, null);
    }

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("EDIT_SERVER"), "EditServerScreen", EDIT_SERVER_SCREEN_CONTROLLER, false, server, null);
    }

    // PropertyChangeEvent Methods

    /**
     * rebuilds the user list
     */
    private void changeUserList(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            Platform.runLater(() -> this.refreshLvUsers(null));
        }
    }

    /**
     * sets the name of a server in the server name label
     */
    private void handleServerNameChange() {
        Platform.runLater(() -> this.lbServerName.setText(this.server.getName()));
    }

    /**
     * deletes the current server with all edges
     */
    public void deleteCurrentServer() {
        // Delete all connection to the server in the data model
        for (Category category : server.getCategories()) {
            for (Channel channel : category.getChannels()) {
                channel.withoutMembers(new ArrayList<>(channel.getMembers()));
            }
        }
        server.withoutMembers(new ArrayList<>(server.getMembers()));
        localUser.withoutServers(server);
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
            Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("LOGIN"), "LoginScreen", LOGIN_SCREEN_CONTROLLER, false, null, null));
        }
        if (this.localUser.getId().equals(this.server.getOwner())) {
            this.lbServerName.getContextMenu().getItems().get(0).setVisible(false);
            this.btnEdit.setVisible(true);
        }

    }

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param members JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JsonArray members) {
        for (int index = 0; index < members.toArray().length; index++) {

            String name = members.getJsonObject(index).getString(NAME);
            String id = members.getJsonObject(index).getString(ID);
            boolean onlineStatus = members.getJsonObject(index).getBoolean(ONLINE);

            editor.haveUserWithServer(name, id, onlineStatus, server);
        }

        // load categories
        categoryTreeViewController.initCategoryChannelList();

        // load list view
        ServerUserListView serverUserListView = new ServerUserListView();
        lvServerUsers.setCellFactory(serverUserListView);
        this.refreshLvUsers(new Channel());
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_MEMBERS, this.userListViewListener);
    }

    // Helping Methods

    /**
     * creates a context menu to leave a server
     *
     * @return the created context menu
     */
    private ContextMenu createContextMenuLeaveServer() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemLeaveServer = new MenuItem(LanguageResolver.getString("LEAVE_SERVER"));
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

    public CategoryTreeViewController getCategoryTreeViewController() {
        return categoryTreeViewController;
    }

    public ServerChatController getServerChatController() {
        return serverChatController;
    }

    public VBox getAudioChannelSubViewContainer() {
        return audioChannelSubViewContainer;
    }
}
