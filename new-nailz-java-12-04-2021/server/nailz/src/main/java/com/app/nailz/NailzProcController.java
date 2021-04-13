
package com.app.nailz;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 *
 * @authors 
 * Kathilee Ledgister -  620121618
 * Shanee Barnes - 620076360
 * Jordan Wilson -  620119365
 * Raman Lewis - 620117907
 */

/**
 * To use push notification we create a "google" firebase project
 *
 * @author vledgister
 */
@RestController
public class NailzProcController {

    private static final String template = "Hello, %s!";
    private static final String error_template = "%s : Error Occurred";
    private final AtomicLong counter = new AtomicLong();

    /**
     * Parse the data sent from the web client into a hashmap structure
     * key-value pairs.
     *
     * @param params
     * @return
     */
    HashMap<String, String> parseQueryString(String params) {
        HashMap<String, String> map = new HashMap<String, String>();
        String qArr[] = params.split("&");
        for (int i = 0; i < qArr.length; i++) {
            String pair[] = qArr[i].split("=");
            String result = "";
            try {
                if (pair.length > 1) {
                    result = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                }
            } catch (UnsupportedEncodingException e) {
                // not going to happen - value came from JDK's own StandardCharsets
                result = pair[1];
            }
            map.put(pair[0], result);
        }
        return map;
    }

    /**
     * when the user selects sign-in button that request is caught here and
     * processed. if the user id does not already have an account it returns a
     * value indicating that the user should signup first. the signup screen is
     * then displayed
     *
     * @param signinParams
     * @return
     */

