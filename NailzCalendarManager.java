/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.nailz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Kathilee Ledgister
 *         Shanee Barnes 
 *         Jordan Wilson 
 *         Raman Lewis
 */
public class NailzCalendarManager {

    private static Sqllitedb m_db = null;
    int m_uuid = -1;
    String m_status = "";
    long m_user_session = 0;
    int m_level = 10;
    long m_slot = 0;
    long m_cal_session = 0;
    String m_alergy = "";
    String m_notify = "";
    String m_service = "";

    public NailzCalendarManager(HashMap<String, String> map) {
        m_uuid = (map.get("uuid").isEmpty() ? -1 : Integer.valueOf(map.get("uuid")));
        m_status = map.get("status");
        m_user_session = (map.get("user_session").isEmpty() ? -1 : Long.valueOf(map.get("user_session")));
        m_level = (map.get("level").isEmpty() ? 9 : Integer.valueOf(map.get("level")));
        m_slot = (map.get("slot").isEmpty() ? -1 : Long.valueOf(map.get("slot")));
        m_cal_session = (map.get("cal_session").isEmpty() ? -1 : Long.valueOf(map.get("cal_session")));
        m_alergy = map.get("alergy");
        m_notify = map.get("notify");
        m_service = map.get("service");
    }

    public NailzCalendarManager closedb() {
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

    public JSONObject get_set_appointment() {
        JSONObject json = new JSONObject();
        PreparedStatement pstmt = null;
        int counter = 0;
        boolean bupdate = false;
        long customer_uuid = -1;
        boolean berror = false;

        json.appendField("status", "404");
        json.appendField("error", "Unable to Process Calendar !");

        do {

            if (m_db == null || m_db.m_connection == null) {
                if (!openDB()) {
                    break;
                }
            }

            try {

                switch (m_notify) {
                    case "Y":

                        pstmt = m_db.m_connection.prepareStatement(""
                                + "SELECT a_uuid FROM appointments WHERE a_slot = ?");
                        // set the corresponding param
                        pstmt.setLong(1, m_slot);
                        // query
                        ResultSet rs = pstmt.executeQuery();

                        if (!rs.next()) {
                            json.clear();
                            json.appendField("status", "404");
                            json.appendField("error", "Customer for this appointment NOT found");
                            berror = true;
                            break;
                        }

                        customer_uuid = rs.getLong("a_uuid");

                        pstmt.close();
                        pstmt = null;
                        break;

                    default:
                        break;
                }
                if (berror) {
                    break;
                }

                switch (m_status) {
                    case "A":
                        pstmt = m_db.m_connection.prepareStatement(""
                                + "INSERT INTO appointments (a_slot, a_uuid, a_service, a_status) VALUES (?,?,?,?)");
                        // set the corresponding param
                        pstmt.setLong(1, m_slot);
                        pstmt.setLong(2, m_uuid);
                        pstmt.setString(3, m_service);
                        if (m_level < 0 || m_level > 5) {
                            pstmt.setString(4, "U");//  used - regular customer
                        } else {
                            pstmt.setString(4, "N"); //  NOT available
                        }
                        break;

                    default:
                        if (m_level >= 0 && m_level < 6) {
                            pstmt = m_db.m_connection.prepareStatement(""
                                    + "DELETE FROM appointments WHERE a_slot = ?");
                            // set the corresponding param
                            pstmt.setLong(1, m_slot);
                        } else {
                            pstmt = m_db.m_connection.prepareStatement(""
                                    + "DELETE FROM appointments WHERE a_slot = ? and a_uuid = ?");
                            // set the corresponding param
                            pstmt.setLong(1, m_slot);
                            pstmt.setLong(2, m_uuid);
                        }
                        break;
                }
                if (berror) {
                    break;
                }

                // update
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;

                NailzCalendar.clearSession();

                if (m_notify.equals("Y")) {
                    json.clear();
                    json.appendField("status", "200");
                    json.appendField("message", "success");
                    break;
                }

                /**
                 * Get substription and send notification
                 */
                /*
                pstmt = m_db.m_connection.prepareStatement(""
                        + "SELECT userid, subscription FROM users WHERE id=?");
                pstmt.setLong(1, customer_uuid);
                ResultSet rs = pstmt.executeQuery();

                if (rs.getRow() == 1 && !rs.getString("subscription").isEmpty()) {
                }
                 */
                
                json.clear();
                json.appendField("status", "200");
                json.appendField("message", "success");
            } catch (SQLException e) {
                System.out.println("Updating Calender. Failed");
                System.err.println(e.getMessage());
            } finally {
            }

        } while (false);
        return json;
    }

}
