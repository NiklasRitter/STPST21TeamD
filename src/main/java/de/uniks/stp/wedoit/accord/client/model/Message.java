package de.uniks.stp.wedoit.accord.client.model;

import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Message
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_TIMESTAMP = "timestamp";
   public static final String PROPERTY_TEXT = "text";
   public static final String PROPERTY_FROM = "from";
   public static final String PROPERTY_CHANNEL = "channel";
   private String id;
   private long timestamp;
   private String text;
   private String from;
   private Channel channel;
   protected PropertyChangeSupport listeners;

   public String getId()
   {
      return this.id;
   }

   public Message setId(String value)
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

   public long getTimestamp()
   {
      return this.timestamp;
   }

   public Message setTimestamp(long value)
   {
      if (value == this.timestamp)
      {
         return this;
      }

      final long oldValue = this.timestamp;
      this.timestamp = value;
      this.firePropertyChange(PROPERTY_TIMESTAMP, oldValue, value);
      return this;
   }

   public String getText()
   {
      return this.text;
   }

   public Message setText(String value)
   {
      if (Objects.equals(value, this.text))
      {
         return this;
      }

      final String oldValue = this.text;
      this.text = value;
      this.firePropertyChange(PROPERTY_TEXT, oldValue, value);
      return this;
   }

   public String getFrom()
   {
      return this.from;
   }

   public Message setFrom(String value)
   {
      if (Objects.equals(value, this.from))
      {
         return this;
      }

      final String oldValue = this.from;
      this.from = value;
      this.firePropertyChange(PROPERTY_FROM, oldValue, value);
      return this;
   }

   public Channel getChannel()
   {
      return this.channel;
   }

   public Message setChannel(Channel value)
   {
      if (this.channel == value)
      {
         return this;
      }

      final Channel oldValue = this.channel;
      if (this.channel != null)
      {
         this.channel = null;
         oldValue.withoutMessages(this);
      }
      this.channel = value;
      if (value != null)
      {
         value.withMessages(this);
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
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getText());
      result.append(' ').append(this.getFrom());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setChannel(null);
   }
}
