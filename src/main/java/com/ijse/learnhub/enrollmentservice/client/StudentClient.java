package com.ijse.learnhub.enrollmentservice.client;

import com.ijse.learnhub.enrollmentservice.exception.DownstreamUnavailableException;
import com.ijse.learnhub.enrollmentservice.exception.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Calls the student-service through Eureka using its logical service name
 * (inter-service communication in the microservice platform).
 */
@Component
public class StudentClient {

    private final RestClient restClient;

    public StudentClient(RestClient.Builder builder) {
        this.restClient = builder.clone().baseUrl("http://student-service").build();
    }

    public Map<String, Object> getStudent(Long id) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/students/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() { });
            if (body == null) {
                throw new ResourceNotFoundException("Student not found with id " + id);
            }
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Student not found with id " + id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            // covers connection errors, 5xx answers and "No instances available" from the load balancer
            throw new DownstreamUnavailableException(
                    "The student service is not reachable right now. Try again in a moment.", e);
        }
    }
}
