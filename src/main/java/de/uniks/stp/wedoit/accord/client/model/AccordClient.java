package de.uniks.stp.wedoit.accord.client.model;
import java.beans.PropertyChangeSupport;

public class AccordClient
{
   public static final String PROPERTY_LOCAL_USER = "localUser";
   public static final String PROPERTY_OPTIONS = "options";
   private LocalUser localUser;
   private Options options;
   protected PropertyChangeSupport listeners;

   public LocalUser getLocalUser()
   {
      return this.localUser;
   }

   public AccordClient setLocalUser(LocalUser value)
   {
      if (this.localUser == value)
      {
         return this;
      }

      final LocalUser oldValue = this.localUser;
      if (this.localUser != null)
      {
         this.localUser = null;
         oldValue.setAccordClient(null);
      }
      this.localUser = value;
      if (value != null)
      {
         value.setAccordClient(this);
      }
      this.firePropertyChange(PROPERTY_LOCAL_USER, oldValue, value);
      return this;
   }

   public Options getOptions()
   {
      return this.options;
   }

   public AccordClient setOptions(Options value)
   {
      if (this.options == value)
      {
         return this;
      }

      final Options oldValue = this.options;
      if (this.options != null)
      {
         this.options = null;
         oldValue.setAccordClient(null);
      }
      this.options = value;
      if (value != null)
      {
         value.setAccordClient(this);
      }
      this.firePropertyChange(PROPERTY_OPTIONS, oldValue, value);
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
      this.setOptions(null);
   }
}
