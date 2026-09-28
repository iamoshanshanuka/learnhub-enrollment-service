package com.ijse.learnhub.enrollmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LearnHub - Enrollment microservice.
 * Data: MongoDB (non-relational). Activity log: Firestore. Calls student-service and
 * course-service through Eureka (load-balanced RestClient) to validate every enrollment.
 */
@SpringBootApplication
public class EnrollmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnrollmentServiceApplication.class, args);
    }
}
