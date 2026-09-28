package com.ijse.learnhub.enrollmentservice.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Non-relational document stored in MongoDB. */
@Document(collection = "enrollments")
public class Enrollment {

    @Id
    private String id;

    private Long studentId;
    private String studentName;

    private Long courseId;
    private String courseTitle;

    private EnrollmentStatus status;

    private LocalDateTime enrollmentDate;

    public enum EnrollmentStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }
}
