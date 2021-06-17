package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class MainScreenServerListView implements javafx.util.Callback<ListView<Server>, ListCell<Server>> {

    @Override
    public ListCell<Server> call(ListView<Server> param) {
        return new ServerListCell();
    }


    private class ServerListCell extends ListCell<Server> {
        protected void updateItem(Server item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                this.setText(item.getName());
            } else {
                this.setText(null);
            }
        }
    }

}
