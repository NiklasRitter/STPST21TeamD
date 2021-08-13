package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.AudioChannelSubViewController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.PrivateChatController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerListController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.view.OnlineUsersCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PrivateChatsScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private ServerListController serverListController;
    private Button btnOptions, btnPlay;
    private Button btnHome;
    private RichTextArea taPrivateChat;
    private ListView<User> lwOnlineUsers;
    private ObservableList<User> onlineUserObservableList;
    private List<User> availableUsers = new ArrayList<>();
    private Label lblSelectedUser, lblOnlineUser;
    private PrivateChatController privateChatController;
    private User selectedUser;
    private VBox audioChannelSubViewContainer;
    private AudioChannelSubViewController audioChannelSubViewController;
    private PropertyChangeListener usersMessageListListener = this::usersMessageListViewChanged;
    private PropertyChangeListener usersOnlineListListener = this::usersOnlineListViewChanged;
    private PropertyChangeListener newUsersListener = this::newUser;
    private PropertyChangeListener audioChannelChange = this::handleAudioChannelChange;
    private PropertyChangeListener languageRefreshed = this::refreshStage;
    private Label lblDescription;
    private PropertyChangeListener usersDescriptionListener = this::userDescriptionChanged;

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
        this.serverListController = new ServerListController(view, editor.getStageManager().getModel(), editor, null);
    }


    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        this.btnPlay = (Button) view.lookup("#btnPlay");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.lblSelectedUser = (Label) view.lookup("#lblSelectedUser");
        this.lblOnlineUser = (Label) view.lookup("#lblOnlineUser");
        this.lblDescription = (Label) view.lookup("#lblDescription");
        HBox hBoxText = (HBox) view.lookup("#hBoxText");
        this.taPrivateChat = new RichTextArea();
        taPrivateChat.setId("tfEnterPrivateChat");
        taPrivateChat.getStyleClass().add("textAreaInput");
        taPrivateChat.updateTextColor(editor.getAccordClient().getOptions().isDarkmode());
        hBoxText.getChildren().add(0, taPrivateChat);


        this.audioChannelSubViewContainer = (VBox) view.lookup("#audioChannelSubViewContainer");
        this.audioChannelSubViewContainer.getChildren().clear();

        this.editor.getStageManager().getStage(StageEnum.STAGE).setTitle(LanguageResolver.getString("PRIVATE_CHATS"));

        this.privateChatController.init();
        this.serverListController.init();

        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);

        this.initOnlineUsersList();

        this.btnPlay.setVisible(false);

        if (localUser.getAudioChannel() != null) {
            initAudioChannelSubView(localUser.getAudioChannel());
        }

        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_AUDIO_CHANNEL, this.audioChannelChange);
        this.editor.getStageManager().getModel().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_LANGUAGE, this.languageRefreshed);

        this.editor.getStageManager().correctZoom();
    }


    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.newUsersListener);
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersOnlineListListener);
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_AUDIO_CHANNEL, this.audioChannelChange);
        this.editor.getStageManager().getModel().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_LANGUAGE, this.languageRefreshed);

        for (User user : availableUsers) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_DESCRIPTION, this.usersDescriptionListener);
        }
        this.usersMessageListListener = null;
        this.usersOnlineListListener = null;
        this.newUsersListener = null;
        this.audioChannelChange = null;
        this.languageRefreshed = null;

        this.btnPlay.setOnAction(null);
        this.lwOnlineUsers.setOnMouseReleased(null);

        privateChatController.stop();
        privateChatController = null;
        this.serverListController.stop();
        this.serverListController = null;
    }

    /**
     * initialize onlineUsers list
     * <p>
     * Load online users from server and add them to the data model.
     * Set CellFactory and build lwOnlineUsers.
     */
    private void initOnlineUsersList() {
        editor.getRestManager().getOnlineUsers(localUser, this);
    }

    /**
     * loads list view for the selected user and adds a listener for the chat
     */
    public void handleGetOnlineUsers() {
        // load list view
        OnlineUsersCellFactory usersListViewCellFactory = new OnlineUsersCellFactory(null, null);
        lwOnlineUsers.setCellFactory(usersListViewCellFactory);
        availableUsers = new ArrayList<>(localUser.getUsers());

        // Add listener for the loaded listView
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_USERS, this.newUsersListener);
        this.onlineUserObservableList = FXCollections.observableList(availableUsers.stream().filter(User::isOnlineStatus)
                .collect(Collectors.toList()));
        this.onlineUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());

        this.onlineUserObservableList.addAll(editor.loadOldChats());

        this.lwOnlineUsers.setItems(onlineUserObservableList);

        for (User user : availableUsers) {
            editor.getUserChatRead(user);
            user.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().addPropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
            user.listeners().addPropertyChangeListener(User.PROPERTY_DESCRIPTION, this.usersDescriptionListener);
        }
    }

    /**
     * update automatically the listView when goes offline or online
     *
     * @param propertyChangeEvent event occurs when a users online status changes
     */
    private void usersOnlineListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        RichTextArea tfPrivateChat = privateChatController.getTfPrivateChat();
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
                    tfPrivateChat.setPromptText(user.getName() + " " + LanguageResolver.getString("IS_OFFLINE"), editor.getAccordClient().getOptions().isDarkmode());
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
                    tfPrivateChat.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"), editor.getAccordClient().getOptions().isDarkmode());
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
     * @param mouseEvent occurs when a list item is clicked
     */
    private void onOnlineUserListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            this.selectedUser = lwOnlineUsers.getSelectionModel().getSelectedItem();
            initPrivateChatView(selectedUser);
        }
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

            audioChannelSubViewController = new AudioChannelSubViewController(localUser, view, editor, null, channel);
            audioChannelSubViewController.init();

            Platform.runLater(() -> this.audioChannelSubViewContainer.getChildren().add(view));
        } catch (Exception e) {
            e.printStackTrace();
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
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage(PropertyChangeEvent propertyChangeEvent) {
        this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null);
    }

    public void initPrivateChatView(User selectedUser) {
        lblDescription.setText("");
        if (selectedUser != null) {
            btnPlay.setText(localUser.getGameInvites().contains(selectedUser) ?
                    LanguageResolver.getString("ACCEPT") : LanguageResolver.getString("PLAY"));
            privateChatController.initPrivateChat(selectedUser);
            this.lblSelectedUser.setText(privateChatController.getCurrentChat().getUser().getName());
            if (selectedUser.getDescription() != null && !selectedUser.getDescription().equals("") && selectedUser.isOnlineStatus()) {
                lblDescription.setText("- " + Editor.parseUserDescription(selectedUser.getDescription()));
            }
            this.btnPlay.setVisible(true);
            this.editor.getStageManager().updateDarkmode();
        }
    }

    private void userDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        if (!lblDescription.getText().equals(selectedUser.getDescription())) {
            if (!selectedUser.getDescription().equals("") && selectedUser.getDescription() != null && selectedUser.isOnlineStatus())
                Platform.runLater(() -> lblDescription.setText("- " + Editor.parseUserDescription(selectedUser.getDescription())));
        }
    }

    public void setSelectedUser(User user) {
        this.selectedUser = user;
    }

    public ListView<User> getLwOnlineUsers() {
        return lwOnlineUsers;
    }

    public Button getBtnOptions() {
        return btnOptions;
    }

    public void setTfPrivateChatText(String text) {
        this.taPrivateChat.replaceText(text);
    }

    public PrivateChatController getPrivateChatController() {
        return privateChatController;
    }
}
