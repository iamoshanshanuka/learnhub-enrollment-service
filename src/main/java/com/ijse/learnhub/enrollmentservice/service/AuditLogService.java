package com.ijse.learnhub.enrollmentservice.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Append-only activity log of enrollment events, stored in Firestore.
 * MongoDB stays the source of truth for enrollments; Firestore is the audit trail, and the
 * frontend reads it back through GET /api/enrollments/audit.
 *
 * The client is created lazily and every call is wrapped in try/catch, so a missing credential or a
 * Firestore outage can never break the enrollment flow itself. Controlled by gcp.firestore.enabled.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Value("${gcp.firestore.enabled:false}")
    private boolean enabled;

    @Value("${gcp.firestore.collection:enrollment_activity}")
    private String collectionName;

    @Value("${gcp.project-id:}")
    private String projectId;

    private volatile Firestore firestore;

    public boolean isEnabled() {
        return enabled;
    }

    private Firestore getFirestore() {
        if (firestore == null) {
            synchronized (this) {
                if (firestore == null) {
                    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder();
                    if (projectId != null && !projectId.isBlank()) {
                        builder.setProjectId(projectId);
                    }
                    firestore = builder.build().getService();
                }
            }
        }
        return firestore;
    }

    public void logEvent(String enrollmentId, String eventType, String details) {
        if (!enabled) {
            return;
        }
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("enrollmentId", enrollmentId);
            data.put("eventType", eventType);
            data.put("details", details);
            data.put("at", Instant.now().toString());
            data.put("timestamp", System.currentTimeMillis());
            getFirestore().collection(collectionName).document().set(data);
        } catch (Exception e) {
            log.warn("Could not write the Firestore activity log entry: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> recent(int limit) {
        if (!enabled) {
            return List.of();
        }
        try {
            QuerySnapshot snapshot = getFirestore().collection(collectionName)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .get(5, TimeUnit.SECONDS);
            List<Map<String, Object>> events = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                events.add(new LinkedHashMap<>(document.getData()));
            }
            return events;
        } catch (Exception e) {
            log.warn("Could not read the Firestore activity log: {}", e.getMessage());
            return List.of();
        }
    }
}
