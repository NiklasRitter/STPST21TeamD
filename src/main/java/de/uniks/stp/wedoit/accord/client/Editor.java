package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;

public class Editor {

    public LocalUser haveLocalUser() {
        return new LocalUser();
    }

    /**
     * create a sever with the given arguments and with localUser as Member
     *
     * @param localUser localUser is member of the server with following id and name
     * @param id        id of the sever
     * @param name      name of the server
     * @return server with given id and name and with member localUser
     */
    public Server haveServer(LocalUser localUser, String id, String name) {
        if (localUser.getServers() != null) {
            for (Server server : localUser.getServers()) {
                if (server.getId() == id) {
                    return server;
                }
            }
        }
        Server server = new Server().setLocalUser(localUser).setId(id).setName(name);
        return server;
    }


}
