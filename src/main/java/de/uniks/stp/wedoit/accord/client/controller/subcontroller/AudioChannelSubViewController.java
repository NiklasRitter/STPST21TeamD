package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Images.*;


/**
 * SubController for the member list of the Edit- and CreateChannelScreen
 */
public class AudioChannelSubViewController implements Controller {

    private final LocalUser localUser;
    private final Parent view;
    private final Editor editor;
    private final CategoryTreeViewController controller;
    private final Channel channel;
    private Button btnMuteYou;
    private Button btnMuteAll;
    private Button btnLeave;
    private Label lblVoiceChannel;
    private ImageView imgViewMuteYourself;
    private ImageView imgViewUnMuteYourself;
    private final PropertyChangeListener allMute = this::allMuteChanged;
    private final PropertyChangeListener localUserMute = this::localUserMutedChanged;

    public AudioChannelSubViewController(LocalUser localUser, Parent view, Editor editor, CategoryTreeViewController controller, Channel channel) {
        this.localUser = localUser;
        this.view = view;
        this.editor = editor;
        this.controller = controller;
        this.channel = channel;
    }

    @Override
    public void init() {
        this.lblVoiceChannel = (Label) this.view.lookup("#lblVoiceChannel");
        Label lblAudioChannelName = (Label) this.view.lookup("#lblAudioChannelName");
        Label lblUserName = (Label) this.view.lookup("#lblUserName");
        this.btnMuteYou = (Button) this.view.lookup("#btnMuteYou");
        this.btnMuteAll = (Button) this.view.lookup("#btnMuteAll");
        this.btnLeave = (Button) this.view.lookup("#btnLeave");

        lblAudioChannelName.setText(channel.getName());
        lblUserName.setText(localUser.getName());

        this.imgViewUnMuteYourself = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_MICRO))));
        this.imgViewUnMuteYourself.setFitHeight(25);
        this.imgViewUnMuteYourself.setFitWidth(25);
        this.imgViewMuteYourself = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_NO_MICRO))));
        this.imgViewMuteYourself.setFitHeight(25);
        this.imgViewMuteYourself.setFitWidth(25);

        this.btnMuteYou.setOnAction(this::btnMuteYouOnClick);
        this.btnMuteAll.setOnAction(this::btnMuteAllOnClick);
        this.btnLeave.setOnAction(this::btnLeaveOnClick);

        this.setComponentsText();

        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_ALL_MUTED, allMute);
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_MUTED, localUserMute);

        allMuteChanged(null);
        localUserMutedChanged(null);

        this.initTooltips();

        this.refreshStage();
    }

    private void setComponentsText() {
        this.lblVoiceChannel.setText(LanguageResolver.getString("VOICE_CHANNEL"));
    }

    /**
     * Initializes the Tooltips for the Buttons
     */
    private void initTooltips() {
        Tooltip toolTipBtnMuteYou = new Tooltip();
        toolTipBtnMuteYou.setText(LanguageResolver.getString("MUTE"));
        btnMuteYou.setTooltip(toolTipBtnMuteYou);

        Tooltip toolTipBtnMuteAll = new Tooltip();
        toolTipBtnMuteAll.setText(LanguageResolver.getString("MUTE_ALL"));
        btnMuteAll.setTooltip(toolTipBtnMuteAll);

        Tooltip toolTipBtnLeave = new Tooltip();
        toolTipBtnLeave.setText(LanguageResolver.getString("LEAVE"));
        toolTipBtnLeave.setStyle("-fx-font-size: 10");
        btnLeave.setTooltip(toolTipBtnLeave);
    }

    private void btnMuteYouOnClick(ActionEvent actionEvent) {
        if (localUser.isMuted()) {
            this.editor.getAudioManager().unmuteYourself(localUser);
        } else {
            this.editor.getAudioManager().muteYourself(localUser);
        }
    }

    private void btnMuteAllOnClick(ActionEvent actionEvent) {
        if (!localUser.isAllMuted()) {
            editor.getAudioManager().muteAllUsers(channel.getAudioMembers());
        } else {
            editor.getAudioManager().unMuteAllUsers(channel.getAudioMembers());
        }
        controller.getTvServerChannels().refresh();
    }

    private void allMuteChanged(PropertyChangeEvent propertyChangeEvent) {
        ImageView icon;
        if (localUser.isAllMuted()) {
            icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../../view/images/sound-off-red.png"))));
        } else {
            icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../../view/images/sound.png"))));
        }
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        btnMuteAll.setGraphic(icon);
    }

    private void localUserMutedChanged(PropertyChangeEvent propertyChangeEvent) {
        if (localUser.isMuted()) {
            Platform.runLater(() -> this.btnMuteYou.setGraphic(this.imgViewMuteYourself));
        } else {
            Platform.runLater(() -> this.btnMuteYou.setGraphic(this.imgViewUnMuteYourself));
        }
    }

    private void btnLeaveOnClick(ActionEvent actionEvent) {
        closeAudioChannel();
    }

    public void closeAudioChannel() {
        this.editor.getRestManager().leaveAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, controller);
    }

    /**
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage() {
        this.editor.getStageManager().getPopupStage().setOnCloseRequest(event -> {
            setComponentsText();
            initTooltips();
        });
    }

    @Override
    public void stop() {
        this.btnMuteYou.setOnAction(null);
        this.btnMuteAll.setOnAction(null);
        this.btnLeave.setOnAction(null);
        this.localUser.listeners().removePropertyChangeListener(allMute);
        this.localUser.listeners().removePropertyChangeListener(localUserMute);
    }
}
