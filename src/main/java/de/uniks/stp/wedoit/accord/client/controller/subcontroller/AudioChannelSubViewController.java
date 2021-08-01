package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private ImageView imgViewMuteYourself;
    private ImageView imgViewUnMuteYourself;
    private ImageView imgViewAllMute;
    private ImageView imgViewAllUnMute;
    private PropertyChangeListener allMute = this::allMuteChanged;
    private PropertyChangeListener localUserMute = this::localUserMutedChanged;

    public AudioChannelSubViewController(LocalUser localUser, Parent view, Editor editor, CategoryTreeViewController controller, Channel channel) {
        this.localUser = localUser;
        this.view = view;
        this.editor = editor;
        this.controller = controller;
        this.channel = channel;
    }

    @Override
    public void init() {
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
        this.imgViewAllMute = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_SOUND_OFF))));
        this.imgViewAllMute.setFitHeight(20);
        this.imgViewAllMute.setFitWidth(20);
        this.imgViewAllUnMute = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_SOUND))));
        this.imgViewAllUnMute.setFitHeight(20);
        this.imgViewAllUnMute.setFitWidth(20);

        this.btnMuteYou.setOnAction(this::btnMuteYouOnClick);
        this.btnMuteAll.setOnAction(this::btnMuteAllOnClick);
        this.btnLeave.setOnAction(this::btnLeaveOnClick);

        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_ALL_MUTED, allMute);
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_MUTED, localUserMute);

        allMuteChanged(null);
        localUserMutedChanged(null);
    }

    private void btnMuteYouOnClick(ActionEvent actionEvent) {
        if (localUser.isMuted()) {
            if (localUser.isAllMuted()) {
                editor.getAudioManager().unMuteAllUsers(channel.getAudioMembers());
            }
            this.editor.getAudioManager().unmuteYourself(localUser);
            this.controller.getTvServerChannels().refresh();
        } else {
            this.editor.getAudioManager().muteYourself(localUser);
        }
    }

    private void btnMuteAllOnClick(ActionEvent actionEvent) {
        if (!localUser.isAllMuted()) {
            editor.getAudioManager().muteAllUsers(channel.getAudioMembers());
            this.editor.getAudioManager().muteYourself(localUser);
        } else {
            editor.getAudioManager().unMuteAllUsers(channel.getAudioMembers());
            this.editor.getAudioManager().unmuteYourself(localUser);
        }
        if (controller != null) {
            controller.getTvServerChannels().refresh();
        }
    }

    private void allMuteChanged(PropertyChangeEvent propertyChangeEvent) {
        if (localUser.isAllMuted()) {
            btnMuteAll.setGraphic(imgViewAllMute);
        } else {
            btnMuteAll.setGraphic(imgViewAllUnMute);
        }
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
        if (channel.getCategory() != null) {
            this.editor.getRestManager().leaveAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, controller);
        } else {
            this.editor.getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public void stop() {
        this.btnMuteYou.setOnAction(null);
        this.btnMuteAll.setOnAction(null);
        this.btnLeave.setOnAction(null);
        this.localUser.listeners().removePropertyChangeListener(allMute);
        this.localUser.listeners().removePropertyChangeListener(localUserMute);
        this.allMute = null;
        this.localUserMute = null;
    }
}
