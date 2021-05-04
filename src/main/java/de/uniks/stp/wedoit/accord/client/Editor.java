package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;

import java.util.Objects;

public class Editor {

    private LocalUser localUser;

    public LocalUser haveLocalUser() {
        localUser = new LocalUser();
        return localUser;
    }

    public LocalUser haveLocalUser(String username, String userkey) {
        if (localUser == null) {
            this.localUser = new LocalUser();
        }
        this.localUser.setName(username);
        this.localUser.setUserKey(userkey);
        return localUser;
    }

    public LocalUser getLocalUser(){
        return localUser;
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
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        if (localUser.getServers() != null) {
            for (Server server : localUser.getServers()) {
                if (server.getId().equals(id)) {
                    return server;
                }
            }
        }
        Server server = new Server().setId(id).setName(name);
        server.setLocalUser(localUser);
        return server;
    }


}
