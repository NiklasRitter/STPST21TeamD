package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;

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
}
