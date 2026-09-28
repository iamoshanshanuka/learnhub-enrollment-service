package com.ijse.learnhub.enrollmentservice.repository;

import com.ijse.learnhub.enrollmentservice.document.Enrollment;
import com.ijse.learnhub.enrollmentservice.document.Enrollment.EnrollmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    List<Enrollment> findByStudentIdOrderByEnrollmentDateDesc(Long studentId);

    long countByStudentIdAndStatusIn(Long studentId, Collection<EnrollmentStatus> statuses);

    boolean existsByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<EnrollmentStatus> statuses);
}
