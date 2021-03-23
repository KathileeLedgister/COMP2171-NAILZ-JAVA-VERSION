/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.nailz;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Kathilee Ledgister
 *         Shanee Barnes 
 *         Jordan Wilson 
 *         Raman Lewis
 */
public class NailzCalendar {

    private static long calender_session = 0;
    private static JSONArray calender_appointments = new JSONArray();
    private static Sqllitedb m_db = null;

    public static long getSession() {
        return calender_session;
    }

    public synchronized static void updateSession() {
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calender_session = System.currentTimeMillis();
    }

    /**
     * set the calender_session to 0, indicating that the calendar
     * is out of sync and needs to be reloaded.
     */
    public synchronized static void clearSession() {
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calender_session = 0;
    }
    
    /**
     * gets the first week of the year for the calendar.
     * calendar times are managed in UTC and translated to local time
     * in the web browser
     * when the user makes an appointment that time is converted back to UTC 
     * time and sent to the server
     * @return 
     */
    public static Calendar get_start_of_calender() {
        //ZonedDateTime zd = java.time.ZonedDateTime.now();
        //System.out.println(zd.toString());
        Clock cl = Clock.systemUTC();
        ZonedDateTime zd1 = ZonedDateTime.now(cl);
        ZonedDateTime zd = zd1.minusDays(zd1.getDayOfWeek().getValue() % 7).minusHours(zd1.getHour())
                .minusMinutes(zd1.getMinute()).minusSeconds(zd1.getSecond()).minusNanos(zd1.getNano())
                .plusHours(7);
        //System.out.println("ZonedDateTime : " + zd); 
        Calendar start_of_calender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start_of_calender.set(zd.getYear(), zd.getMonthValue() - 1, zd.getDayOfMonth(), zd.getHour(), 0, 0);
        //System.out.println("ZonedDateTime : " + start_of_calender.getTime()); 
        return start_of_calender;
    }

    private static boolean openDB() {
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

    public static JSONArray get_calendar() {

        return calender_appointments;
    }

    public synchronized static boolean fetch_calendar(boolean do_reload) {
        Calendar start_of_calender = get_start_of_calender();
        boolean bres = false;

        PreparedStatement pstmt = null;

        do {
            try {

                if (do_reload) {
                    if (m_db == null || m_db.m_connection == null) {
                        if (!openDB()) {
                            break;
                        }
                    }

                    pstmt = m_db.m_connection.prepareStatement(""
                            + "SELECT appointments.*, email, fullname, allergy, phone "
                            + "FROM appointments INNER JOIN users ON "
                            + "appointments.a_uuid=users.id WHERE a_slot >= ?");
                    // set the corresponding param
                    pstmt.setLong(1, start_of_calender.getTimeInMillis());
                    // query
                    ResultSet rs = pstmt.executeQuery();

                    JSONArray appointmentsArray = m_db.convert(rs);
                    calender_appointments = appointmentsArray;
                    updateSession();
                }

                bres = true;
            } catch (SQLException e) {
                System.out.println("Reloading Calender. Failed");
                System.err.println(e.getMessage());

                calender_session = 0;
                calender_appointments = new JSONArray();
            } finally {
            }

        } while (false);

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
            }
        }

        return bres;
    }
}
