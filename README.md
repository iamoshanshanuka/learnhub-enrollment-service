# LearnHub Enrollment Service

Enrollments of students in courses (Spring Boot, MongoDB, Firestore, service-to-service calls through Eureka).

## Student Information

- **Student Name:** M.W. Oshan Shanuka
- **Student Number:** 2301692025
- **Slack Handle:** oshan_shanuka
- **GCP Project ID:** learnhub-capstone

Final project of **ITS 2130 Enterprise Cloud Architecture** (Higher Diploma in Software Engineering, IJSE).

## Project Description

Creating an enrollment calls **student-service** and **course-service through Eureka** (a load-balanced `RestClient`) to make sure both exist, then stores the enrollment as a document in **MongoDB**. Rules: a student cannot enroll twice in the same course and may hold at most `learnhub.enrollment.max-active-per-student` open enrollments (delivered by the Config Server). Every change is also written to **Firestore** as an activity log that the web application reads back.

## Technology Stack

- Java 25
- Spring Boot 4.0.7 and Spring Cloud 2025.1.2
- Spring Data MongoDB (MongoDB 8 on a VM)
- Google Cloud Firestore client
- Spring Cloud Netflix Eureka Client + LoadBalancer (`@LoadBalanced RestClient`), Spring Cloud Config Client, Bean Validation, Actuator
- PM2 (process manager on the VM: restarts on failure, starts again after a reboot)
- Google Compute Engine (IaaS), Cloud DNS, Cloud NAT, Cloud Load Balancing

## Setup / Getting Started

**Requirements:** JDK 25 and Maven 3.9+.

```bash
git clone https://github.com/iamoshanshanuka/learnhub-enrollment-service.git
cd learnhub-enrollment-service
mvn clean package
java -jar target/enrollment-service.jar   # needs MongoDB on localhost:27017; calls the other two services via Eureka
```

Runs on port **8083**. Every setting has a local default, so no environment variable is needed for a local run.

### Configuration (environment variables)

| Variable | Meaning | Default |
|---|---|---|
| `SERVER_PORT` | HTTP port | `8083` |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/learnhub_enrollment_db` |
| `FIRESTORE_ENABLED` | `true` = write the Firestore activity log | `false` |
| `GCP_PROJECT_ID` | GCP project for Firestore | `(empty = automatic)` |
| `CONFIG_SERVER_URL` | Config Server address | `http://localhost:8888` |
| `EUREKA_SERVER_URL` | Eureka URL(s), comma separated | `http://localhost:8761/eureka/` |

### API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/enrollments` | enroll a student `{studentId, courseId}` |
| GET | `/api/enrollments`, `/api/enrollments/{id}` | list, read |
| GET | `/api/enrollments/student/{studentId}` | enrollments of one student |
| PATCH | `/api/enrollments/{id}/status` | `{status: PENDING, ACTIVE, COMPLETED or CANCELLED}` |
| DELETE | `/api/enrollments/{id}` | remove |
| GET | `/api/enrollments/rules` | rules received from the Config Server |
| GET | `/api/enrollments/audit?limit=10` | latest events read from Firestore |

Spring Boot 4 reads `spring.mongodb.uri` for the real `MongoClient`, so `application.yml` sets both `spring.mongodb.uri` and `spring.data.mongodb.uri`.
Errors return `{timestamp, status, error, message}` with 400, 404, 409 or 503 (a downstream service is unreachable).

### Run under PM2 (like on the GCP VM)

```bash
sudo mkdir -p /opt/learnhub /var/log/pm2
sudo cp target/enrollment-service.jar /opt/learnhub/enrollment-service.jar
pm2 start ecosystem.config.js --update-env
pm2 save
pm2 startup systemd -u root --hp /root      # PM2 starts again after a reboot
```

`ecosystem.config.js` restarts the app when it crashes and writes logs to `/var/log/pm2/enrollment-service-out.log` and `enrollment-service-error.log`.

## Related repositories

- [`learnhub-backend-services`](https://github.com/iamoshanshanuka/learnhub-backend-services)
- [`learnhub-student-service`](https://github.com/iamoshanshanuka/learnhub-student-service)
- [`learnhub-course-service`](https://github.com/iamoshanshanuka/learnhub-course-service)
- [`learnhub-enrollment-service`](https://github.com/iamoshanshanuka/learnhub-enrollment-service)
