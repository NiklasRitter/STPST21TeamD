package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

public class ChannelTreeView implements javafx.util.Callback<TreeView<Object>, TreeCell<Object>> {

    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ChannelTreeCell();
    }


    private static class ChannelTreeCell extends TreeCell<Object> {
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item instanceof Category) {
                    this.setText("#" + ((Category) item).getName());
                }
                if (item instanceof Channel) {
                    this.setText(((Channel) item).getName());
                }
            } else {
                this.setText(null);
            }
        }
    }
}
