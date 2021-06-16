package de.uniks.stp.wedoit.accord.client.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Invitation
{
   public static final String PROPERTY_LINK = "link";
   public static final String PROPERTY_SERVER = "server";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_TYPE = "type";
   public static final String PROPERTY_MAX = "max";
   public static final String PROPERTY_CURRENT = "current";
   private String link;
   private Server server;
   protected PropertyChangeSupport listeners;
   private String id;
   private String type;
   private int max;
   private int current;

   public String getLink()
   {
      return this.link;
   }

   public Invitation setLink(String value)
   {
      if (Objects.equals(value, this.link))
      {
         return this;
      }

      final String oldValue = this.link;
      this.link = value;
      this.firePropertyChange(PROPERTY_LINK, oldValue, value);
      return this;
   }

   public Server getServer()
   {
      return this.server;
   }

   public Invitation setServer(Server value)
   {
      if (this.server == value)
      {
         return this;
      }

      final Server oldValue = this.server;
      if (this.server != null)
      {
         this.server = null;
         oldValue.withoutInvitations(this);
      }
      this.server = value;
      if (value != null)
      {
         value.withInvitations(this);
      }
      this.firePropertyChange(PROPERTY_SERVER, oldValue, value);
      return this;
   }

   public String getId()
   {
      return this.id;
   }

   public Invitation setId(String value)
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

   public String getType()
   {
      return this.type;
   }

   public Invitation setType(String value)
   {
      if (Objects.equals(value, this.type))
      {
         return this;
      }

      final String oldValue = this.type;
      this.type = value;
      this.firePropertyChange(PROPERTY_TYPE, oldValue, value);
      return this;
   }

   public int getMax()
   {
      return this.max;
   }

   public Invitation setMax(int value)
   {
      if (value == this.max)
      {
         return this;
      }

      final int oldValue = this.max;
      this.max = value;
      this.firePropertyChange(PROPERTY_MAX, oldValue, value);
      return this;
   }

   public int getCurrent()
   {
      return this.current;
   }

   public Invitation setCurrent(int value)
   {
      if (value == this.current)
      {
         return this;
      }

      final int oldValue = this.current;
      this.current = value;
      this.firePropertyChange(PROPERTY_CURRENT, oldValue, value);
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
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getLink());
      result.append(' ').append(this.getType());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setServer(null);
   }
}
