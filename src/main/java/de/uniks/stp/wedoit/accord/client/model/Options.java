package de.uniks.stp.wedoit.accord.client.model;
import java.beans.PropertyChangeSupport;

public class Options
{
   public static final String PROPERTY_DARKMODE = "darkmode";
   public static final String PROPERTY_ACCORD_CLIENT = "accordClient";
   private boolean darkmode;
   protected PropertyChangeSupport listeners;
   private AccordClient accordClient;

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

   public AccordClient getAccordClient()
   {
      return this.accordClient;
   }

   public Options setAccordClient(AccordClient value)
   {
      if (this.accordClient == value)
      {
         return this;
      }

      final AccordClient oldValue = this.accordClient;
      if (this.accordClient != null)
      {
         this.accordClient = null;
         oldValue.setOptions(null);
      }
      this.accordClient = value;
      if (value != null)
      {
         value.setOptions(this);
      }
      this.firePropertyChange(PROPERTY_ACCORD_CLIENT, oldValue, value);
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
      this.setAccordClient(null);
   }
}
