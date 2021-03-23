/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author Kathilee Ledgister
 *         Shanee Barnes 
 *         Jordan Wilson 
 *         Raman Lewis
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

    public UserAccount closedb() {
        if (m_db != null) {
            m_db.close();
            m_db = null;
        }
        return this;
    }

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

    public JSONObject Signin() {
        JSONObject json = new JSONObject();
        PreparedStatement pstmt = null;
        int counter = 0;

        json.appendField("status", "404");
        json.appendField("error", "Unable to Login User !!!");

        do {
            if (!openDB()) {
                json.clear();
                json.appendField("status", "404");
                json.appendField("error", "Unable to Access Database !!!");
                break;
            }

            try {
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
                        json.clear();
                        json.appendField("status", "200");
                        json.appendField("noaccount", "No account was found for this user");
                        break;
                    case 1:
                        if (m_password.isEmpty()) {
                            json.clear();
                            json.appendField("status", "200");
                            json.appendField("updateaccount", "You may now update YOUR Account");
                            counter++;
                            break;
                        }
                        pstmt.close();
                        pstmt = null;

                        pstmt = this.m_db.m_connection.prepareStatement("SELECT * FROM users WHERE userid = ? AND password = ?");
                        pstmt.setString(1, m_userid);
                        pstmt.setString(2, m_password);

                        rs = pstmt.executeQuery();

                        // loop through the result set
                        counter = 0;
                        while (rs.next()) {
                            if (counter == 0) {
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
                            json.clear();
                            json.appendField("status", "404");
                            json.appendField("error", "Unable to login user. invalid [userid or password]");
                            break;
                        }

                        break;
                    default:
                        json.clear();
                        json.appendField("status", "404");
                        json.appendField("error", "Unable to login user. Check [userid or password]");
                        break;
                }
                if (counter != 1) {
                    break;
                }
            } catch (SQLException e) {
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

        return json;
    }

    public boolean updateAccount() throws SQLException {
        PreparedStatement pstmt = null;

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

    public boolean addAccount() throws SQLException {
        PreparedStatement pstmt = null;

        pstmt = this.m_db.m_connection.prepareStatement("INSERT "
                + "INTO users (userid, email, password, level, fullname, "
                + "allergy, notify, subscription, phone) VALUES (?,?,?,?,?,?,?,?,?)");

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

    public JSONObject Signup() {
        JSONObject json = new JSONObject();
        PreparedStatement pstmt = null;
        int counter = 0;
        boolean bupdate = false;

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
                        break;
                    case 1:
                        if (m_password.equals(rs.getString("password"))) {
                            bupdate = true;
                            counter = 0;
                            break;
                        }
                    // FALL THRU ****

                    default:
                        json.clear();
                        json.appendField("status", "404");
                        json.appendField("error", "Account already exists. Try another UserID");
                        break;
                }
                if (counter > 0) {
                    break;
                }

                if (bupdate) {
                    // update an EXISTING user account
                    updateAccount();
                } else {
                    // add a NEW user account
                    addAccount();
                }

                // signin the user now
                json = Signin();
            } catch (SQLException e) {
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

        return json;
    }

    public UserAccount(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public UserAccount(HashMap<String, String> map) {
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

    public UserAccount setId(long id) {
        this.id = id;

        return this;
    }

    public UserAccount setContent(String content) {
        this.content = content;

        return this;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

}
