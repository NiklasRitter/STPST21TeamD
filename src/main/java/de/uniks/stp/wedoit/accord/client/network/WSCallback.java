package de.uniks.stp.wedoit.accord.client.network;

import javax.json.JsonStructure;

public interface WSCallback {
    void handleMessage(JsonStructure msg);
}
