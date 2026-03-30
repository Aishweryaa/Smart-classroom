package com.smartclassroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartClassroomApplication - Entry point for the Spring Boot application.
 *
 * @SpringBootApplication enables: - @Configuration : beans defined in this
 * class are registered - @EnableAutoConfiguration : Spring Boot auto-configures
 * based on classpath - @ComponentScan : scans com.smartclassroom and
 * sub-packages
 */
@SpringBootApplication

public class SmartClassroomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartClassroomApplication.class, args);
        System.out.println("\n✅  Smart Classroom started at http://localhost:8080\n");
    }
}
