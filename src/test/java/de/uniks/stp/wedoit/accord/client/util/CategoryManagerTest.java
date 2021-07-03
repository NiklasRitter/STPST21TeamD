package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class CategoryManagerTest {

    private CategoryManager categoryManager;
    private Editor editor;
    private Server server;
    private Category category;
    private Channel channel;

    @Before
    public void CategoryManager() {
        categoryManager = new CategoryManager();
        editor = new Editor();
        server = new Server();
        category = new Category();
        channel = new Channel();
    }

    @Test
    public void testHaveCategory() {
        Assert.assertNotEquals(category.getName(), "Conversation");

        category = categoryManager.haveCategory("123", "Conversation", server);

        Assert.assertEquals(category.getName(), "Conversation");
    }

    @Test
    public void testHaveCategories() {
        JsonObject categoryJson = Json.createObjectBuilder()
                .add("server", "0098021")
                .add("channels", Json.createArrayBuilder()
                        .add("60ba1d40026b3534ca6021da").build())
                .add("name", "Accord")
                .add("id", "55700554").build();
        JsonArray categoryJsonArray = Json.createArrayBuilder().add(categoryJson).build();

        Assert.assertNotEquals(server.getCategories().size(), 1);

        categoryManager.haveCategories(server, categoryJsonArray);

        Assert.assertEquals(server.getCategories().size(), 1);
        Assert.assertEquals(server.getCategories().get(0).getName(), categoryJson.getString(NAME));
        Assert.assertEquals(server.getCategories().get(0).getId(), "55700554");
    }
}
