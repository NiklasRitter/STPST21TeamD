package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.ATTENTION_LEAVE_SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUP_STAGE;

public class MainScreenServerListView implements javafx.util.Callback<ListView<Server>, ListCell<Server>> {

    private Editor editor;

    public MainScreenServerListView(Editor editor) {
        this.editor = editor;
    }

    @Override
    public ListCell<Server> call(ListView<Server> param) {
        return new ServerListCell();
    }

    private class ServerListCell extends ListCell<Server> {
        protected void updateItem(Server item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                this.setText(item.getName());
                this.setContextMenu(addContextMenu(item));
            } else {
                this.setText(null);
            }
        }
    }

    public ContextMenu addContextMenu(Server item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("LEAVE_SERVER"));
        contextMenu.getItems().add(menuItem1);
        menuItem1.setOnAction((event) -> this.editor.getStageManager().initView(ControllerEnum.ATTENTION_LEAVE_SERVER_SCREEN, item, null));

        return contextMenu;
    }

}