    @PostMapping(value = "/r1/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject Signin(@RequestBody String signinParams) {
        HashMap<String, String> map = parseQueryString(signinParams);
        UserAccount acc = null;
        JSONObject json = new JSONObject();

        json.appendField("status", "404");
        json.appendField("error", "Unable to Login User !!!");

        // check if the account is valid
        // must have userid and password
        do {
            if (map.get("password").isEmpty() || map.get("userid").isEmpty()) {
                break;
            }

            // if account not valid return signup request
            //create a new account object and initialize with 
            //the values from the web parameter            acc = new UserAccount(map);
            json.clear();
            //call the signin method to signin the user.
            //returns the json result of processing the request.
            //this could be an error
            json = acc.Signin();
        } while (false);

        if (acc != null) {
            acc.closedb();
        }

        /**
         * Check the return status from the UserAccount class. If there was an
         * error status send it back to the user.
         */
        if (acc == null || json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        /**
         * There was no error so return the JSON object to the user as a JSON
         * string object.
         */
        return json;
    }

    /*
    * if the user enters a user id that does not exist
    * or ennters a user id without a password the signup screen is displayed.
    * when the user fills out the screen and clicks the signup button 
    * the request is sent to this handler. 
    * this is used to create a new account or update an existing account
     */
    @PostMapping(value = "/r1/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject Signup(@RequestBody String signinParams) {
        HashMap<String, String> map = parseQueryString(signinParams);
        UserAccount acc = null;
        JSONObject json = new JSONObject();

        json.appendField("status", "404");
        json.appendField("error", "Unable to Sign-Up User !!!");

        // check if the account is valid
        // must have userid and password
        do {
            if (map.get("password").isEmpty() || map.get("userid").isEmpty()) {
                break;
            }

            //create a new account object and initialize with 
            //the values from the web parameter
            //signup creates or updates the user account            acc = new UserAccount(map);
            json.clear();
            //call the signup methid to signup with the user data
            json = acc.Signup();
        } while (false);

        if (acc != null) {
            acc.closedb();
        }

        /**
         * Check the return status from the UserAccount class. If there was an
         * error status send it back to the user.
         */
        if (acc == null || json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        /**
         * There was no error so return the JSON object to the user as a JSON
         * string object.
         */
        return json;
    }

    //sends back entire appointments list for the calendar starting from
    //the beginning of the week
    /**
     * Every time a user logs-in a request is sent for the entire calender so
     * that it can be displayed on the screen. this is the handler for the
     * calender refresh.
     *
     * @param map
     * @return
     */
    @GetMapping(value = "/r1/calender_all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject calender_all(@RequestParam HashMap<String, String> map) { //calendarParams) {
        UserAccount acc = null;
        JSONObject json = new JSONObject();
        boolean do_fetch = false;

        String pg_id = map.get("pg_id");
        long cal_session = Long.valueOf(map.get("cal_session"));

        do {
            /**
             * If the browser did not send a calendar session we can't handle
             * this request.
             */
            if (map.get("cal_session").isEmpty()) {
                json.appendField("status", "404");
                json.appendField("error", "No [calender_session] specified");
                break;
            }

            /**
             * If the calendar session is zero or the user's calendar session
             * does not match that of the server then this indicates that the
             * cache is out of sync and needs to be refreshed. Send true to
             * ensure that 'fetch_calendar' requeries the appointments database.
             */
            if (NailzCalendar.getSession() == 0 || cal_session != NailzCalendar.getSession()) {
                do_fetch = true;
            }

            /**
             * Return either the cached appointments or the newly queried
             * appointments set.
             */
            NailzCalendar.fetch_calendar(do_fetch);

            /**
             * Create JSON object as the response.
             */
            json.appendField("status", "200");
            json.appendField("cal_session", NailzCalendar.getSession());
            json.appendField("start_of_cal", NailzCalendar.get_start_of_calender().getTimeInMillis());
            json.appendField("cal_block", NailzCalendar.get_calendar());
        } while (false);

        /**
         * Check the return status from the UserAccount class. If there was an
         * error status send it back to the user.
         */
        if (json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        /**
         * There was no error so return the JSON object to the user as a JSON
         * string object.
         */
        return json;
    }

    /**
     * when the user selects an entry on the calendar screen to add or delete,
     * an appointment, this is the handler that processes the request
     *
     * @param map
     * @return
     */
    @GetMapping(value = "/r1//cal_set_avail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject cal_set_avail(@RequestParam HashMap<String, String> map) {
        JSONObject json = new JSONObject();
        String serrors = "";
        NailzCalendarManager calmgr = null;

        int uuid = Integer.valueOf(map.get("uuid"));
        String status = map.get("status");
        String alergy = map.get("alergy");
        String notify = map.get("notify");
        long cal_session = Long.valueOf(map.get("cal_session"));
        long user_session = Long.valueOf(map.get("user_session"));
        int level = Integer.valueOf(map.get("level"));
        long slot = Long.valueOf(map.get("slot"));

        do {
            /**
             * Verify that he data send by the web has all the required
             * properties.
             */
            if (uuid == 0) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No user KEYID specified";
            }
            if (status.isEmpty()) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No operation specified";
            }
            if (map.get("user_session").isEmpty()) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No [user_session] specified";
            }
            if (map.get("level").isEmpty()) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No [user_level] specified";
            }
            if (map.get("slot").isEmpty()) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No [Time Slot] specified";
            }
            if (map.get("cal_session").isEmpty()) {
                serrors += (!serrors.isEmpty() ? "<br>" : "") + "No [cal_session] specified";
            }
            if (alergy.isEmpty()) {
            }
            if (notify.isEmpty()) {
            }

            //============================================
            /**
             * If something was wrong return an error.
             */
            if (!serrors.isEmpty()) {
                json.appendField("status", "404");
                json.appendField("error", serrors);
                break;
            }

            // if account not valid return signup request
            /**
             * Create a new NailzCalendarManager class initialized with the data
             * from the web.
             */
            calmgr = new NailzCalendarManager(map);
            json.clear();
            /**
             * Get the result from the requested add or delete operation as a
             * JSON object.
             */
            json = calmgr.cal_set_avail();
        } while (false);

        if (calmgr != null) {
            calmgr.closedb();
            calmgr = null;
        }
        /**
         * Check the return status from the UserAccount class. If there was an
         * error status send it back to the user.
         */
        if (json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        /**
         * There was no error so return the JSON object to the user as a JSON
         * string object.
         */
        return json;
    }

}
