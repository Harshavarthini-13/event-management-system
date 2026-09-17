package com.eventmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Event Management Backend.
 *
 * @SpringBootApplication is shorthand for:
 *   - @Configuration       (this class is a Spring config source)
 *   - @EnableAutoConfiguration (Spring Boot auto-wires beans)
 *   - @ComponentScan       (scans com.eventmgmt and sub-packages)
 */
@SpringBootApplication
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }
}