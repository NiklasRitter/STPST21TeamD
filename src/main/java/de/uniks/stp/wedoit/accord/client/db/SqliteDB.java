package de.uniks.stp.wedoit.accord.client.db;

import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;

import java.sql.*;

public class SqliteDB {

    Connection c;
    Statement stmt;
    Boolean hasData = false;

    public SqliteDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./src/main/resources/data/chats.sqlite");

            initialize();


        }catch (Exception e){
            System.out.println("Couldnt init db");
            System.err.println(e.getMessage());
        }
    }

    private void initialize() throws SQLException {
        System.out.println("initing...");
        if(!hasData){
            hasData = true;

            this.stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='messages'");
            System.out.println("first query done");

            while(!rs.next()){
                System.out.println("creating...");
                this.stmt = c.createStatement();
                stmt.execute("CREATE TABLE messages(" +
                        "id integer PRIMARY KEY AUTOINCREMENT, " +
                        "timestamp integer NOT NULL, " +
                        "text varchar(255), " +
                        "sender varchar(255) NOT NULL, " +
                        "receiver varchar(255) NOT NULL"
                       );

                System.out.println("created table :)");
            }


        }
    }
    //        String id;
    //        long timestamp;
    //        String text;
    //        String from;
    //        String to;

    public void save(PrivateMessage message) throws SQLException {

        PreparedStatement prep = c.prepareStatement("INSERT INTO messages VALUES(?,?,?,?)");
        prep.setLong(1,message.getTimestamp());
        prep.setString(2,message.getText());
        prep.setString(3,message.getFrom());
        prep.setString(4,message.getTo());
        prep.execute();
    }

    public void getChats(){
        try{
            this.stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Chats");



            while(rs.next()){
                int id = rs.getInt("id");
                String from = rs.getString("from");
                //TODO
            }



        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public void close(){
        try{
            c.close();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

}
