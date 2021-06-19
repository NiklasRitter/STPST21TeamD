package de.uniks.stp.wedoit.accord.client.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Channel
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_TYPE = "type";
   public static final String PROPERTY_PRIVILEGED = "privileged";
   public static final String PROPERTY_MESSAGES = "messages";
   public static final String PROPERTY_CATEGORY = "category";
   public static final String PROPERTY_MEMBERS = "members";
   public static final String PROPERTY_READ = "read";
   private String id;
   private String name;
   private String type;
   private boolean privileged;
   private List<Message> messages;
   private Category category;
   private List<User> members;
   protected PropertyChangeSupport listeners;
   private boolean read;

   public String getId()
   {
      return this.id;
   }

   public Channel setId(String value)
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

   public String getName()
   {
      return this.name;
   }

   public Channel setName(String value)
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

   public String getType()
   {
      return this.type;
   }

   public Channel setType(String value)
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

   public boolean isPrivileged()
   {
      return this.privileged;
   }

   public Channel setPrivileged(boolean value)
   {
      if (value == this.privileged)
      {
         return this;
      }

      final boolean oldValue = this.privileged;
      this.privileged = value;
      this.firePropertyChange(PROPERTY_PRIVILEGED, oldValue, value);
      return this;
   }

   public List<Message> getMessages()
   {
      return this.messages != null ? Collections.unmodifiableList(this.messages) : Collections.emptyList();
   }

   public Channel withMessages(Message value)
   {
      if (this.messages == null)
      {
         this.messages = new ArrayList<>();
      }
      if (!this.messages.contains(value))
      {
         this.messages.add(value);
         value.setChannel(this);
         this.firePropertyChange(PROPERTY_MESSAGES, null, value);
      }
      return this;
   }

   public Channel withMessages(Message... value)
   {
      for (final Message item : value)
      {
         this.withMessages(item);
      }
      return this;
   }

   public Channel withMessages(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withMessages(item);
      }
      return this;
   }

   public Channel withoutMessages(Message value)
   {
      if (this.messages != null && this.messages.remove(value))
      {
         value.setChannel(null);
         this.firePropertyChange(PROPERTY_MESSAGES, value, null);
      }
      return this;
   }

   public Channel withoutMessages(Message... value)
   {
      for (final Message item : value)
      {
         this.withoutMessages(item);
      }
      return this;
   }

   public Channel withoutMessages(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withoutMessages(item);
      }
      return this;
   }

   public Category getCategory()
   {
      return this.category;
   }

   public Channel setCategory(Category value)
   {
      if (this.category == value)
      {
         return this;
      }

      final Category oldValue = this.category;
      if (this.category != null)
      {
         this.category = null;
         oldValue.withoutChannels(this);
      }
      this.category = value;
      if (value != null)
      {
         value.withChannels(this);
      }
      this.firePropertyChange(PROPERTY_CATEGORY, oldValue, value);
      return this;
   }

   public List<User> getMembers()
   {
      return this.members != null ? Collections.unmodifiableList(this.members) : Collections.emptyList();
   }

   public Channel withMembers(User value)
   {
      if (this.members == null)
      {
         this.members = new ArrayList<>();
      }
      if (!this.members.contains(value))
      {
         this.members.add(value);
         value.withChannels(this);
         this.firePropertyChange(PROPERTY_MEMBERS, null, value);
      }
      return this;
   }

   public Channel withMembers(User... value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

   public Channel withMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

   public Channel withoutMembers(User value)
   {
      if (this.members != null && this.members.remove(value))
      {
         value.withoutChannels(this);
         this.firePropertyChange(PROPERTY_MEMBERS, value, null);
      }
      return this;
   }

   public Channel withoutMembers(User... value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

   public Channel withoutMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

   public boolean isRead()
   {
      return this.read;
   }

   public Channel setRead(boolean value)
   {
      if (value == this.read)
      {
         return this;
      }

      final boolean oldValue = this.read;
      this.read = value;
      this.firePropertyChange(PROPERTY_READ, oldValue, value);
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
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getType());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutMessages(new ArrayList<>(this.getMessages()));
      this.setCategory(null);
      this.withoutMembers(new ArrayList<>(this.getMembers()));
   }
}
