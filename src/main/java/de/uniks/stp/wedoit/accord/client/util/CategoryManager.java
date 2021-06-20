package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Server;

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.ID;

public class CategoryManager {

    /**
     * This method
     * <p>
     * - creates a category with the given arguments
     * <p>
     * - updates a category with the given name if the category has already been created
     *
     * @param id   id of the category
     * @param name name of the category
     * @return category with given id and name and with server server
     */
    public Category haveCategory(String id, String name, Server server) {

        for (Category category : server.getCategories()) {
            if (category.getId().equals(id)) {
                if(name != null){
                    category.setName(name);
                }
                return category;
            }
        }
        return new Category().setName(name).setId(id).setServer(server);
    }

    /**
     * This method gives the the server categories which are created with the data of the JSONArray
     * The categories dont have channels.
     *  @param server                  server which gets the categories
     * @param serversCategoryResponse server answer for categories of the server
     */
    public void haveCategories(Server server, JsonArray serversCategoryResponse) {
        Objects.requireNonNull(server);
        Objects.requireNonNull(serversCategoryResponse);

        List<String> categoryIds = new ArrayList<>();
        for (Category category : server.getCategories()) {
            categoryIds.add(category.getId());
        }
        for (int index = 0; index < serversCategoryResponse.toArray().length; index++) {
            if (!categoryIds.contains(serversCategoryResponse.getJsonObject(index).getString(ID))) {
                Category category = JsonUtil.parseCategory(serversCategoryResponse.getJsonObject(index));
                category.setServer(server);
            }
        }
        server.getCategories();
    }

}
