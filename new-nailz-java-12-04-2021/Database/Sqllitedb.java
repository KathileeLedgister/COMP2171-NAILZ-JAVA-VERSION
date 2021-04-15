
// mvn install:install-file -Dfile=./libs/sqlite-jdbc-3.34.0.jar -DgroupId=com.app.nailz -DartifactId=sqlite  -Dversion=3.34.0  -Dpackaging=jar -DgeneratePom=true
package com.app.nailz;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @authors 
 * Kathilee Ledgister -  620121618
 * Shanee Barnes - 620076360
 * Jordan Wilson -  620119365
 * Raman Lewis - 620117907
 */
public class Sqllitedb {

    public static final String DBSOURCE = "jdbc:sqlite:./db/nailz.db";
    public Connection m_connection = null;

    /**
     * Database constructor.
     */
    public Sqllitedb() {
        super();
        try {
            m_connection = DriverManager.getConnection(DBSOURCE);
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
        }
    }

    /**
     * Close the database
     * @return 
     */
    public Sqllitedb close() {
        try {
            if (m_connection != null) {
                m_connection.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            m_connection = null; // database connection hanging around ??
        }

        return this;
    }

    protected void finalize() {
        try {
            super.finalize();
            if (m_connection != null) {
                System.out.println("Please call close(). This is finalize"); //test finalize run only once
            }
        } catch (Throwable ex) {
            Logger.getLogger(Sqllitedb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Establish connection to the databse
     * @return 
     */
    public Connection getConnection() {
        return m_connection;
    }

     /**
     * initialize the database; 
     * @return 
     */
    public Sqllitedb db_init() {
        Statement stmt = null;
        PreparedStatement pstmt = null;

        do {
            if (m_connection == null) {
                break;
            }
            try {
                // create a database connection
                stmt = m_connection.createStatement();
                //statement.setQueryTimeout(30);  // set timeout to 30 sec.


                /**
                 * Create the user accounts table.
                 */
                stmt.executeUpdate("CREATE TABLE users ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "userid text NOT NULL UNIQUE,"
                        + "email text,"
                        + "password text,"
                        + "level INTEGER,"
                        + "fullname text,"
                        + "allergy text,"
                        + "notify text,"
                        + "subscription text,"
                        + "phone text)");

                stmt.close();
                stmt = null;

                /**
                 * Initialize the table with the administrator
                 * accounts. This administrator account cannot be
                 * created using the application.
                 */
                pstmt = m_connection.prepareStatement("INSERT INTO users (userid, email, password,"
                        + "level, fullname, allergy, notify, subscription,"
                        + "phone) VALUES (?,?,?,?,?,?,?,?,?)");

                // Table just created, creating some rows
                // set the corresponding param
                /**
                 * Add the admin account.
                 */
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin@nailz.com");
                pstmt.setString(3, "password");//pstmt.setString(3, Md5Hash.getMd5("adm@12$34"));
                pstmt.setInt(4, 0);
                pstmt.setString(5, "");
                pstmt.setString(6, "");
                pstmt.setString(7, "");
                pstmt.setString(8, "");
                pstmt.setString(9, "");
                // update 
                pstmt.executeUpdate();

                /**
                 * Add the manager account.
                 */
                pstmt.clearParameters();
                // set the corresponding param
                pstmt.setString(1, "manager");
                pstmt.setString(2, "manager@nailz.com");
                pstmt.setString(3, "password");
                pstmt.setInt(4, 1);
                pstmt.setString(5, "");
                pstmt.setString(6, "");
                pstmt.setString(7, "");
                pstmt.setString(8, "");
                pstmt.setString(9, "");
                // update 
                pstmt.executeUpdate();

                pstmt.close();
                pstmt = null;

                System.out.println("Database Table [nailz.users] Created and Initialized. Ready ...");
            } catch (SQLException e) {
                // if the error message is "out of memory",
                // it probably means no database file is found
                // Table already created
                System.err.println("Database [nailz.users] Found and Initialized. Ready ...");
                //System.err.println(e.getMessage());
            } finally {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }

            try {
                // create a database connection
                stmt = m_connection.createStatement();

                /**
                 * Create the appointments table.
                 */
                stmt.executeUpdate("CREATE TABLE appointments ("
                        + "a_slot INTEGER NOT NULL PRIMARY KEY,"
                        + "a_uuid INTEGER NOT NULL,"
                        + "a_service text,"
                        + "a_status text)");

                stmt.close();
                stmt = null;

                System.out.println("Database [nailz.appointments] Created and Initialized. Ready ...");
            } catch (SQLException e) {
                // if the error message is "out of memory",
                // it probably means no database file is found
                // Table already created
                System.err.println("Database [nailz.appointments] Found and Initialized. Ready ...");
                try {
                    stmt.close();
                    stmt = null;
                    //stmt = m_connection.createStatement();
                    //stmt.executeUpdate("DELETE FROM appointments");
                    System.err.println(e.getMessage());
                } catch (SQLException e1) {

                }
            } finally {
            }

        } while (false);

        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e.getMessage());
        }

        return this;
    }

      /**
     * convert the java open database connectivity(odbc)
     * results returned from a query to a json array
     * @param rs
     * @return
     * @throws SQLException 
     */
    public static JSONArray convert(ResultSet rs) throws SQLException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i = 1; i < numColumns + 1; i++) {
                String column_name = rsmd.getColumnName(i);

                switch (rsmd.getColumnType(i)) {
                    case java.sql.Types.ARRAY:
                        obj.put(column_name, rs.getArray(column_name));
                        break;
                    case java.sql.Types.BIGINT:
                        obj.put(column_name, rs.getInt(column_name));
                        break;
                    case java.sql.Types.BOOLEAN:
                        obj.put(column_name, rs.getBoolean(column_name));
                        break;
                    case java.sql.Types.BLOB:
                        obj.put(column_name, rs.getBlob(column_name));
                        break;
                    case java.sql.Types.DOUBLE:
                        obj.put(column_name, rs.getDouble(column_name));
                        break;
                    case java.sql.Types.FLOAT:
                        obj.put(column_name, rs.getFloat(column_name));
                        break;
                    case java.sql.Types.INTEGER:
                        //obj.put(column_name, rs.getInt(column_name));
                        obj.put(column_name, rs.getLong(column_name));
                        break;
                    case java.sql.Types.NVARCHAR:
                        obj.put(column_name, rs.getNString(column_name));
                        break;
                    case java.sql.Types.VARCHAR:
                        obj.put(column_name, rs.getString(column_name));
                        break;
                    case java.sql.Types.TINYINT:
                        obj.put(column_name, rs.getInt(column_name));
                        break;
                    case java.sql.Types.SMALLINT:
                        obj.put(column_name, rs.getInt(column_name));
                        break;
                    case java.sql.Types.DATE:
                        obj.put(column_name, rs.getDate(column_name));
                        break;
                    case java.sql.Types.TIMESTAMP:
                        obj.put(column_name, rs.getTimestamp(column_name));
                        break;
                    default:
                        obj.put(column_name, rs.getObject(column_name));
                        break;
                }
            }

            json.add(obj);
        }

        return json;
    }
}
