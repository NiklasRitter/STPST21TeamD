package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.CreateChannelScreenController;
import de.uniks.stp.wedoit.accord.client.controller.EditChannelScreenController;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


/**
 * SubController for the member list of the Edit- and CreateChannelScreen
 */
public class AudioChannelSubViewController implements Controller {

    private final LocalUser localUser;
    private final Parent view;
    private final Controller controller;
    private final Channel channel;
    private Button btnMuteYou;
    private Button btnMuteAll;
    private Button btnLeave;

    public AudioChannelSubViewController(LocalUser localUser, Parent view, Controller controller, Channel channel) {
        this.localUser = localUser;
        this.view = view;
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

        this.btnMuteYou.setOnAction(this::btnMuteYouOnClick);
        this.btnMuteAll.setOnAction(this::btnMuteAllOnClick);
        this.btnLeave.setOnAction(this::btnLeaveOnClick);

        initAudioChannel();
    }

    private void btnMuteYouOnClick(ActionEvent actionEvent) {

    }

    private void btnMuteAllOnClick(ActionEvent actionEvent) {

    }

    private void btnLeaveOnClick(ActionEvent actionEvent) {

    }

    public void initAudioChannel() {

    }

    @Override
    public void stop() {
        this.btnMuteYou.setOnAction(null);
        this.btnMuteAll.setOnAction(null);
        this.btnLeave.setOnAction(null);
    }
}
