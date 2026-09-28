package com.ijse.learnhub.enrollmentservice.exception;

/** A service this one depends on (student-service / course-service) could not be reached. */
public class DownstreamUnavailableException extends RuntimeException {
    public DownstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
