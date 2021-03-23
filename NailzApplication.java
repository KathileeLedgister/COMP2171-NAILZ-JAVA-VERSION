package com.app.nailz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NailzApplication {

    public static void main(String[] args) {
        {
            (new Sqllitedb()).db_init().close();
            //NailzCalendar.updateSession();
        }

        SpringApplication.run(NailzApplication.class, args);
    }

}
