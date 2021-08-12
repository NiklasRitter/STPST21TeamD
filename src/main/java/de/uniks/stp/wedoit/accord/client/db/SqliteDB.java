package de.uniks.stp.wedoit.accord.client.db;

import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SqliteDB {

    private final String username;
    private final String url = "jdbc:sqlite::resource:" + getClass().getResource("/data/");

    /**
     * When creating a SqliteDB object </p>
     * creates three tables: messages, privateChats, options
     *
     * @param username for creating the DB
     */
    public SqliteDB(String username) {
        this.username = username;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection(url + username + ".sqlite");

            Statement stmt = c.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + "	text varchar(255) NOT NULL,\n"
                    + " times long NOT NULL,\n"
                    + "	sender varchar(255) NOT NULL,\n"
                    + " receiver varchar(255) NOT NULL "
                    + ");"
            );

            stmt.execute("CREATE TABLE IF NOT EXISTS privateChats (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + "	user varchar(255) NOT NULL,\n"
                    + " read boolean NOT NULL\n"
                    + ");"
            );

            stmt.execute("CREATE TABLE IF NOT EXISTS options (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + " key varchar(255) NOT NULL,\n"
                    + " value varchar(255)\n"
                    + ");"
            );

            c.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * saves a PrivateMessage to the DB
     *
     * @param message to be saved
     */
    public void save(PrivateMessage message) {
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("INSERT INTO messages VALUES(NULL,?,?,?,?);")) {

            prep.setString(1, message.getText());
            prep.setLong(2, message.getTimestamp());
            prep.setString(3, message.getFrom());
            prep.setString(4, message.getTo());


            prep.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
        updateOrInsertUserChatRead(message.getChat().getUser());
    }

    public String getSteam64ID() {
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("SELECT * FROM options WHERE key = 'steam64ID' LIMIT 1;")) {

            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                return rs.getString("value");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSteam64ID(String steam64ID) {
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("INSERT OR REPLACE INTO options(id,key,value) VALUES(1,?,?)")) {

            prep.setString(1, "steam64ID");
            prep.setString(2, steam64ID);

            prep.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates or inserts the read status in userChats
     *
     * @param user that the status should be changed
     */
    public void updateOrInsertUserChatRead(User user) {
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("INSERT OR REPLACE INTO privateChats (id, user, read) " +
                     "VALUES((SELECT id FROM privateChats WHERE user = ?),?,?)")) {

            prep.setString(1, user.getName());
            prep.setString(2, user.getName());
            prep.setBoolean(3, user.isChatRead());


            prep.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Query for messages between username (set in constructor) and user, ordered by timestamp
     *
     * @param user username to find chats with
     */
    public void getChatReadForUser(User user) {
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("SELECT * FROM privateChats WHERE user = ? LIMIT 1")) {

            prep.setString(1, user.getName());

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                user.setChatRead(rs.getBoolean("read"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Query for messages between username (set in constructor) and user, ordered by timestamp
     *
     * @param user username to find chats with
     * @return list of PrivateMessage that the localUser had with user
     */
    public List<PrivateMessage> getAllMessagesBetweenUsers(String user) {
        List<PrivateMessage> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("SELECT * FROM messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY times")) {

            prep.setString(1, user);
            prep.setString(2, username);
            prep.setString(3, username);
            prep.setString(4, user);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                PrivateMessage msg = new PrivateMessage();
                msg.setText(rs.getString("text"));
                msg.setTimestamp(rs.getLong("times"));
                msg.setTo(rs.getString("receiver"));
                msg.setFrom(rs.getString("sender"));

                messages.add(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Query for messages between username (set in constructor) and user, ordered by timestamp
     *
     * @param user   username to find chats with
     * @param offset The offset from the start
     * @return list of PrivateMessage that the localUser had with user
     */
    public List<PrivateMessage> getLastFiftyMessagesBetweenUsers(String user, int offset) {
        List<PrivateMessage> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("SELECT * FROM messages WHERE ((sender = ? AND receiver =" +
                     " ?) OR (sender = ? AND receiver = ?)) ORDER BY id DESC LIMIT ?, 50")) {
            prep.setString(1, user);
            prep.setString(2, username);
            prep.setString(3, username);
            prep.setString(4, user);
            prep.setInt(5, offset);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                PrivateMessage msg = new PrivateMessage();
                msg.setText(rs.getString("text"));
                msg.setTimestamp(rs.getLong("times"));
                msg.setTo(rs.getString("receiver"));
                msg.setFrom(rs.getString("sender"));

                messages.add(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Query for messages between username (set in constructor) and user, ordered by timestamp
     *
     * @param user username to find chats with
     * @return list of PrivateMessage that the localUser had with user
     */
    public List<PrivateMessage> getLastFiftyMessagesBetweenUsers(String user) {
        List<PrivateMessage> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite");
             PreparedStatement prep = conn.prepareStatement("SELECT * FROM messages WHERE ((sender = ? AND receiver =" +
                     " ?) OR (sender = ? AND receiver = ?)) ORDER BY id DESC LIMIT 50")) {
            prep.setString(1, user);
            prep.setString(2, username);
            prep.setString(3, username);
            prep.setString(4, user);

            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                PrivateMessage msg = new PrivateMessage();
                msg.setText(rs.getString("text"));
                msg.setTimestamp(rs.getLong("times"));
                msg.setTo(rs.getString("receiver"));
                msg.setFrom(rs.getString("sender"));

                messages.add(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Query for started chats between username (set in constructor) and user, ordered by timestamp
     *
     * @param user username to find chats with
     * @return list of usernames that a chat is opened with
     */
    public List<String> getOpenChats(String user) {
        Set<String> users = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(url + username + ".sqlite")) {
            PreparedStatement prep = conn.prepareStatement("SELECT * FROM messages WHERE sender = ? OR receiver = ? ORDER BY id;");

            prep.setString(1, user);
            prep.setString(2, username);
            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                if (rs.getString("sender").equals(username)) users.add(rs.getString("receiver"));
                else users.add(rs.getString("sender"));
            }
            conn.close();
            prep.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(users);
    }

}
