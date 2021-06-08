package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Invitation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class InvitationListView implements javafx.util.Callback<ListView<Invitation>, ListCell<Invitation>> {

    @Override
    public ListCell<Invitation> call(ListView<Invitation> param) {
        return new InvitationListCell();
    }


    private static class InvitationListCell extends ListCell<Invitation> {
        protected void updateItem(Invitation item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                this.setText(item.getLink());
            } else {
                this.setText(null);
            }
        }
    }

}
