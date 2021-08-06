package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.view.MainScreenServerListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.WS_SERVER_ID_URL;
import static de.uniks.stp.wedoit.accord.client.constants.Network.WS_SERVER_URL;

public class ServerListController implements Controller {

    private final Parent view;
    private final AccordClient model;
    private final Editor editor;
    private Button btnOptions;
    private Button btnHome;
    private Button addServerButton;
    private Button enterInvitationButton;
    private ListView<Server> lvServerList;
    private PropertyChangeListener serverListListener = this::serverListViewChanged;
    private WSCallback serverWSCallback = this::handleServersMessage;
    private final List<String> webSocketServerUrls = new ArrayList<>();

    public ServerListController(Parent view, AccordClient model, Editor editor) {
        this.view = view;
        this.model = model;
        this.editor = editor;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.enterInvitationButton = (Button) view.lookup("#btnEnterInvitation");
        this.lvServerList = (ListView<Server>) view.lookup("#lwServerList");

        // load servers of the localUser
        editor.getRestManager().getServers(editor.getLocalUser(), this);

        // load localUserId in order to later be able to edit server
        editor.getRestManager().getLocalUserId(editor.getLocalUser());


        this.btnOptions.setOnAction(this::optionsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.enterInvitationButton.setOnAction(this::enterInvitationButtonOnClick);
        this.lvServerList.setOnMouseReleased(this::onServerListViewClicked);

        if (!editor.getStageManager().getStage(StageEnum.STAGE).getTitle().equals("Private Chats")) {
            Platform.runLater(() -> {
                lvServerList.scrollTo(editor.getCurrentServer());
                lvServerList.getSelectionModel().select(editor.getCurrentServer());
            });

            btnHome.getStyleClass().remove("button-selected");
        }
        else {
            btnHome.getStyleClass().add("button-selected");
        }
    }

    public void stop() {
        this.btnOptions.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.addServerButton.setOnAction(null);
        this.enterInvitationButton.setOnAction(null);
        this.lvServerList.setOnMouseReleased(null);

        this.btnOptions = null;
        this.btnHome = null;
        this.addServerButton = null;
        this.enterInvitationButton = null;
        this.lvServerList = null;
    }

    /**
     * handles a response of a get servers request and inits the server list view.
     *
     * @param success success of the get servers request
     */
    public void handleGetServers(boolean success) {
        if (success) {
            // load list view
            MainScreenServerListView mainScreenServerListView = new MainScreenServerListView(this.editor);
            lvServerList.setCellFactory(mainScreenServerListView);
            List<Server> localUserServers = editor.getLocalUser().getServers().stream().sorted(Comparator.comparing(Server::getName)).collect(Collectors.toList());
            this.lvServerList.setItems(FXCollections.observableList(localUserServers));

            // Add listener for the loaded listView
            this.editor.getLocalUser().listeners().addPropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);
            // Add server websockets
            for (Server server : editor.getLocalUser().getServers()) {
                webSocketServerUrls.add(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
                editor.getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
            }
        } else {
            Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, true, null));
        }
    }

    /**
     * update automatically the listView when localUser.getServers changed
     *
     * @param propertyChangeEvent event which changed the Listener for the servers of the local user
     */
    private void serverListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            lvServerList.getItems().removeAll();
            List<Server> localUserServers = editor.getLocalUser().getServers().stream().sorted(Comparator.comparing(Server::getName))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.lvServerList.setItems(FXCollections.observableList(localUserServers)));
            lvServerList.refresh();
        }
    }

    /**
     * This method
     * <p>
     * handels a server message
     *
     * @param msg message from the web socket
     */
    private void handleServersMessage(JsonStructure msg) {
        JsonObject data = ((JsonObject) msg).getJsonObject(DATA);
        String action = ((JsonObject) msg).getString(ACTION);

        if (action.equals(SERVER_UPDATED)) {
            editor.haveServer(editor.getLocalUser(), data.getString(ID), data.getString(NAME));
            lvServerList.refresh();
        }
        if (action.equals(SERVER_DELETED)) {
            for (Server server : editor.getLocalUser().getServers()) {
                if (server.getId().equals(data.getString(ID))) {
                    Platform.runLater(() -> {
                        this.lvServerList.getItems().remove(server);
                        server.removeYou();
                        lvServerList.getItems().sort(Comparator.comparing(Server::getName));
                        lvServerList.refresh();
                    });
                }
            }
        }
    }

    /**
     * Redirect to the ServerScreen of the chosen server
     *
     * @param mouseEvent Expects an mouse event, such as when the listView is doubleclicked
     */
    private void onServerListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            Server server = lvServerList.getSelectionModel().getSelectedItem();
            if (server != null) {
                this.editor.getStageManager().initView(ControllerEnum.SERVER_SCREEN, server, null);
            }
        }
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        if (!editor.getStageManager().getStage(StageEnum.STAGE).getTitle().equals("Private Chats")) {
            this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null);
        }
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
     * Opens a pop-up windows, where you can enter the servername
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.CREATE_SERVER_SCREEN, null, null);
    }

    /**
     * Opens a pop-up windows, where you can enter a invitation
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void enterInvitationButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.JOIN_SERVER_SCREEN, null, null);
    }
}
