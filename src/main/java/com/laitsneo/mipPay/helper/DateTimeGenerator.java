package com.laitsneo.mipPay.helper;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

@Service
public class DateTimeGenerator {

    public String dateAndTimeGenerator(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy, EEE, hh:mm:ss a z");
        String formattedDate = formatter.format(zonedDateTime);
        return formattedDate;
    }

    public String dateAndTimeGenerator1(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        String formattedDate = formatter.format(zonedDateTime);
        return formattedDate;
    }

    public String fetchDate(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = formatter.format(zonedDateTime);
        return formattedDate;
    }

    public String fetchTime(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a z");
        String formattedDate = formatter.format(zonedDateTime);
        return formattedDate;
    }

    public String getCurrentTimeInIST(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date now = new Date();
        return dateFormat.format(now);
    }
}
