package de.uniks.stp.wedoit.accord.client.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;
import java.util.Timer;

public class LocalUser
{
   public static final String PROPERTY_PASSWORD = "password";
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_USER_KEY = "userKey";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_GAME_INVITES = "gameInvites";
   public static final String PROPERTY_GAME_REQUESTS = "gameRequests";
   public static final String PROPERTY_SERVERS = "servers";
   public static final String PROPERTY_USERS = "users";
   public static final String PROPERTY_ACCORD_CLIENT = "accordClient";
   public static final String PROPERTY_AUDIO_CHANNEL = "audioChannel";
   public static final String PROPERTY_MUTED = "muted";
   public static final String PROPERTY_ALL_MUTED = "allMuted";
   public static final String PROPERTY_DESCRIPTION = "description";
   public static final String PROPERTY_STEAM64_ID = "steam64ID";
   public static final String PROPERTY_STEAM_GAME_EXTRA_INFO = "steamGameExtraInfo";
   public static final String PROPERTY_STEAM_GAME_EXTRA_INFO_TIMER = "steamGameExtraInfoTimer";
   public static final String PROPERTY_CUSTOM_DESCRIPTION = "customDescription";
   private String password;
   private String name;
   private String userKey;
   private String id;
   private List<User> gameInvites;
   private List<User> gameRequests;
   private List<Server> servers;
   private List<User> users;
   private AccordClient accordClient;
   protected PropertyChangeSupport listeners;
   private Channel audioChannel;
   private boolean muted;
   private boolean allMuted;
   private String description;
   private String steam64ID;
   private String steamGameExtraInfo;
   private Timer steamGameExtraInfoTimer;
   private String customDescription;

   public String getPassword()
   {
      return this.password;
   }

   public LocalUser setPassword(String value)
   {
      if (Objects.equals(value, this.password))
      {
         return this;
      }

      final String oldValue = this.password;
      this.password = value;
      this.firePropertyChange(PROPERTY_PASSWORD, oldValue, value);
      return this;
   }

   public String getName()
   {
      return this.name;
   }

   public LocalUser setName(String value)
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

   public String getUserKey()
   {
      return this.userKey;
   }

   public LocalUser setUserKey(String value)
   {
      if (Objects.equals(value, this.userKey))
      {
         return this;
      }

      final String oldValue = this.userKey;
      this.userKey = value;
      this.firePropertyChange(PROPERTY_USER_KEY, oldValue, value);
      return this;
   }

   public String getId()
   {
      return this.id;
   }

   public LocalUser setId(String value)
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

   public List<User> getGameInvites()
   {
      return this.gameInvites != null ? Collections.unmodifiableList(this.gameInvites) : Collections.emptyList();
   }

   public LocalUser withGameInvites(User value)
   {
      if (this.gameInvites == null)
      {
         this.gameInvites = new ArrayList<>();
      }
      if (this.gameInvites.add(value))
      {
         this.firePropertyChange(PROPERTY_GAME_INVITES, null, value);
      }
      return this;
   }

   public LocalUser withGameInvites(User... value)
   {
      for (final User item : value)
      {
         this.withGameInvites(item);
      }
      return this;
   }

