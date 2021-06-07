package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.constants.JSON;
import de.uniks.stp.wedoit.accord.client.model.Invitation;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.COUNT;

public class InvitationListView implements javafx.util.Callback<ListView<Invitation>, ListCell<Invitation>> {

    @Override
    public ListCell<Invitation> call(ListView<Invitation> param) {
        return new InvitationListCell();
    }


    private static class InvitationListCell extends ListCell<Invitation> {
        protected void updateItem(Invitation item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item.getType().equals(COUNT)) {
                    this.setText(item.getCurrent() + "/" + item.getMax() + ": " + item.getLink());
                } else {
                    this.setText(item.getLink());
                }
            } else {
                this.setText(null);
            }
        }
    }

}
