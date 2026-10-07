package com.tup.programacion3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        SpringApplication.run(Main.class, args);
    }
}