   public LocalUser withGameInvites(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withGameInvites(item);
      }
      return this;
   }

   public LocalUser withoutGameInvites(User value)
   {
      if (this.gameInvites != null && this.gameInvites.removeAll(Collections.singleton(value)))
      {
         this.firePropertyChange(PROPERTY_GAME_INVITES, value, null);
      }
      return this;
   }

   public LocalUser withoutGameInvites(User... value)
   {
      for (final User item : value)
      {
         this.withoutGameInvites(item);
      }
      return this;
   }

   public LocalUser withoutGameInvites(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutGameInvites(item);
      }
      return this;
   }

   public List<User> getGameRequests()
   {
      return this.gameRequests != null ? Collections.unmodifiableList(this.gameRequests) : Collections.emptyList();
   }

   public LocalUser withGameRequests(User value)
   {
      if (this.gameRequests == null)
      {
         this.gameRequests = new ArrayList<>();
      }
      if (this.gameRequests.add(value))
      {
         this.firePropertyChange(PROPERTY_GAME_REQUESTS, null, value);
      }
      return this;
   }

   public LocalUser withGameRequests(User... value)
   {
      for (final User item : value)
      {
         this.withGameRequests(item);
      }
      return this;
   }

   public LocalUser withGameRequests(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withGameRequests(item);
      }
      return this;
   }

   public LocalUser withoutGameRequests(User value)
   {
      if (this.gameRequests != null && this.gameRequests.removeAll(Collections.singleton(value)))
      {
         this.firePropertyChange(PROPERTY_GAME_REQUESTS, value, null);
      }
      return this;
   }

   public LocalUser withoutGameRequests(User... value)
   {
      for (final User item : value)
      {
         this.withoutGameRequests(item);
      }
      return this;
   }

   public LocalUser withoutGameRequests(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutGameRequests(item);
      }
      return this;
   }

   public List<Server> getServers()
   {
      return this.servers != null ? Collections.unmodifiableList(this.servers) : Collections.emptyList();
   }

   public LocalUser withServers(Server value)
   {
      if (this.servers == null)
      {
         this.servers = new ArrayList<>();
      }
      if (!this.servers.contains(value))
      {
         this.servers.add(value);
         value.setLocalUser(this);
         this.firePropertyChange(PROPERTY_SERVERS, null, value);
      }
      return this;
   }

   public LocalUser withServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withServers(item);
      }
      return this;
   }

   public LocalUser withServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withServers(item);
      }
      return this;
   }

   public LocalUser withoutServers(Server value)
   {
      if (this.servers != null && this.servers.remove(value))
      {
         value.setLocalUser(null);
         this.firePropertyChange(PROPERTY_SERVERS, value, null);
      }
      return this;
   }

   public LocalUser withoutServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutServers(item);
      }
      return this;
   }

   public LocalUser withoutServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutServers(item);
      }
      return this;
   }

   public List<User> getUsers()
   {
      return this.users != null ? Collections.unmodifiableList(this.users) : Collections.emptyList();
   }

   public LocalUser withUsers(User value)
   {
      if (this.users == null)
      {
         this.users = new ArrayList<>();
      }
      if (!this.users.contains(value))
      {
         this.users.add(value);
         value.setLocalUser(this);
         this.firePropertyChange(PROPERTY_USERS, null, value);
      }
      return this;
   }

   public LocalUser withUsers(User... value)
   {
      for (final User item : value)
      {
         this.withUsers(item);
      }
      return this;
   }

   public LocalUser withUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withUsers(item);
      }
      return this;
   }

   public LocalUser withoutUsers(User value)
   {
      if (this.users != null && this.users.remove(value))
      {
         value.setLocalUser(null);
         this.firePropertyChange(PROPERTY_USERS, value, null);
      }
      return this;
   }

   public LocalUser withoutUsers(User... value)
   {
      for (final User item : value)
      {
         this.withoutUsers(item);
      }
      return this;
   }

   public LocalUser withoutUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutUsers(item);
      }
      return this;
   }

   public AccordClient getAccordClient()
   {
      return this.accordClient;
   }

   public LocalUser setAccordClient(AccordClient value)
   {
      if (this.accordClient == value)
      {
         return this;
      }

      final AccordClient oldValue = this.accordClient;
      if (this.accordClient != null)
      {
         this.accordClient = null;
         oldValue.setLocalUser(null);
      }
      this.accordClient = value;
      if (value != null)
      {
         value.setLocalUser(this);
      }
      this.firePropertyChange(PROPERTY_ACCORD_CLIENT, oldValue, value);
      return this;
   }

   public Channel getAudioChannel()
   {
      return this.audioChannel;
   }

   public LocalUser setAudioChannel(Channel value)
   {
      if (Objects.equals(value, this.audioChannel))
      {
         return this;
      }

      final Channel oldValue = this.audioChannel;
      this.audioChannel = value;
      this.firePropertyChange(PROPERTY_AUDIO_CHANNEL, oldValue, value);
      return this;
   }

   public boolean isMuted()
   {
      return this.muted;
   }

   public LocalUser setMuted(boolean value)
   {
      if (value == this.muted)
      {
         return this;
      }

      final boolean oldValue = this.muted;
      this.muted = value;
      this.firePropertyChange(PROPERTY_MUTED, oldValue, value);
      return this;
   }

   public boolean isAllMuted()
   {
      return this.allMuted;
   }

   public LocalUser setAllMuted(boolean value)
   {
      if (value == this.allMuted)
      {
         return this;
      }

      final boolean oldValue = this.allMuted;
      this.allMuted = value;
      this.firePropertyChange(PROPERTY_ALL_MUTED, oldValue, value);
      return this;
   }

   public String getDescription()
   {
      return this.description;
   }

   public LocalUser setDescription(String value)
   {
      if (Objects.equals(value, this.description))
      {
         return this;
      }

      final String oldValue = this.description;
      this.description = value;
      this.firePropertyChange(PROPERTY_DESCRIPTION, oldValue, value);
      return this;
   }

   public String getSteam64ID()
   {
      return this.steam64ID;
   }

   public LocalUser setSteam64ID(String value)
   {
      if (Objects.equals(value, this.steam64ID))
      {
         return this;
      }

      final String oldValue = this.steam64ID;
      this.steam64ID = value;
      this.firePropertyChange(PROPERTY_STEAM64_ID, oldValue, value);
      return this;
   }

   public String getSteamGameExtraInfo()
   {
      return this.steamGameExtraInfo;
   }

   public LocalUser setSteamGameExtraInfo(String value)
   {
      if (Objects.equals(value, this.steamGameExtraInfo))
      {
         return this;
      }

      final String oldValue = this.steamGameExtraInfo;
      this.steamGameExtraInfo = value;
      this.firePropertyChange(PROPERTY_STEAM_GAME_EXTRA_INFO, oldValue, value);
      return this;
   }

   public Timer getSteamGameExtraInfoTimer()
   {
      return this.steamGameExtraInfoTimer;
   }

   public LocalUser setSteamGameExtraInfoTimer(Timer value)
   {
      if (Objects.equals(value, this.steamGameExtraInfoTimer))
      {
         return this;
      }

      final Timer oldValue = this.steamGameExtraInfoTimer;
      this.steamGameExtraInfoTimer = value;
      this.firePropertyChange(PROPERTY_STEAM_GAME_EXTRA_INFO_TIMER, oldValue, value);
      return this;
   }

   public String getCustomDescription()
   {
      return this.customDescription;
   }

   public LocalUser setCustomDescription(String value)
   {
      if (Objects.equals(value, this.customDescription))
      {
         return this;
      }

      final String oldValue = this.customDescription;
      this.customDescription = value;
      this.firePropertyChange(PROPERTY_CUSTOM_DESCRIPTION, oldValue, value);
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
      result.append(' ').append(this.getCustomDescription());
      result.append(' ').append(this.getPassword());
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getUserKey());
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getSteam64ID());
      result.append(' ').append(this.getSteamGameExtraInfo());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutServers(new ArrayList<>(this.getServers()));
      this.withoutUsers(new ArrayList<>(this.getUsers()));
      this.setAccordClient(null);
   }
}
