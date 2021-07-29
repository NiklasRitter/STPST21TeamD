package de.uniks.stp.wedoit.accord.client.model;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import javax.sound.sampled.Mixer.Info;

public class Options
{
   public static final String PROPERTY_DARKMODE = "darkmode";
   public static final String PROPERTY_ACCORD_CLIENT = "accordClient";
   public static final String PROPERTY_REMEMBER_ME = "rememberMe";
   public static final String PROPERTY_LANGUAGE = "language";
   public static final String PROPERTY_OUTPUT_DEVICE = "outputDevice";
   public static final String PROPERTY_INPUT_DEVICE = "inputDevice";
   public static final String PROPERTY_SYSTEM_VOLUME = "systemVolume";
   private boolean darkmode;
   private AccordClient accordClient;
   protected PropertyChangeSupport listeners;
   private boolean rememberMe;
   private String language;
   private float systemVolume;
   private Info outputDevice;
   private Info inputDevice;

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

   public boolean isRememberMe()
   {
      return this.rememberMe;
   }

   public Options setRememberMe(boolean value)
   {
      if (value == this.rememberMe)
      {
         return this;
      }

      final boolean oldValue = this.rememberMe;
      this.rememberMe = value;
      this.firePropertyChange(PROPERTY_REMEMBER_ME, oldValue, value);
      return this;
   }

   public String getLanguage()
   {
      return this.language;
   }

   public Options setLanguage(String value)
   {
      if (Objects.equals(value, this.language))
      {
         return this;
      }

      final String oldValue = this.language;
      this.language = value;
      this.firePropertyChange(PROPERTY_LANGUAGE, oldValue, value);
      return this;
   }

   public float getSystemVolume()
   {
      return this.systemVolume;
   }

   public Options setSystemVolume(float value)
   {
      if (value == this.systemVolume)
      {
         return this;
      }

      final float oldValue = this.systemVolume;
      this.systemVolume = value;
      this.firePropertyChange(PROPERTY_SYSTEM_VOLUME, oldValue, value);
      return this;
   }

   public Info getOutputDevice()
   {
      return this.outputDevice;
   }

   public Options setOutputDevice(Info value)
   {
      if (Objects.equals(value, this.outputDevice))
      {
         return this;
      }

      final Info oldValue = this.outputDevice;
      this.outputDevice = value;
      this.firePropertyChange(PROPERTY_OUTPUT_DEVICE, oldValue, value);
      return this;
   }

   public Info getInputDevice()
   {
      return this.inputDevice;
   }

   public Options setInputDevice(Info value)
   {
      if (Objects.equals(value, this.inputDevice))
      {
         return this;
      }

      final Info oldValue = this.inputDevice;
      this.inputDevice = value;
      this.firePropertyChange(PROPERTY_INPUT_DEVICE, oldValue, value);
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

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getLanguage());
      return result.substring(1);
   }
}
