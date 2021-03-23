/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.nailz;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author vledgister
 */
public class Md5Hash {

    public Md5Hash() {

    }

    public static String getMd5(String input) {
        String hashtext = input;
        try {

            // Static getInstance method is called with hashing MD5 
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest 
            //  of an input digest() return array of byte 
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value 
            hashtext = no.toString();
            /*hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = hashtext; 
            }*/
        } // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }

        return hashtext;
    }

}
