package de.uniks.stp.wedoit.accord.client.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class Server {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_CATEGORIES = "categories";
    public static final String PROPERTY_MEMBERS = "members";
    public static final String PROPERTY_LOCAL_USER = "localUser";
    protected PropertyChangeSupport listeners;
    private String name;
    private String id;
    private String owner;
    private List<Category> categories;
    private List<User> members;
    private LocalUser localUser;

    public String getName()
   {
      return this.name;
   }

    public Server setName(String value)
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

    public Server setId(String value)
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

    public String getOwner()
   {
      return this.owner;
   }

    public Server setOwner(String value)
   {
      if (Objects.equals(value, this.owner))
      {
         return this;
      }

      final String oldValue = this.owner;
      this.owner = value;
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

    public List<Category> getCategories()
   {
      return this.categories != null ? Collections.unmodifiableList(this.categories) : Collections.emptyList();
   }

    public Server withCategories(Category value)
   {
      if (this.categories == null)
      {
         this.categories = new ArrayList<>();
      }
      if (!this.categories.contains(value))
      {
         this.categories.add(value);
         value.setServer(this);
         this.firePropertyChange(PROPERTY_CATEGORIES, null, value);
      }
      return this;
   }

    public Server withCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

    public Server withCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

    public Server withoutCategories(Category value)
   {
      if (this.categories != null && this.categories.remove(value))
      {
         value.setServer(null);
         this.firePropertyChange(PROPERTY_CATEGORIES, value, null);
      }
      return this;
   }

    public Server withoutCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

    public Server withoutCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

    public List<User> getMembers()
   {
      return this.members != null ? Collections.unmodifiableList(this.members) : Collections.emptyList();
   }

    public Server withMembers(User value)
   {
      if (this.members == null)
      {
         this.members = new ArrayList<>();
      }
      if (!this.members.contains(value))
      {
         this.members.add(value);
         value.withServers(this);
         this.firePropertyChange(PROPERTY_MEMBERS, null, value);
      }
      return this;
   }

    public Server withMembers(User... value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

    public Server withMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

    public Server withoutMembers(User value)
   {
      if (this.members != null && this.members.remove(value))
      {
         value.withoutServers(this);
         this.firePropertyChange(PROPERTY_MEMBERS, value, null);
      }
      return this;
   }

    public Server withoutMembers(User... value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

    public Server withoutMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

    public LocalUser getLocalUser()
   {
      return this.localUser;
   }

    public Server setLocalUser(LocalUser value)
   {
      if (this.localUser == value)
      {
         return this;
      }

      final LocalUser oldValue = this.localUser;
      if (this.localUser != null)
      {
         this.localUser = null;
         oldValue.withoutServers(this);
      }
      this.localUser = value;
      if (value != null)
      {
         value.withServers(this);
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
      result.append(' ').append(this.getOwner());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.withoutCategories(new ArrayList<>(this.getCategories()));
      this.withoutMembers(new ArrayList<>(this.getMembers()));
      this.setLocalUser(null);
   }
}
