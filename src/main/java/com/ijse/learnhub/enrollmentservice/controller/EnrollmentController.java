package com.ijse.learnhub.enrollmentservice.controller;

import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.EnrollmentRequest;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.EnrollmentResponse;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.RulesResponse;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.StatusUpdateRequest;
import com.ijse.learnhub.enrollmentservice.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<EnrollmentResponse> findAll() {
        return service.findAll();
    }

    /** Business rules that were delivered to this service by the Config Server. */
    @GetMapping("/rules")
    public RulesResponse rules() {
        return service.rules();
    }

    /** Latest enrollment events, read back from Firestore. */
    @GetMapping("/audit")
    public List<Map<String, Object>> audit(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.recentActivity(safeLimit);
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> findByStudent(@PathVariable Long studentId) {
        return service.findByStudent(studentId);
    }

    @GetMapping("/{id}")
    public EnrollmentResponse findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public EnrollmentResponse updateStatus(@PathVariable String id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
