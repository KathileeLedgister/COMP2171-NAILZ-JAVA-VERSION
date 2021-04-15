
package com.app.nailz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import net.minidev.json.JSONObject;

/**
 *
 * @authors 
 * Kathilee Ledgister -  620121618
 * Shanee Barnes - 620076360
 * Jordan Wilson -  620119365
 * Raman Lewis - 620117907
 */

public class UserAccount {

    private long id = 1;
    private String content = "";

    private String m_password;
    private String m_userid;
    private String m_email;
    private String m_fullname;
    private String m__app_submitter;
    private String m_allergy;
    private String m_notify;
    private String m_phone;
    private int m_session;
    private int m_level = 9;
    private int m_uuid;
    private String m_subscription;

    private Sqllitedb m_db = null;

    public UserAccount addToDB() {

        return this;
    }

      /**
     * close the database after performing an operation with
     * a user account or user request
     * @return 
     */
    public UserAccount closedb() {
        if (m_db != null) {
            m_db.close();
            m_db = null;
        }
        return this;
    }

        /**
     * open database for allowing operations to be performed
     * with user account or user request
     * @return 
     */
    private boolean openDB() {
        boolean bres = true;

        if (m_db == null || m_db.m_connection == null) {
            m_db = new Sqllitedb();
            if (m_db.m_connection == null) {
                m_db.close();
                bres = false;
            }
        }
        return bres;
    }

