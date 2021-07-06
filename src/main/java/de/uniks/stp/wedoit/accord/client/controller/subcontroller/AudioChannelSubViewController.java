package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_CHOOSINGIMG;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_IMGURL;


/**
 * SubController for the member list of the Edit- and CreateChannelScreen
 */
public class AudioChannelSubViewController implements Controller {

    private final LocalUser localUser;
    private final Parent view;
    private final CategoryTreeViewController controller;
    private final Channel channel;
    private final Editor editor;
    private Button btnMuteYou;
    private Button btnMuteAll;
    private Button btnLeave;
    private ImageView imgMuteYourself;

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
        this.imgMuteYourself = (ImageView) view.lookup("#imgMuteYourself");

        lblAudioChannelName.setText(channel.getName());
        lblUserName.setText(localUser.getName());

        this.btnMuteYou.setOnAction(this::btnMuteYouOnClick);
        this.btnMuteAll.setOnAction(this::btnMuteAllOnClick);
        this.btnLeave.setOnAction(this::btnLeaveOnClick);
    }

    private void btnMuteYouOnClick(ActionEvent actionEvent) {
        if (localUser.isMuted()) {
            this.editor.getAudioManager().unmuteYourself(localUser);
            this.imgMuteYourself.setImage(new Image(String.valueOf(getClass().getResource("/de/uniks/stp/wedoit/accord/client/view/images/micro.png"))));
        } else {
            this.editor.getAudioManager().muteYourself(localUser);
            this.imgMuteYourself.setImage(new Image(String.valueOf(getClass().getResource("/de/uniks/stp/wedoit/accord/client/view/images/nomicro.png"))));
        }
    }

    private void btnMuteAllOnClick(ActionEvent actionEvent) {

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
