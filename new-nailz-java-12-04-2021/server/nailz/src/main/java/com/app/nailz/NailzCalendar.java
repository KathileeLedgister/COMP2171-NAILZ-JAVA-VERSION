
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
 * @authors 
 * Kathilee Ledgister -  620121618
 * Shanee Barnes - 620076360
 * Jordan Wilson -  620119365
 * Raman Lewis - 620117907
 */
public class NailzCalendar {

    private static long calender_session = 0;
    
    /**
     * Cache of the last successfully queried calendar.
     */
    private static JSONArray calender_appointments = new JSONArray();
    private static Sqllitedb m_db = null;

    /**
     * Return the current calendar session number.
     * @return
     */
    public static long getSession() {
        return calender_session;
    }

    /**
     * Sets the calendar session number to the current
     * time.
     */
    public synchronized static void updateSession() {
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calender_session = System.currentTimeMillis();
    }

    /**
     * Clear the calendar session so that we know the caledar
     * needs to be updated.
     */
    public synchronized static void clearSession() {
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calender_session = 0;
    }

    /**
     * Get the date-time of the start of the calendar month.
     * This time is in UTC format since the server could be 
     * located anywhere in the world. The browser must convert this
     * time to local time.
     * @return 
     */
    public static Calendar get_start_of_calender() {

        Clock cl = Clock.systemUTC();
        ZonedDateTime zd1 = ZonedDateTime.now(cl);
        ZonedDateTime zd = zd1.minusDays(zd1.getDayOfWeek().getValue() % 7).minusHours(zd1.getHour())
                .minusMinutes(zd1.getMinute()).minusSeconds(zd1.getSecond()).minusNanos(zd1.getNano())
                .plusHours(7);
        Calendar start_of_calender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start_of_calender.set(zd.getYear(), zd.getMonthValue() - 1, zd.getDayOfMonth(), zd.getHour(), 0, 0);
        start_of_calender.setTimeInMillis(((long) (start_of_calender.getTimeInMillis() / 1000)) * 1000);
        return start_of_calender;
    }

    /**
     * open database to allow for user queries
     *
     * @return
     */
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

    /**
     * Get current instance of the calendar cache.
     * This contains all the appointments for the 
     * current calendar session.
     * @return
     */
    public static JSONArray get_calendar() {

        return calender_appointments;
    }

    /**
     * Load all the appointments entered into the database by customers into a
     * JSON object and send them to the browser to display on the calendar. Only
     * appointments made for the current appointments month session is retrieved
     * from the database.
     *
     * @param do_reload
     * @return
     */
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

                    /**
                     * SQL query to load the current appointments.
                     */
                    pstmt = m_db.m_connection.prepareStatement(""
                            + "SELECT appointments.*, email, fullname, allergy, phone "
                            + "FROM appointments INNER JOIN users ON "
                            + "appointments.a_uuid=users.id WHERE a_slot >= ?");
                    // set the corresponding param
                    pstmt.setLong(1, start_of_calender.getTimeInMillis());
                    // query
                    ResultSet rs = pstmt.executeQuery();

                    /**
                     * Convert the JDBC data set retrieved from the database
                     * into a JSON array to be sent to the browser.
                     */
                    JSONArray appointmentsArray = m_db.convert(rs);
                    /**
                     * Cache this new data set into the calendar class 
                     * property so that other threads can just use it
                     * without requerying the database.
                     */
                    calender_appointments = appointmentsArray;
                    /**
                     * Notify the calendar class that the data has been updated.
                     */
                    updateSession();
                }

                bres = true;
            } catch (SQLException e) {
                /**
                 * If the was an error fetching the data report it.
                 */
                System.out.println("Reloading Calender. Failed");
                System.err.println(e.getMessage());

                /**
                 * Mark that the calendar needs to be refreshed. If another
                 * thread tries to access the calendar it will see the zero
                 * value and know to requery the appointments database.
                 */
                calender_session = 0;
                /**
                 * Empty the cache since there was a query error
                 */
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

        /**
         * Return true if the query was successful otherwise
         * return false.
         */
        return bres;
    }
}
