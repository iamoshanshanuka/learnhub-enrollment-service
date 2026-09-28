package com.ijse.learnhub.enrollmentservice.dto;

import com.ijse.learnhub.enrollmentservice.document.Enrollment.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EnrollmentDtos {

    public record EnrollmentRequest(
            @NotNull(message = "Choose a student") Long studentId,
            @NotNull(message = "Choose a course") Long courseId
    ) {}

    public record StatusUpdateRequest(
            @NotNull(message = "Status is required") EnrollmentStatus status
    ) {}

    public record EnrollmentResponse(
            String id,
            Long studentId,
            String studentName,
            Long courseId,
            String courseTitle,
            EnrollmentStatus status,
            LocalDateTime enrollmentDate
    ) {}

    /** Business rules delivered by the Config Server, shown in the frontend footer. */
    public record RulesResponse(
            String platformName,
            int maxActivePerStudent,
            boolean activityLogEnabled
    ) {}
}
