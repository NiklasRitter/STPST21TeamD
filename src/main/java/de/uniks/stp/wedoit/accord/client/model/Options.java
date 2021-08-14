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
   public static final String PROPERTY_INPUT_VOLUME = "inputVolume";
   public static final String PROPERTY_AUDIO_ROOT_MEAN_SQUARE = "audioRootMeanSquare";
   public static final String PROPERTY_CHAT_FONT_SIZE = "chatFontSize";
   public static final String PROPERTY_ZOOM_LEVEL = "zoomLevel";
   private boolean darkmode;
   private AccordClient accordClient;
   protected PropertyChangeSupport listeners;
   private boolean rememberMe;
   private String language;
   private float systemVolume;
   private Info outputDevice;
   private Info inputDevice;
   private float inputVolume;
   private double audioRootMeanSquare;
   private int chatFontSize;
   private int zoomLevel;

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

   public float getInputVolume()
   {
      return this.inputVolume;
   }

   public Options setInputVolume(float value)
   {
      if (value == this.inputVolume)
      {
         return this;
      }

      final float oldValue = this.inputVolume;
      this.inputVolume = value;
      this.firePropertyChange(PROPERTY_INPUT_VOLUME, oldValue, value);
      return this;
   }

   public double getAudioRootMeanSquare()
   {
      return this.audioRootMeanSquare;
   }

   public Options setAudioRootMeanSquare(double value)
   {
      if (value == this.audioRootMeanSquare)
      {
         return this;
      }

      final double oldValue = this.audioRootMeanSquare;
      this.audioRootMeanSquare = value;
      this.firePropertyChange(PROPERTY_AUDIO_ROOT_MEAN_SQUARE, oldValue, value);
      return this;
   }

   public int getChatFontSize()
   {
      return this.chatFontSize;
   }

   public Options setChatFontSize(int value)
   {
      if (value == this.chatFontSize)
      {
         return this;
      }

      final int oldValue = this.chatFontSize;
      this.chatFontSize = value;
      this.firePropertyChange(PROPERTY_CHAT_FONT_SIZE, oldValue, value);
      return this;
   }

   public int getZoomLevel()
   {
      return this.zoomLevel;
   }

   public Options setZoomLevel(int value)
   {
      if (value == this.zoomLevel)
      {
         return this;
      }

      final int oldValue = this.zoomLevel;
      this.zoomLevel = value;
      this.firePropertyChange(PROPERTY_ZOOM_LEVEL, oldValue, value);
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