        /**
     * takes the user through the sign-in process
     * @return 
     */
    public JSONObject Signin() {
        JSONObject json = new JSONObject();
        PreparedStatement pstmt = null;
        int counter = 0;

        json.appendField("status", "404");
        json.appendField("error", "Unable to Login User !!!");

        do {
            /**
             * check if user account exists in database
             * if not return error message
             */
            if (!openDB()) {
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Unable to Access Database !!!");
                break;
            }

            try {
                /**
                 * Create the SQL statement to check if the user
                 * already has an account.
                 */
                pstmt = this.m_db.m_connection.prepareStatement("SELECT * FROM users WHERE userid = ?");

                // set the corresponding param
                pstmt.setString(1, m_userid);
                // query
                ResultSet rs = pstmt.executeQuery();

                // loop through the result set
                while (rs.next()) {
                    counter++;
                    if (counter > 1) {
                        break;
                    }
                }

                switch (counter) {
                    case 0:;
                    /**
                     * return error message if user tries signin but
                     * account does not exist
                     */
                        json.clear();
                        json.appendField("status", "200");
                        json.appendField("noaccount", "No account was found for this user");
                        break;
                    case 1:
                         /**
                         * when user enters a username without password,
                         * and the user account exists, return message
                         * saying user is allowed to update 
                         * account information
                         */
                        if (m_password.isEmpty()) {
                            json.clear();
                            json.appendField("status", "200");
                            json.appendField("updateaccount", "You may now update YOUR Account");
                            counter++;
                            break;
                        }
                        pstmt.close();
                        pstmt = null;

                        /**
                         * Create an SQL query to check the username
                         * and password supplied by the user.
                         */
                        pstmt = this.m_db.m_connection.prepareStatement("SELECT * FROM users WHERE userid = ? AND password = ?");
                        pstmt.setString(1, m_userid);
                        pstmt.setString(2, m_password);

                        rs = pstmt.executeQuery();

                        // loop through the result set
                        counter = 0;
                        while (rs.next()) {
                            if (counter == 0) {
                                /**
                                 * We found at least one correct username and
                                 * password for the user in the accounts database
                                 * therefore we assume that everything is valid
                                 * and load the success information for the user.
                                 */
                                json.clear();
                                json.appendField("status", "200");
                                json.appendField("message", "success");
                                json.appendField("start_of_calender",
                                        NailzCalendar.get_start_of_calender().getTimeInMillis());
                                json.appendField("cal_session", NailzCalendar.getSession());
                                json.appendField("session", System.currentTimeMillis());
                                json.appendField("id", rs.getInt("id"));
                                json.appendField("userid", rs.getString("userid"));
                                json.appendField("level", rs.getInt("level"));
                            }
                            counter++;
                            if (counter > 1) {
                                break;
                            }
                        }

                        if (counter != 1) {
                            /**
                             * If we later discover that there was more than one
                             * records for the user in the accounts database
                             * for some reason, that is 'counter > 1' then
                             * something is wrong with the database. So even
                             * though we found the correct username and 
                             * password for the user we return an error since 
                             * there is more than one record. 
                             * 
                             * If counter was equal to zero then we did not find
                             * a matching username and password for the user in
                             * the accounts tables and so we return an error to 
                             * the user.
                             */
                            json.clear();
                            json.appendField("status", "404");
                            json.appendField("error", "Unable to login user. invalid [userid or password]");
                            break;
                        }

                        break;
                    default:
                        /**
                         * If there is more than one record in the database
                         * for this user id we do bother to check the 
                         * password and also return an error.
                         */
                        /**
                         * Return that the log-in request
                         * has failed. 
                         */
                        json.clear();
                        json.appendField("status", "404");
                        json.appendField("error", "Unable to login user. Check [userid or password]");
                        break;
                }
                if (counter != 1) {
                    break;
                }
            } catch (SQLException e) {
                /**
                 * If there is an error return that the log-in request
                 * has failed.
                 */
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Unable to Query Database !!!");
                System.err.println(e.getMessage());
            } finally {
            }

        } while (false);

        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e.getMessage());
        }

        /**
         * In any case return the result of the log-in request.
         */
        return json;
    }

    /**
     * allows user to update account info
     * @return
     * @throws SQLException 
     */
    public boolean updateAccount() throws SQLException {
        PreparedStatement pstmt = null;

        /**
         * Create SQL query to update the database for this user.
         */
        pstmt = this.m_db.m_connection.prepareStatement("UPDATE "
                + "users set email = ?, password = ?, fullname = ?,"
                + "allergy=?, phone=? WHERE userid=?");

        pstmt.setString(1, this.m_email);
        pstmt.setString(2, this.m_password);
        pstmt.setString(3, this.m_fullname);
        pstmt.setString(4, this.m_allergy);
        pstmt.setString(5, this.m_phone);
        pstmt.setString(6, this.m_userid);

        pstmt.executeUpdate();

        pstmt.close();

        return true;
    }

    /**
     * allows user to add new account
     * @return
     * @throws SQLException 
     */
    public boolean addAccount() throws SQLException {
        PreparedStatement pstmt = null;

        /**
         * Create an SQL query to add a record to the accounts database.
         */
        pstmt = this.m_db.m_connection.prepareStatement("INSERT "
                + "INTO users (userid, email, password, level, fullname, "
                + "allergy, notify, subscription, phone) VALUES (?,?,?,?,?,?,?,?,?)");

        /**
         * Specify the parameters for the query.
         */
        pstmt.setString(1, this.m_userid);
        pstmt.setString(2, this.m_email);
        pstmt.setString(3, this.m_password);
        pstmt.setInt(4, this.m_level);
        pstmt.setString(5, this.m_fullname);
        pstmt.setString(6, this.m_allergy);
        pstmt.setString(7, this.m_notify);
        pstmt.setString(8, this.m_subscription);
        pstmt.setString(9, this.m_phone);

        pstmt.executeUpdate();

        pstmt.close();

        return true;
    }

    /**
     * allows the user to create an account
     * @return 
     */
    public JSONObject Signup() {
        JSONObject json = new JSONObject();
        PreparedStatement pstmt = null;
        int counter = 0;
        boolean bupdate = false;

        /**
         * Assume an error will be generated by default.
         */
        json.appendField("status", "404");
        json.appendField("error", "Unable to Sign-Up User !!!");

        do {
            if (m_fullname.isEmpty()) {
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Fullname MUST be specified !");
                break;
            }
            if (!openDB()) {
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Unable to Access Database !!!");
                break;
            }

            try {
                /**
                 * Check if the user is already in the accounts table.
                 */
                pstmt = this.m_db.m_connection.prepareStatement("SELECT * FROM users WHERE userid = ?");

                // set the corresponding param
                pstmt.setString(1, m_userid);
                // query
                ResultSet rs = pstmt.executeQuery();

                // loop through the result set
                while (rs.next()) {
                    counter++;
                    if (counter > 1) {
                        break;
                    }
                }

                pstmt.close();
                pstmt = null;

                switch (counter) {
                    case 0:;
                    /**
                     * We did not find the user in the accounts table.
                     */
                        break;
                    case 1:
                        /**
                         * We found the user id so check that 
                         * the password is the same.
                         */
                        if (m_password.equals(rs.getString("password"))) {
                            /**
                             * The password is correct so we only need 
                             * to update the current user record in
                             * the database.
                             */
                            bupdate = true;
                            counter = 0;
                            break;
                        }
                    // FALL THRU ****
                         /**
                         * if an account already exists,
                         * no need to maje a new account
                         */

                    default:
                        /**
                         * The user was found in the database so return 
                         * an error.
                         */
                        json.clear();
                        json.appendField("status", "404");
                        json.appendField("error", "Account already exists. Try another UserID");
                        break;
                }
                if (counter > 0) {
                    break;
                }

                if (bupdate) {
                    // update the EXISTING user account
                    updateAccount();
                } else {
                    // add the NEW user account
                    addAccount();
                }

                // signin the user now
                /**
                 * In any case sign-in the user with the data in
                 * the class. Return the result from the sign-in 
                 * method call.
                 */
                json = Signin();
            } catch (SQLException e) {
                /**
                 * Something is wrong so return an error to the user.
                 */
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Unable to Update Database !!!");

                System.err.println(e.getMessage());
            } finally {
            }

        } while (false);

        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e.getMessage());
        }

        /**
         * Return the JSON object result of the operation
         * to the user.
         */
        return json;
    }

    public UserAccount(long id, String content) {
        /**
         * This is a default safety constructor.
         */
        this.id = id;
        this.content = content;
    }

    public UserAccount(HashMap<String, String> map) {
        /**
         * Initialize the class with the data from the web.
         */
        m_password = map.get("password");
        m_userid = map.get("userid");
        m_email = map.get("email");
        m_fullname = map.get("fullname");
        m__app_submitter = map.get("_app_submitter");
        m_allergy = map.get("allergy");
        m_notify = map.get("notify");
        m_phone = map.get("phone");
        m_session = Integer.valueOf(map.get("session"));
        m_level = (map.get("level").isEmpty() ? 9 : Integer.valueOf(map.get("level")));
        if (m_level == 0) {
            m_level = 9;
        }
        m_uuid = Integer.valueOf(map.get("uuid"));
        m_subscription = map.get("subscription");
    }

    /**
     * set the user id for the user account
     * @param id
     * @return 
     */
    public UserAccount setId(long id) {
        this.id = id;

        return this;
    }

    /**
     * set content for the user account
     * @param content
     * @return 
     */
    public UserAccount setContent(String content) {
        this.content = content;

        return this;
    }

     /**
     * Get the user id for the user account
     * @param id
     * @return 
     */
    public long getId() {
        return id;
    }

      /**
     * get content for the user account
     * @return 
     */
    public String getContent() {
        return content;
    }

}
