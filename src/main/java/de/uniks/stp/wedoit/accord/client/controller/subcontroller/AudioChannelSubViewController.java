package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_CHOOSINGIMG;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_IMGURL;
import static de.uniks.stp.wedoit.accord.client.constants.Images.*;

import java.util.Objects;


/**
 * SubController for the member list of the Edit- and CreateChannelScreen
 */
public class AudioChannelSubViewController implements Controller {

    private final LocalUser localUser;
    private final Parent view;
    private final Editor editor;
    private final CategoryTreeViewController controller;
    private final Channel channel;
    private boolean allMuted;
    private Button btnMuteYou;
    private Button btnMuteAll;
    private Button btnLeave;
    private ImageView imgMuteYourself;
    private Label lblVoiceChannel;

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
        this.imgMuteYourself = (ImageView) view.lookup("#imgMuteYourself");

        lblAudioChannelName.setText(channel.getName());
        lblUserName.setText(localUser.getName());

        this.btnMuteYou.setOnAction(this::btnMuteYouOnClick);
        this.btnMuteAll.setOnAction(this::btnMuteAllOnClick);
        this.btnLeave.setOnAction(this::btnLeaveOnClick);

        this.setComponentsText();
    }

    private void setComponentsText() {
        this.lblVoiceChannel.setText(LanguageResolver.getString("VOICE_CHANNEL"));
    }

    private void btnMuteYouOnClick(ActionEvent actionEvent) {
        if (localUser.isMuted()) {
            this.editor.getAudioManager().unmuteYourself(localUser);

            ImageView imgMuteYourself = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_MICRO))));
            imgMuteYourself.setFitHeight(25);
            imgMuteYourself.setFitWidth(25);
            this.btnMuteYou.setGraphic(imgMuteYourself);
        } else {
            this.editor.getAudioManager().muteYourself(localUser);

            ImageView imgMuteYourself = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(IMAGES_PATH + IMAGE_NOMICRO))));
            imgMuteYourself.setFitHeight(25);
            imgMuteYourself.setFitWidth(25);
            this.btnMuteYou.setGraphic(imgMuteYourself);
        }
    }

    private void btnMuteAllOnClick(ActionEvent actionEvent) {
        if(!allMuted){
            editor.getAudioManager().muteAllUsers(channel.getAudioMembers());
            controller.getTvServerChannels().refresh();
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("../../view/images/sound-off-red.png"))));
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            btnMuteAll.setGraphic(icon);
            allMuted = true;
        }
    }

    private void btnLeaveOnClick(ActionEvent actionEvent) {
        closeAudioChannel();
    }

    public void closeAudioChannel() {
        this.editor.getRestManager().leaveAudioChannel(localUser.getUserKey(), channel.getCategory().getServer(), channel.getCategory(), channel, controller);
    }

    @Override
    public void stop() {
        this.btnMuteYou.setOnAction(null);
        this.btnMuteAll.setOnAction(null);
        this.btnLeave.setOnAction(null);
    }
}
