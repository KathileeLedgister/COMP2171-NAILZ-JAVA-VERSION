/**
 *
 * @authors 
 * Kathilee Ledgister -  620121618
 * Shanee Barnes - 620076360
 * Jordan Wilson -  620119365
 * Raman Lewis - 620117907
 */
package com.app.nailz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NailzApplication {

    /**
     * This is where the program starts.
     * @param args 
     */
    public static void main(String[] args) {
        {
            /**
             * On start up try to create and initialize
             * the database in case non exists.
             */
            (new Sqllitedb()).db_init().close();
        }

        /**
         * Run the spring framework.
         */
        SpringApplication.run(NailzApplication.class, args);
    }

}
