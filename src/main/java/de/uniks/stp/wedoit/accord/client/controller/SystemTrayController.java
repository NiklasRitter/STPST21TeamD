package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import javafx.application.Platform;

import java.awt.*;
import java.awt.event.ActionEvent;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;

public class SystemTrayController {
    private final Editor editor;
    private TrayIcon trayIcon;


    /**
     * Create a new Controller
     *
     * @param editor The editor of the Application
     */
    public SystemTrayController(Editor editor) {
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
        try {
            Image image = Toolkit.getDefaultToolkit().createImage(StageManager.class.getResource("view/images/LogoAccord.png"));
            trayIcon = new TrayIcon(image, "Accord");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(this::handleClickTrayIcon);
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception exception) {
            System.err.println("Error while initializing System Tray:");
            exception.printStackTrace();
        }
    }

    public void handleClickTrayIcon(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            this.editor.getStageManager().getStage().setIconified(false);
            this.editor.getStageManager().getStage().setAlwaysOnTop(true);
            this.editor.getStageManager().getStage().setAlwaysOnTop(false);
        });
    }

    public void stop() {
        SystemTray.getSystemTray().remove(trayIcon);
        trayIcon.removeActionListener(this::handleClickTrayIcon);
        trayIcon = null;
    }

    /**
     * Display notification for new private message
     *
     * @param message The new private message
     */
    public void displayPrivateMessageNotification(PrivateMessage message) {
        if(message.getText().startsWith(GAME_PREFIX)) message.setText(message.getText().substring(GAME_PREFIX.length()));
        trayIcon.displayMessage("New private message.", message.getFrom() + ": " + message.getText(), TrayIcon.MessageType.NONE);
    }

}
