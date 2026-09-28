package com.ijse.learnhub.enrollmentservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * A load-balanced RestClient builder: a URL such as http://student-service/api/students/5 is
 * resolved through Eureka and spread across every registered instance of that service.
 * Short timeouts make sure a slow downstream service can never hang enrollment requests.
 */
@Configuration
public class RestClientConfig {

    /**
     * A PLAIN (not load-balanced) builder. Spring Cloud LoadBalancer attaches its interceptor to
     * every RestClient.Builder it can find, including the one the Eureka client itself uses to
     * talk to the Eureka servers -- that turns Eureka's own registry calls into a load-balanced
     * "resolve platform-a.learnhub.internal as a service" call, which circularly needs the very
     * eurekaClient bean that is still being created. Marking this one @Primary means Eureka's own
     * auto-configuration (which asks for RestClient.Builder with no qualifier) gets THIS plain
     * builder, while our own clients below ask for the load-balanced one by name.
     */
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @Lazy
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(requestFactory);
    }
}
