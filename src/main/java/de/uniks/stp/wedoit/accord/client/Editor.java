package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;

import java.util.Objects;

public class Editor {

    private LocalUser localUser;

    /**
     * create localUser without initialisation and set localUser in Editor
     *
     * @return localUser
     */
    public LocalUser haveLocalUser() {
        localUser = new LocalUser();
        return localUser;
    }

    /**
     * create localUser with the given arguments and set localUser in Editor
     *
     * if localUser already exists set username and userkey to current localUser
     *
     * @param username     id of the localUser
     * @param userkey      name of the localUser
     * @return localUser
     */
    public LocalUser haveLocalUser(String username, String userkey) {
        if (localUser == null) {
            this.localUser = new LocalUser();
        }
        this.localUser.setName(username);
        this.localUser.setUserKey(userkey);
        return localUser;
    }

    /**
     * return localUser
     *
     * @return localUser
     */
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

    /**
     * create a user with the given arguments and add to users of localUser
     *
     * @param id        id of the user
     * @param name      name of the user
     * @return localUser
     */
    public LocalUser haveUser(String id, String name) {
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);

        if (name.equals(localUser.getName())){
            return localUser;
        }

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    return localUser;
                }
            }
        }

        User user = new User().setId(id).setName(name);
        this.localUser.withUsers(user);
        return localUser;
    }

    /**
     * deletes a user with the given id
     *
     * @param id        id of the user
     * @return this
     */
    public Editor userLeft(String id){
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    localUser.withoutUsers(user);
                    return this;
                }
            }
        }

        return this;
    }
}
