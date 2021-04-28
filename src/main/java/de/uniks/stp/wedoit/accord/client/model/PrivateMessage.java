package de.uniks.stp.wedoit.accord.client.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class PrivateMessage
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_TIMESTAMP = "timestamp";
   public static final String PROPERTY_TEXT = "text";
   public static final String PROPERTY_FROM = "from";
   public static final String PROPERTY_TO = "to";
   public static final String PROPERTY_CHAT = "chat";
   private String id;
   private long timestamp;
   private String text;
   private String from;
   private String to;
   private Chat chat;
   protected PropertyChangeSupport listeners;

   public String getId()
   {
      return this.id;
   }

   public PrivateMessage setId(String value)
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

   public PrivateMessage setTimestamp(long value)
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

   public PrivateMessage setText(String value)
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

   public PrivateMessage setFrom(String value)
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

   public String getTo()
   {
      return this.to;
   }

   public PrivateMessage setTo(String value)
   {
      if (Objects.equals(value, this.to))
      {
         return this;
      }

      final String oldValue = this.to;
      this.to = value;
      this.firePropertyChange(PROPERTY_TO, oldValue, value);
      return this;
   }

   public Chat getChat()
   {
      return this.chat;
   }

   public PrivateMessage setChat(Chat value)
   {
      if (this.chat == value)
      {
         return this;
      }

      final Chat oldValue = this.chat;
      if (this.chat != null)
      {
         this.chat = null;
         oldValue.withoutMessages(this);
      }
      this.chat = value;
      if (value != null)
      {
         value.withMessages(this);
      }
      this.firePropertyChange(PROPERTY_CHAT, oldValue, value);
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
      result.append(' ').append(this.getTo());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setChat(null);
   }
}
