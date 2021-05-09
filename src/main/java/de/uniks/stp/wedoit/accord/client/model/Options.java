package de.uniks.stp.wedoit.accord.client.model;
import java.beans.PropertyChangeSupport;

public class Options
{
   public static final String PROPERTY_DARKMODE = "darkmode";
   public static final String PROPERTY_LOCAL_USER = "localUser";
   private boolean darkmode;
   private LocalUser localUser;
   protected PropertyChangeSupport listeners;

   public boolean isDarkmode()
   {
      return this.darkmode;
   }

   public Options setDarkmode(boolean value)
   {
      if (value == this.darkmode)
      {
         return this;
      }

      final boolean oldValue = this.darkmode;
      this.darkmode = value;
      this.firePropertyChange(PROPERTY_DARKMODE, oldValue, value);
      return this;
   }

   public LocalUser getLocalUser()
   {
      return this.localUser;
   }

   public Options setLocalUser(LocalUser value)
   {
      if (this.localUser == value)
      {
         return this;
      }

      final LocalUser oldValue = this.localUser;
      if (this.localUser != null)
      {
         this.localUser = null;
         oldValue.setOptions(null);
      }
      this.localUser = value;
      if (value != null)
      {
         value.setOptions(this);
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

   public void removeYou()
   {
      this.setLocalUser(null);
   }
}
