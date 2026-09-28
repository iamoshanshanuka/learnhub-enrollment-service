package com.ijse.learnhub.enrollmentservice.client;

import com.ijse.learnhub.enrollmentservice.exception.DownstreamUnavailableException;
import com.ijse.learnhub.enrollmentservice.exception.ResourceNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Calls the course-service through Eureka using its logical service name
 * (inter-service communication in the microservice platform).
 */
@Component
public class CourseClient {

    private final RestClient restClient;

    public CourseClient(RestClient.Builder builder) {
        this.restClient = builder.clone().baseUrl("http://course-service").build();
    }

    public Map<String, Object> getCourse(Long id) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/courses/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() { });
            if (body == null) {
                throw new ResourceNotFoundException("Course not found with id " + id);
            }
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Course not found with id " + id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            // covers connection errors, 5xx answers and "No instances available" from the load balancer
            throw new DownstreamUnavailableException(
                    "The course service is not reachable right now. Try again in a moment.", e);
        }
    }
}
