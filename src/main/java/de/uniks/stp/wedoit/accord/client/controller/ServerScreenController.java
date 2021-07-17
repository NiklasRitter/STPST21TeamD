package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.AudioChannelSubViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private Label lbServerName, lblServerUsers, lbChannelName;
    private TextArea tfInputMessage;
    private ListView<User> lvServerUsers;

    // Websockets
    private WSCallback chatWSCallback;
    private WSCallback serverWSCallback;

    // PropertyChangeListener
    private final PropertyChangeListener userListViewListener = this::changeUserList;
    private final PropertyChangeListener serverNameListener = (propertyChangeEvent) -> this.handleServerNameChange();
    private final PropertyChangeListener audioChannelChange = this::handleAudioChannelChange;

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
        this.tfInputMessage = (TextArea) view.lookup("#tfInputMessage");
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
        this.tfInputMessage.setEditable(false);

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

        if (localUser.getAudioChannel() != null && localUser.getAudioChannel().getCategory().getServer().getId().equals(server.getId())) {
            initAudioChannelSubView(localUser.getAudioChannel());
        }

        // add PropertyChangeListener
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_AUDIO_CHANNEL, this.audioChannelChange);

        this.refreshStage();
    }


    private void setComponentsText() {
        this.lblServerUsers.setText(LanguageResolver.getString("SERVER_USERS"));
        this.lbChannelName.setText(LanguageResolver.getString("SELECT_A_CHANNEL"));
        this.tfInputMessage.setPromptText(LanguageResolver.getString("SELECT_A_CHANNEL"));
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
        if (!this.audioChannelSubViewContainer.getChildren().isEmpty()) {
            this.audioChannelSubViewContainer.getChildren().clear();
        }
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/subview/AudioChannelSubView.fxml")));

            audioChannelSubViewController = new AudioChannelSubViewController(localUser, view, editor, categoryTreeViewController, channel);
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

        if (audioChannelSubViewController != null) {
            this.audioChannelSubViewController.stop();
        }
    }

    // ActionEvent Methods

    /**
     * opens the AttentionLeaveServerScreen
     */
    private void leaveServerAttention(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.ATTENTION_LEAVE_SERVER_SCREEN, server, null);
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.MAIN_SCREEN, null, null);
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.EDIT_SERVER_SCREEN, server, null);
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
    private void handleServerNameChange() {
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
            Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, null, null));
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

    /**
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage() {
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).setOnCloseRequest(event -> {
            setComponentsText();
            initTooltips();
            editor.getStageManager().getStage(StageEnum.STAGE).setTitle(LanguageResolver.getString("SERVER"));
            lbServerName.setContextMenu(createContextMenuLeaveServer());
            serverChatController.initToolTip();
            serverChatController.addUserMessageContextMenu();
            serverChatController.addLocalUserMessageContextMenu();
            serverChatController.getLvTextChat().refresh();
            categoryTreeViewController.initContextMenu();
        });
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
}
