package de.uniks.stp.wedoit.accord.client.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import java.util.List;

public class GenModel implements ClassModelDecorator {
    @Override
    public void decorate(ClassModelManager mm) {
        mm.haveNestedClasses(GenModel.class);
    }

    class AccordClient {
        @Link("accordClient")
        LocalUser localUser;

        @Link("accordClient")
        Options options;
    }

    class Options {
        boolean darkmode;
        boolean rememberMe;
        String language;

        @Link("options")
        AccordClient accordClient;
    }

    class LocalUser {
        String password;
        String name;
        String userKey;
        String id;
        List<User> gameInvites;
        List<User> gameRequests;
        Channel audioChannel;
        boolean muted;
        boolean allMuted;

        @Link("localUser")
        List<User> users;

        @Link("localUser")
        List<Server> servers;

        @Link("localUser")
        AccordClient accordClient;
    }

    class User {
        String name;
        boolean onlineStatus;
        boolean chatRead;
        String gameMove;

        String id;

        @Link("users")
        LocalUser localUser;

        @Link("user")
        Chat privateChat;

        @Link("members")
        List<Server> servers;

        @Link("members")
        List<Channel> channels;

        @Link("audioMembers")
        Channel audioChannel;
    }

    class Chat {
        String name;

        @Link("privateChat")
        User user;

        @Link("chat")
        List<PrivateMessage> messages;
    }

    class Server {
        String name;
        String id;
        String owner;

        @Link("servers")
        List<User> members;

        @Link("servers")
        LocalUser localUser;

        @Link("server")
        List<Category> categories;

        @Link("server")
        List<Invitation> invitations;
    }

    class Invitation {
        String id;
        String link;
        String type;
        int max;
        int current;

        @Link("invitations")
        Server server;
    }

    class Channel {
        String id;
        String name;
        String type;
        boolean privileged;
        boolean read;

        @Link("channels")
        Category category;

        @Link("channels")
        List<User> members;

        @Link("channel")
        List<Message> messages;

        @Link("audioChannel")
        List<User> audioMembers;
    }

    class Category {
        String name;
        String id;

        @Link("category")
        List<Channel> channels;

        @Link("categories")
        Server server;
    }

    class Message {
        String id;
        long timestamp;
        String text;
        String from;

        @Link("messages")
        Channel channel;
    }

    class PrivateMessage extends Message {
        String id;
        long timestamp;
        String text;
        String from;
        String to;

        @Link("messages")
        Chat chat;
    }
}
