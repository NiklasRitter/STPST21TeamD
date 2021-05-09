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

    class Options {
        boolean darkmode;

        @Link("options")
        LocalUser localUser;
    }

    class User {
        String name;
        boolean onlineStatus;
        String id;

        @Link("users")
        LocalUser localUser;

        @Link("user")
        Chat privateChat;

        @Link("members")
        List<Server> servers;

        @Link("members")
        List<Channel> channels;
    }

    class LocalUser {
        String name;
        String userKey;

        @Link("localUser")
        List<User> users;

        @Link("localUser")
        List<Server> servers;

        @Link("localUser")
        Options options;
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

    }

    class Channel {
        String id;
        String name;
        String type;
        boolean privileged;

        @Link("channel")
        Category category;

        @Link("channels")
        List<User> members;

        @Link("channel")
        List<Message> messages;
    }

    class Category {
        String name;
        String id;

        @Link("category")
        Channel channel;

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

    class PrivateMessage {
        String id;
        long timestamp;
        String text;
        String from;
        String to;

        @Link("messages")
        Chat chat;
    }
}
