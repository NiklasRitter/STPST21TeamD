package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;

import javax.json.JsonArray;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class ServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;

    // View Elements
    private Button btnOptions;
    private Button btnHome;
    private Button btnEdit;
    private Label lbServerName;
    private ListView<User> lvServerUsers;

    // Websockets
    private WSCallback chatWSCallback;
    private WSCallback serverWSCallback;

    // PropertyChangeListener
    private final PropertyChangeListener serverNameListener = (propertyChangeEvent) -> this.handleServerNameChange();

    private final CategoryTreeViewController categoryTreeViewController;
    private final ServerChatController serverChatController;

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
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnEdit = (Button) view.lookup("#btnEdit");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");

        categoryTreeViewController.init();
        serverChatController.init();

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
        homeButton.setText("home");
        btnHome.setTooltip(homeButton);

        Tooltip optionsButton = new Tooltip();
        optionsButton.setText("options");
        btnOptions.setTooltip(optionsButton);

        Tooltip editButton = new Tooltip();
        editButton.setText("edit Server");
        btnEdit.setTooltip(editButton);
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

        server.listeners().removePropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);

        this.chatWSCallback = null;
        this.serverWSCallback = null;

        categoryTreeViewController.stop();
        serverChatController.stop();
        editor.setCurrentServer(null);
        deleteCurrentServer();
    }

    // ActionEvent Methods

    /**
     * opens the AttentionLeaveServerScreen
     */
    private void leaveServerAttention(ActionEvent actionEvent) {
        this.editor.getStageManager().showAttentionLeaveServerScreen(this.server);
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().showMainScreen();
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().showOptionsScreen();
    }

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().showEditServerScreen(this.server);
    }

    // PropertyChangeEvent Methods

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
            Platform.runLater(() -> this.editor.getStageManager().showLoginScreen());
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
        //Platform.runLater(() -> this.refreshLvUsers(new Channel()));
    }

    // Helping Methods

    /**
     * creates a context menu to leave a server
     *
     * @return the created context menu
     */
    private ContextMenu createContextMenuLeaveServer() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemLeaveServer = new MenuItem("Leave Server");
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
    public synchronized void refreshLvUsers(Channel channel) {
        List<User> users = null;
        if (channel != null) {
            if (channel.isPrivileged()) {
                users = channel.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus)).collect(Collectors.toList());
            } else {
                users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus)).collect(Collectors.toList());
            }
        }
        if (users != null) {
            Collections.reverse(users);
            this.lvServerUsers.getItems().removeAll();
            this.lvServerUsers.setItems(FXCollections.observableList(users));
            this.lvServerUsers.refresh();
        }
    }

    public CategoryTreeViewController getCategoryTreeViewController() {
        return categoryTreeViewController;
    }

    public ServerChatController getServerChatController() {
        return serverChatController;
    }
}
