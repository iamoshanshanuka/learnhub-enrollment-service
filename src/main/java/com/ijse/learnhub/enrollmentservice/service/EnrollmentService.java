package com.ijse.learnhub.enrollmentservice.service;

import com.ijse.learnhub.enrollmentservice.client.CourseClient;
import com.ijse.learnhub.enrollmentservice.client.StudentClient;
import com.ijse.learnhub.enrollmentservice.document.Enrollment;
import com.ijse.learnhub.enrollmentservice.document.Enrollment.EnrollmentStatus;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.EnrollmentRequest;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.EnrollmentResponse;
import com.ijse.learnhub.enrollmentservice.dto.EnrollmentDtos.RulesResponse;
import com.ijse.learnhub.enrollmentservice.exception.ConflictException;
import com.ijse.learnhub.enrollmentservice.exception.ResourceNotFoundException;
import com.ijse.learnhub.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EnrollmentService {

    /** An enrollment that still counts against the per-student limit. */
    private static final List<EnrollmentStatus> OPEN_STATUSES = List.of(EnrollmentStatus.PENDING, EnrollmentStatus.ACTIVE);

    private final EnrollmentRepository repository;
    private final StudentClient studentClient;
    private final CourseClient courseClient;
    private final AuditLogService auditLogService;

    /** Both values come from the Config Server (config-repo/application.yml and enrollment-service.yml). */
    private final int maxActivePerStudent;
    private final String platformName;

    public EnrollmentService(EnrollmentRepository repository,
                             StudentClient studentClient,
                             CourseClient courseClient,
                             AuditLogService auditLogService,
                             @Value("${learnhub.enrollment.max-active-per-student:5}") int maxActivePerStudent,
                             @Value("${app.platform-name:local-defaults}") String platformName) {
        this.repository = repository;
        this.studentClient = studentClient;
        this.courseClient = courseClient;
        this.auditLogService = auditLogService;
        this.maxActivePerStudent = maxActivePerStudent;
        this.platformName = platformName;
    }

    public EnrollmentResponse create(EnrollmentRequest request) {
        // Inter-service calls through Eureka: both the student and the course must really exist
        Map<String, Object> student = studentClient.getStudent(request.studentId());
        Map<String, Object> course = courseClient.getCourse(request.courseId());
        String studentName = String.valueOf(student.get("fullName"));
        String courseTitle = String.valueOf(course.get("title"));

        if (repository.existsByStudentIdAndCourseIdAndStatusIn(request.studentId(), request.courseId(), OPEN_STATUSES)) {
            throw new ConflictException(studentName + " is already enrolled in " + courseTitle + ".");
        }
        long open = repository.countByStudentIdAndStatusIn(request.studentId(), OPEN_STATUSES);
        if (open >= maxActivePerStudent) {
            throw new ConflictException(studentName + " already has " + open
                    + " open enrollments. The limit is " + maxActivePerStudent + ".");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.studentId());
        enrollment.setStudentName(studentName);
        enrollment.setCourseId(request.courseId());
        enrollment.setCourseTitle(courseTitle);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment saved = repository.save(enrollment);
        auditLogService.logEvent(saved.getId(), "ENROLLED", studentName + " enrolled in " + courseTitle);
        return toResponse(saved);
    }

    public List<EnrollmentResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "enrollmentDate")).stream().map(this::toResponse).toList();
    }

    public EnrollmentResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return repository.findByStudentIdOrderByEnrollmentDateDesc(studentId).stream().map(this::toResponse).toList();
    }

    public EnrollmentResponse updateStatus(String id, EnrollmentStatus status) {
        Enrollment enrollment = getOrThrow(id);
        EnrollmentStatus previous = enrollment.getStatus();
        enrollment.setStatus(status);
        Enrollment saved = repository.save(enrollment);
        auditLogService.logEvent(id, "STATUS_CHANGED",
                enrollment.getStudentName() + " / " + enrollment.getCourseTitle() + ": " + previous + " to " + status);
        return toResponse(saved);
    }

    public void delete(String id) {
        Enrollment enrollment = getOrThrow(id);
        repository.deleteById(id);
        auditLogService.logEvent(id, "REMOVED",
                enrollment.getStudentName() + " was removed from " + enrollment.getCourseTitle());
    }

    public RulesResponse rules() {
        return new RulesResponse(platformName, maxActivePerStudent, auditLogService.isEnabled());
    }

    public List<Map<String, Object>> recentActivity(int limit) {
        return auditLogService.recent(limit);
    }

    private Enrollment getOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id " + id));
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(), e.getStudentId(), e.getStudentName(), e.getCourseId(),
                e.getCourseTitle(), e.getStatus(), e.getEnrollmentDate()
        );
    }
}
