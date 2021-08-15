package de.uniks.stp.wedoit.accord.client.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class Category {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_CHANNELS = "channels";
    public static final String PROPERTY_SERVER = "server";
    protected PropertyChangeSupport listeners;
    private String name;
    private String id;
    private List<Channel> channels;
    private Server server;

    public String getName()
   {
      return this.name;
   }

    public Category setName(String value)
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

    public String getId()
   {
      return this.id;
   }

    public Category setId(String value)
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

    public Category withChannels(Channel value)
   {
      if (this.channels == null)
      {
         this.channels = new ArrayList<>();
      }
      if (!this.channels.contains(value))
      {
         this.channels.add(value);
         value.setCategory(this);
         this.firePropertyChange(PROPERTY_CHANNELS, null, value);
      }
      return this;
   }

    public Category withChannels(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withChannels(item);
      }
      return this;
   }

    public Category withChannels(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withChannels(item);
      }
      return this;
   }

    public Category withoutChannels(Channel value)
   {
      if (this.channels != null && this.channels.remove(value))
      {
         value.setCategory(null);
         this.firePropertyChange(PROPERTY_CHANNELS, value, null);
      }
      return this;
   }

    public Category withoutChannels(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withoutChannels(item);
      }
      return this;
   }

    public Category withoutChannels(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withoutChannels(item);
      }
      return this;
   }

    public Server getServer()
   {
      return this.server;
   }

    public Category setServer(Server value)
   {
      if (this.server == value)
      {
         return this;
      }

      final Server oldValue = this.server;
      if (this.server != null)
      {
         this.server = null;
         oldValue.withoutCategories(this);
      }
      this.server = value;
      if (value != null)
      {
         value.withCategories(this);
      }
      this.firePropertyChange(PROPERTY_SERVER, oldValue, value);
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
      this.setServer(null);
   }
}
