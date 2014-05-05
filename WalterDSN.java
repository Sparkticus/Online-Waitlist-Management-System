import java.io.*;               // for Printwriter
import java.sql.*;              // for Connection

public class WalterDSN {

    private static final String HOSTNAME = "tempest.wellesley.edu";
    private static final String USERNAME = "walter";
    private static final String PASSWORD = "3jzRlTqi3MXRGqG";

    /**

       Here's how to use this function:

       Connection con = null;
       try {
            con = WendyDSN.connect("wmdb");  // or whatever database you want to connect to
            // more code here
            }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if( con != null ) {
                try {
                    con.close();
                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }
        }

    */

    public static Connection connect(String database)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException,
               SQLException
    {

        // protocol:subprotocol://host/database
        String url = "jdbc:mysql://"+ HOSTNAME + "/" + database;
        Connection con = null;

        // The following loads and instantiates the Java Module (Class)
        // called Connector/J which implements the JDBC API to the MySQL
        // database.  Connector/J is the official JDBC driver for MySQL.
        // See online documentation at mysql.com.  To use it, you have to
        // have the top-level directory or the jar file on your CLASSPATH.
        // The invocation of newInstance() is for certain buggy JVMs where
        // the static class initializer isn't run until an instance is
        // made.
        
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); // yuck
        }
        catch( ClassNotFoundException e ) {
            // This output will go into the logs. Look in $CATALINA_HOME/logs/catalina.out
            System.err.println("Couldn't load MySQL Driver: " + e);
            throw e;
        }
        catch( InstantiationException e ) {
            // This output will go into the logs. Look in $CATALINA_HOME/logs/catalina.out
            System.err.println("Couldn't load MySQL Driver: " + e);
            throw e;
        }
        catch( IllegalAccessException e ) {
            // This output will go into the logs. Look in $CATALINA_HOME/logs/catalina.out
            System.err.println("Couldn't load MySQL Driver: " + e);
            throw e;
        }
        
        try {
            con = DriverManager.getConnection(url,USERNAME,PASSWORD);
        }
        catch (SQLException e) {
            System.err.println("Cannot connect to database: " + e); 
            throw e;
        }
        return con;
    }

    /** For testing purposes only.
        Test this class by running it and seeing if it can connect to the
        database:  java Classname DBname
    */

    public static void main(String args[]) {
        if( args.length < 1 ) {
            System.out.println("Usage: java WendyDSN dbname\n"
                               +"where you have access to the given database");
        } else {
            Connection conn = null;
            try {
                conn = connect(args[0]);
                String whoami_sql = "SELECT user() as user, database() as db";
                Statement whoami_st = conn.createStatement();
                ResultSet whoami_rs = whoami_st.executeQuery(whoami_sql);
                if(whoami_rs.next()) {
                    String user = whoami_rs.getString("user");
                    String db = whoami_rs.getString("db");
                    System.out.println("You are connected as "
                                       +user
                                       +" to database "
                                       +db);
                }
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
                // For more verbose error output, use this:
                // e.printStackTrace();
            }
            finally {
                // Make sure we close the connection.  This is all
                // unnecessary, as Java would close the connection upon
                // exiting.
                if(conn != null) {
                    try {
                        conn.close();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
