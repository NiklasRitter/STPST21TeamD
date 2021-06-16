package de.uniks.stp.wedoit.accord.client.db;

import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteDB {

    private String username;
    private String url = "jdbc:sqlite:./src/main/resources/data/";

    public SqliteDB(String username){
        this.username = username;
        try{
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection(url + username + ".sqlite");

            Statement stmt = c.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + "	text varchar(255) NOT NULL,\n"
                    + " times long NOT NULL,\n"
                    + "	sender varchar(255) NOT NULL,\n"
                    + " reciever varchar(255) NOT NULL "
                    + ");"
            );

            c.close();


        }catch (Exception e){
            System.err.println("Couldnt init db");
            e.printStackTrace();
        }
    }

    public void save(PrivateMessage message) {
        try(Connection conn = DriverManager.getConnection(url + username + ".sqlite");
            PreparedStatement prep = conn.prepareStatement("INSERT INTO messages VALUES(NULL,?,?,?,?);")){

            prep.setLong(1, message.getTimestamp());
            prep.setString(2, message.getText());
            prep.setString(3, message.getFrom());
            prep.setString(4, message.getTo());
            prep.execute();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<PrivateMessage> getMessagesBetweenUsers(String user1, String user2){
        List<PrivateMessage> messages = new ArrayList<>();
        try(Connection conn = DriverManager.getConnection(url + username + ".sqlite");
        PreparedStatement prep = conn.prepareStatement("SELECT * FROM " + username + " WHERE (sender = ? AND reciever = ?) OR (sender = ? AND reciever = ?) ORDER BY times DESC")){
            prep.setString(1,user1);
            prep.setString(2,user2);
            prep.setString(3,user2);
            prep.setString(4,user1);

            ResultSet rs = prep.executeQuery();

            while(rs.next()){
                PrivateMessage msg = new PrivateMessage();
                msg.setTimestamp(rs.getLong("times"));
                msg.setTo(rs.getString("receiver"));
                msg.setFrom(rs.getString("sender"));
                msg.setText(rs.getString("text"));
                messages.add(msg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return messages;

    }

}
