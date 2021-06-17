package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.NetworkController;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;

public class JsonUtilTest {

    private JsonUtil jsonUtil;
    private RestClient restClient;
    private Editor editor;
    private User user;
    private LocalUser localUser;
    private Server server;

    @Before
    public void initJsonUtil() {
        jsonUtil = new JsonUtil();
        restClient = new RestClient();
        editor = new Editor();
        editor.haveAccordClient();
        editor.haveLocalUser();
        server = new Server();
        user = new User().setName("Gelareh").setId("021");
        localUser = new LocalUser().setName("Amir").setUserKey("testKey123").setId("1364");
    }

    @Test
    public void testParse() {
        String stringJson = "{\"data\":{\"name\":\"Iran\",\"id\":\"0098\"},\"message\":\"\",\"status\":\"success\"}";

        JsonObject createServerAnswer = JsonUtil.parse(stringJson);

        Assert.assertEquals(createServerAnswer.getString(MESSAGE), "");
        Assert.assertEquals(createServerAnswer.getString(STATUS), "success");
        Assert.assertEquals(createServerAnswer.getJsonObject(DATA).getString(NAME), "Iran");
        Assert.assertEquals(createServerAnswer.getJsonObject(DATA).getString(ID), "0098");
    }

    @Test
    public void testBuildLogin() {
        JsonObject loginData = JsonUtil.buildLogin(user.getName(), "1324567890");

        Assert.assertEquals(loginData.getString(NAME), "Gelareh");
        Assert.assertEquals(loginData.getString(PASSWORD), "1324567890");
    }
}
