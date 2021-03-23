/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author Kathilee Ledgister
 *         Shanee Barnes 
 *         Jordan Wilson 
 *         Raman Lewis
 */
/**
 * To use push notification we create a "google" firebase project
 *
 */
@RestController
public class NailzProcController {

    private static final String template = "Hello, %s!";
    private static final String error_template = "%s : Error Occurred";
    private final AtomicLong counter = new AtomicLong();

    /**
     * parse web parameters to a hashmap of key/value pairs
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
     * when the user selects sign-in button that
     * request is caught here and processed. 
     * if the user id does not already have an account
     * it returns a value indicating that the user should signup first.
     * the signup screen is then displayed
     * @param signinParams
     * @return 
     */
    @PostMapping(value = "/r1/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    //public UserAccount Signin(@RequestBody String signinParams) {
    public JSONObject Signin(@RequestBody String signinParams) {
        HashMap<String, String> map = parseQueryString(signinParams);
        UserAccount acc = null;
        JSONObject json = new JSONObject();

        json.appendField("status", "404");
        json.appendField("error", "Unable to Login User !!!");

        // chech if the account is valid
        // must have userid and password
        do {
            if (map.get("password").isEmpty() || map.get("userid").isEmpty()) {
                break;
            }

            // if account not valid return signup request
            //create a new account object and initialize with 
            //the values from the web parameter
            acc = new UserAccount(map);
            json.clear();
            //call the signin method to signin the user.
            //returns the json result of processing the request.
            //this could be an error
            json = acc.Signin();
        } while (false);

        if (acc != null) {
            acc.closedb();
        }

        if (acc == null || json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        return json;
    }

    /*
    if the user enters a user id that does not exist
    or ennters a user id without a password the signup screen is displayed.
    when the user fills out the screen and clicks the signup button 
    the request is sent to this handler. 
    this is used to create a new account or update an existing account
    */
    @PostMapping(value = "/r1/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject Signup(@RequestBody String signinParams) {
        HashMap<String, String> map = parseQueryString(signinParams);
        UserAccount acc = null;
        JSONObject json = new JSONObject();

        json.appendField("status", "404");
        json.appendField("error", "Unable to Sign-Up User !!!");

        // chech if the account is valid
        // must have userid and password
        do {
            if (map.get("password").isEmpty() || map.get("userid").isEmpty()) {
                break;
            }

            //create a new account object and initialize with 
            //the values from the web parameter
            //signup creates or updates the user account
            acc = new UserAccount(map);
            json.clear();
            //call the signup methid to signup with the user data
            json = acc.Signup();
        } while (false);

        if (acc != null) {
            acc.closedb();
        }

        if (acc == null || json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        return json;
    }

    //sends back entire appointments list for the calendar starting from
    //the beginning of the week
    /**
     * every time a user logs in a request is sent for the entire
     * calender so that it can be displayed on the screen. 
     * this is the handler for the calender refresh. 
     * @param map
     * @return 
     */
    @GetMapping(value = "/r1/calender_all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject calender_all(@RequestParam HashMap<String,String>  map){ //calendarParams) {
        UserAccount acc = null;
        JSONObject json = new JSONObject();
        boolean do_fetch = false;

        String pg_id = map.get("pg_id");
        long cal_session = Long.valueOf(map.get("cal_session"));

        do {
            if (map.get("cal_session").isEmpty()) {
                json.appendField("status", "404");
                json.appendField("error", "No [calender_session] specified");
                break;
            }

            //the calendar is managed as a static property.  
            //this acts as a cache where all requests get the same calendar.
            //when the appointments is updated the calendar session is set to 0
            //which triggers reloading of the calendar
            if (NailzCalendar.getSession() == 0 || cal_session != NailzCalendar.getSession()) {
                do_fetch = true;//refresh the calendar cache
            }

            //call the calendar reload routine
            NailzCalendar.fetch_calendar(do_fetch);
            
            //return these values to the web browser
            json.appendField("status", "200");
            json.appendField("cal_session", NailzCalendar.getSession());
            json.appendField("start_of_cal", NailzCalendar.get_start_of_calender().getTimeInMillis());
            //retrieve the cached version of the appointments calendar
            json.appendField("cal_block", NailzCalendar.get_calendar());
        } while (false);

        if (json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        //return the json object data to the web browser
        return json;
    }

    /**
     * when the user selects an entry on the calendar screen to add or delete,
     * an appointment, this is the handler that processes the request
     * @param map
     * @return 
     */
    @GetMapping(value = "/r1//cal_set_avail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject get_set_appointment(@RequestParam HashMap<String,String>  map) {
        JSONObject json = new JSONObject();
        String serrors = "";
        NailzCalendarManager calmgr = null;

        //get the appointment details from the web parameters
        int uuid = Integer.valueOf(map.get("uuid"));
        String status = map.get("status");
        String alergy = map.get("alergy");
        String notify = map.get("notify");
        long cal_session = Long.valueOf(map.get("cal_session"));
        long user_session = Long.valueOf(map.get("user_session"));
        int level = Integer.valueOf(map.get("level"));
        long slot = Long.valueOf(map.get("slot"));

        do {

            //verify that the appointments details is filled out properly
            //return error if there are missing fields
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
            if (!serrors.isEmpty()) {
                json.appendField("status", "404");
                json.appendField("error", serrors);
                break;
            }

            /**
             * create an instance of the appointments calendar manager
             * and initialize it with the values from the web parameters
             * we have already checked up above that they exist and are valid.
             * 
             */
            calmgr = new NailzCalendarManager(map);
            json.clear();
            //get or set the appointment based on the data in the web request
            json = calmgr.get_set_appointment();
        } while (false);

        if(calmgr != null){
            calmgr.closedb();
            calmgr= null;
        }
        if (json.getAsString("status").equals("404")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, json.toJSONString());
        }

        return json;
    }

}
