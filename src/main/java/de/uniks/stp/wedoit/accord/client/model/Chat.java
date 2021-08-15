package de.uniks.stp.wedoit.accord.client.model;

import java.beans.PropertyChangeSupport;
import java.util.*;

public class Chat {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MESSAGES = "messages";
    public static final String PROPERTY_USER = "user";
    protected PropertyChangeSupport listeners;
    private String name;
    private List<PrivateMessage> messages;
    private User user;

    public String getName() {
        return this.name;
    }

    public Chat setName(String value) {
        if (Objects.equals(value, this.name)) {
            return this;
        }

        final String oldValue = this.name;
        this.name = value;
        this.firePropertyChange(PROPERTY_NAME, oldValue, value);
        return this;
    }

    public List<PrivateMessage> getMessages() {
        return this.messages != null ? Collections.unmodifiableList(this.messages) : Collections.emptyList();
    }

    public Chat withMessages(PrivateMessage value) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        if (!this.messages.contains(value)) {
            this.messages.add(value);
            value.setChat(this);
            this.firePropertyChange(PROPERTY_MESSAGES, null, value);
        }
        return this;
    }

    public Chat withMessages(PrivateMessage... value) {
        for (final PrivateMessage item : value) {
            this.withMessages(item);
        }
        return this;
    }

    public Chat withMessages(Collection<? extends PrivateMessage> value) {
        for (final PrivateMessage item : value) {
            this.withMessages(item);
        }
        return this;
    }

    public Chat withoutMessages(PrivateMessage value) {
        if (this.messages != null && this.messages.remove(value)) {
            value.setChat(null);
            this.firePropertyChange(PROPERTY_MESSAGES, value, null);
        }
        return this;
    }

    public Chat withoutMessages(PrivateMessage... value) {
        for (final PrivateMessage item : value) {
            this.withoutMessages(item);
        }
        return this;
    }

    public Chat withoutMessages(Collection<? extends PrivateMessage> value) {
        for (final PrivateMessage item : value) {
            this.withoutMessages(item);
        }
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public Chat setUser(User value) {
        if (this.user == value) {
            return this;
        }

        final User oldValue = this.user;
        if (this.user != null) {
            this.user = null;
            oldValue.setPrivateChat(null);
        }
        this.user = value;
        if (value != null) {
            value.setPrivateChat(this);
        }
        this.firePropertyChange(PROPERTY_USER, oldValue, value);
        return this;
    }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.listeners != null) {
            this.listeners.firePropertyChange(propertyName, oldValue, newValue);
            return true;
        }
        return false;
    }

    public PropertyChangeSupport listeners() {
        if (this.listeners == null) {
            this.listeners = new PropertyChangeSupport(this);
        }
        return this.listeners;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(' ').append(this.getName());
        return result.substring(1);
    }

    public void removeYou() {
        this.withoutMessages(new ArrayList<>(this.getMessages()));
        this.setUser(null);
    }
}
