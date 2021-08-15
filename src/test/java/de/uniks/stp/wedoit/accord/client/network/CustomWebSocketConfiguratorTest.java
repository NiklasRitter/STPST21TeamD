package de.uniks.stp.wedoit.accord.client.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.USER_KEY;

public class CustomWebSocketConfiguratorTest {
    @Test
    public void testCustomWebSocketConfigurator() {
        String userKey = "userKey";
        CustomWebSocketConfigurator customWebSocketConfigurator = new CustomWebSocketConfigurator(userKey);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        customWebSocketConfigurator.beforeRequest(headers);
        Assert.assertEquals(headers.get(USER_KEY).get(0), userKey);
    }
}
