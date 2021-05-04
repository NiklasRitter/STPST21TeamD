package de.uniks.stp.wedoit.accord.client.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class User {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ONLINE_STATUS = "onlineStatus";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CHANNELS = "channels";
    public static final String PROPERTY_SERVERS = "servers";
    public static final String PROPERTY_PRIVATE_CHAT = "privateChat";
    public static final String PROPERTY_LOCAL_USER = "localUser";
    protected PropertyChangeSupport listeners;
    private String name;
    private boolean onlineStatus;
    private String id;
    private List<Channel> channels;
    private List<Server> servers;
    private Chat privateChat;
    private LocalUser localUser;

    public String getName()
   {
      return this.name;
   }

    public User setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

    public boolean isOnlineStatus()
   {
      return this.onlineStatus;
   }

    public User setOnlineStatus(boolean value)
   {
      if (value == this.onlineStatus)
      {
         return this;
      }

      final boolean oldValue = this.onlineStatus;
      this.onlineStatus = value;
      this.firePropertyChange(PROPERTY_ONLINE_STATUS, oldValue, value);
      return this;
   }

    public String getId()
   {
      return this.id;
   }

    public User setId(String value)
   {
      if (Objects.equals(value, this.id))
      {
         return this;
      }

      final String oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_ID, oldValue, value);
      return this;
   }

    public List<Channel> getChannels()
   {
      return this.channels != null ? Collections.unmodifiableList(this.channels) : Collections.emptyList();
   }

    public User withChannels(Channel value)
   {
      if (this.channels == null)
      {
         this.channels = new ArrayList<>();
      }
      if (!this.channels.contains(value))
      {
         this.channels.add(value);
         value.withMembers(this);
         this.firePropertyChange(PROPERTY_CHANNELS, null, value);
      }
      return this;
   }

    public User withChannels(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withChannels(item);
      }
      return this;
   }

    public User withChannels(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withChannels(item);
      }
      return this;
   }

    public User withoutChannels(Channel value)
   {
      if (this.channels != null && this.channels.remove(value))
      {
         value.withoutMembers(this);
         this.firePropertyChange(PROPERTY_CHANNELS, value, null);
      }
      return this;
   }

    public User withoutChannels(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withoutChannels(item);
      }
      return this;
   }

    public User withoutChannels(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withoutChannels(item);
      }
      return this;
   }

    public List<Server> getServers()
   {
      return this.servers != null ? Collections.unmodifiableList(this.servers) : Collections.emptyList();
   }

    public User withServers(Server value)
   {
      if (this.servers == null)
      {
         this.servers = new ArrayList<>();
      }
      if (!this.servers.contains(value))
      {
         this.servers.add(value);
         value.withMembers(this);
         this.firePropertyChange(PROPERTY_SERVERS, null, value);
      }
      return this;
   }

    public User withServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withServers(item);
      }
      return this;
   }

    public User withServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withServers(item);
      }
      return this;
   }

    public User withoutServers(Server value)
   {
      if (this.servers != null && this.servers.remove(value))
      {
         value.withoutMembers(this);
         this.firePropertyChange(PROPERTY_SERVERS, value, null);
      }
      return this;
   }

    public User withoutServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutServers(item);
      }
      return this;
   }

    public User withoutServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutServers(item);
      }
      return this;
   }

    public Chat getPrivateChat()
   {
      return this.privateChat;
   }

    public User setPrivateChat(Chat value)
   {
      if (this.privateChat == value)
      {
         return this;
      }

      final Chat oldValue = this.privateChat;
      if (this.privateChat != null)
      {
         this.privateChat = null;
         oldValue.setUser(null);
      }
      this.privateChat = value;
      if (value != null)
      {
         value.setUser(this);
      }
      this.firePropertyChange(PROPERTY_PRIVATE_CHAT, oldValue, value);
      return this;
   }

    public LocalUser getLocalUser()
   {
      return this.localUser;
   }

    public User setLocalUser(LocalUser value)
   {
      if (this.localUser == value)
      {
         return this;
      }

      final LocalUser oldValue = this.localUser;
      if (this.localUser != null)
      {
         this.localUser = null;
         oldValue.withoutUsers(this);
      }
      this.localUser = value;
      if (value != null)
      {
         value.withUsers(this);
      }
      this.firePropertyChange(PROPERTY_LOCAL_USER, oldValue, value);
      return this;
   }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

    public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

    @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getId());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.withoutChannels(new ArrayList<>(this.getChannels()));
      this.withoutServers(new ArrayList<>(this.getServers()));
      this.setPrivateChat(null);
      this.setLocalUser(null);
   }
}
