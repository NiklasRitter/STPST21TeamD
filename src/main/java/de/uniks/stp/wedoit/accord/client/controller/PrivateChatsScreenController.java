package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.PrivateChatController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.view.PrivateChatsScreenOnlineUsersCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.MAIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.OPTIONS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class PrivateChatsScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private Button btnOptions, btnPlay;
    private Button btnHome;
    private TextField tfPrivateChat;
    private ListView<User> lwOnlineUsers;
    private final PropertyChangeListener usersMessageListListener = this::usersMessageListViewChanged;
    //private final PropertyChangeListener usersChatReadListener = this::usersChatReadChanged;
    private ObservableList<User> onlineUserObservableList;
    private final PropertyChangeListener usersOnlineListListener = this::usersOnlineListViewChanged;
    private List<User> availableUsers = new ArrayList<>();
    private final PropertyChangeListener newUsersListener = this::newUser;
    private Label lblSelectedUser, lblOnlineUser;
    private final PrivateChatController privateChatController;
    private Boolean userIsSelected = false;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public PrivateChatsScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.privateChatController = new PrivateChatController(view, model, editor);
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
        this.btnPlay = (Button) view.lookup("#btnPlay");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.lblSelectedUser = (Label) view.lookup("#lblSelectedUser");
        this.lblOnlineUser = (Label) view.lookup("#lblOnlineUser");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");

        this.setComponentsText();

        privateChatController.init();

        this.btnHome.setOnAction(this::btnHomeOnClicked);

        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);

        this.initTooltips();
        this.initOnlineUsersList();

        this.btnPlay.setVisible(false);

        this.editor.getStageManager().getPopupStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setComponentsText();
                initTooltips();
                editor.getStageManager().getStage().setTitle(LanguageResolver.getString("PRIVATE_CHATS"));
                privateChatController.initToolTip();
                privateChatController.addMessageContextMenu();
            }
        });

    }

    private void setComponentsText() {
        this.lblOnlineUser.setText(LanguageResolver.getString("ONLINE_USERS"));
        this.lblSelectedUser.setText(LanguageResolver.getString("NO_USER_SELECTED"));
        this.btnPlay.setText(LanguageResolver.getString("PLAY"));
        if (userIsSelected) {
            this.tfPrivateChat.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"));
        } else {
            this.tfPrivateChat.setPromptText(LanguageResolver.getString("SELECT_A_USER"));
        }
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

        Tooltip playButton = new Tooltip();
        playButton.setText(LanguageResolver.getString("ROCK_PAPER_SCISSORS"));
        btnPlay.setTooltip(playButton);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersOnlineListListener);

        for (User user : availableUsers) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
        }
        this.btnHome.setOnAction(null);
        this.btnPlay.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.lwOnlineUsers.setOnMouseReleased(null);
    }

    /**
     * redirect to Main Screen
     *
     * @param actionEvent occurs when Home Button is clicked
     */
    private void btnHomeOnClicked(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null);
    }


    /**
     * redirect to Options Menu
     *
     * @param actionEvent occurs when Options Button is clicked
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("OPTIONS"), "OptionsScreen", OPTIONS_SCREEN_CONTROLLER, false, null, null);
    }

    /**
     * initialize onlineUsers list
     * <p>
     * Load online users from server and add them to the data model.
     * Set CellFactory and build lwOnlineUsers.
     */
    private void initOnlineUsersList() {
        // load online Users
        editor.getRestManager().getOnlineUsers(localUser, this);
    }

    /**
     * loads list view for the selected user and adds a listener for the chat
     */
    public void handleGetOnlineUsers() {
        // load list view
        PrivateChatsScreenOnlineUsersCellFactory usersListViewCellFactory = new PrivateChatsScreenOnlineUsersCellFactory();
        lwOnlineUsers.setCellFactory(usersListViewCellFactory);
        availableUsers = new ArrayList<>(localUser.getUsers());

        // Add listener for the loaded listView
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_USERS, this.newUsersListener);
        this.onlineUserObservableList = FXCollections.observableList(availableUsers.stream().filter(User::isOnlineStatus)
                .collect(Collectors.toList()));
        this.onlineUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());

        this.onlineUserObservableList.addAll(editor.loadOldChats());

        Platform.runLater(() -> this.lwOnlineUsers.setItems(onlineUserObservableList));

        for (User user : availableUsers) {
            editor.getUserChatRead(user);
            user.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().addPropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
        }
    }

    /**
     * update automatically the listView when goes offline or online
     *
     * @param propertyChangeEvent event occurs when a users online status changes
     */
    private void usersOnlineListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        TextField tfPrivateChat = privateChatController.getTfPrivateChat();
        User user = (User) propertyChangeEvent.getSource();
        editor.getUserChatRead(user);
        if (!user.isOnlineStatus()) {
            Platform.runLater(() -> {
                this.onlineUserObservableList.removeIf((e) -> e.getName().equals(user.getName()));
                if (editor.loadOldChats().stream().anyMatch((u) -> u.getName().equals(user.getName())))
                    this.onlineUserObservableList.add(user);
                this.onlineUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                        .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());
                if (privateChatController.getCurrentChat() != null && privateChatController.getCurrentChat().getUser() != null)
                    lwOnlineUsers.getSelectionModel().select(privateChatController.getCurrentChat().getUser());
                lwOnlineUsers.refresh();
                if (user.getName().equals(this.lblSelectedUser.getText())) {
                    tfPrivateChat.setPromptText(user.getName() + LanguageResolver.getString("IS_OFFLINE"));
                    tfPrivateChat.setEditable(false);
                }
            });
        } else {
            Platform.runLater(() -> {
                this.onlineUserObservableList.removeIf((e) -> e.getName().equals(user.getName()));
                this.onlineUserObservableList.add(user);
                this.onlineUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                        .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());
                if (privateChatController.getCurrentChat() != null && privateChatController.getCurrentChat().getUser() != null)
                    lwOnlineUsers.getSelectionModel().select(privateChatController.getCurrentChat().getUser());
                lwOnlineUsers.refresh();
                if (user.getName().equals(this.lblSelectedUser.getText())) {
                    tfPrivateChat.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"));
                    tfPrivateChat.setEditable(true);
                }
            });
        }
    }

    /**
     * Update listview when user gets new message.
     *
     * @param propertyChangeEvent event occurs when a user gets a new message
     */
    private void usersMessageListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        editor.updateUserChatRead((User) propertyChangeEvent.getSource());
        Platform.runLater(() -> lwOnlineUsers.refresh());
    }

    /**
     * update the listView automatically when a new user joined
     *
     * @param propertyChangeEvent event occurs when a user joined
     */
    private void newUser(PropertyChangeEvent propertyChangeEvent) {
        User oldSelect = lwOnlineUsers.getSelectionModel().getSelectedItem();
        if (propertyChangeEvent.getNewValue() != null) {
            User newUser = (User) propertyChangeEvent.getNewValue();
            newUser.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            newUser.listeners().addPropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
            this.availableUsers.add(newUser);
            if (newUser.getPrivateChat() == null) newUser.setChatRead(true);
            Platform.runLater(() -> {
                this.onlineUserObservableList.removeIf((e) -> e.getName().equals(newUser.getName()));
                this.onlineUserObservableList.add(newUser);
                this.onlineUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                        .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());
                if (oldSelect != null) lwOnlineUsers.getSelectionModel().select(oldSelect);
                lwOnlineUsers.refresh();
            });
        }
    }

    /**
     * initPrivateChat when item of userList is clicked twice
     * manages the the Play button
     *
     * @param mouseEvent occurs when a listitem is clicked
     */
    private void onOnlineUserListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            User selectedUser = lwOnlineUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                btnPlay.setText(localUser.getGameInvites().contains(selectedUser) ?
                        LanguageResolver.getString("ACCEPT") : LanguageResolver.getString("PLAY"));
                privateChatController.initPrivateChat(selectedUser);
                userIsSelected = true;
                lwOnlineUsers.refresh();
                this.lblSelectedUser.setText(privateChatController.getCurrentChat().getUser().getName());
                this.btnPlay.setVisible(true);
            }
        }
    }
}
