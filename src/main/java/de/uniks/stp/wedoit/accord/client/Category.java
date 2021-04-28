package de.uniks.stp.wedoit.accord.client;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Category
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_SERVER = "server";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_CHANNEL = "channel";
   private String name;
   private Server server;
   protected PropertyChangeSupport listeners;
   private String id;
   private Channel channel;

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

   public Channel getChannel()
   {
      return this.channel;
   }

   public Category setChannel(Channel value)
   {
      if (this.channel == value)
      {
         return this;
      }

      final Channel oldValue = this.channel;
      if (this.channel != null)
      {
         this.channel = null;
         oldValue.setCategory(null);
      }
      this.channel = value;
      if (value != null)
      {
         value.setCategory(this);
      }
      this.firePropertyChange(PROPERTY_CHANNEL, oldValue, value);
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
      this.setChannel(null);
      this.setServer(null);
   }
}
