# Insurance Policy Management System (IPMS)

A full-stack web application for managing the insurance policy lifecycle: policy creation, activation, renewal, suspension, and cancellation, with role-based access for **Admin**, **Agent**, and **Customer** users.

This repository contains the **Spring Boot backend**. The Angular frontend lives in a separate module/repository (see [Frontend](#frontend)).

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1 |
| Security | Spring Security, JWT (JJWT 0.12.6), BCrypt |
| Database | PostgreSQL 15+, Spring Data JPA (Hibernate), Flyway |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito |
| Quality / CI | Checkstyle, SonarQube, Trivy, Gitleaks, GitHub Actions |
| Container | Docker |
| Frontend | Angular 17+ (separate module) |

## Features

- **Auth**: register, login, refresh token rotation, logout (refresh token revoked). Access token valid 24h, refresh token 28 days.
- **Account lockout** after 5 consecutive failed logins.
- **Password rules**: min 8 chars with upper, lower, digit, and special character (`@$!%*?&`).
- **Role-based access** enforced with `@PreAuthorize` and ownership checks for customers/agents.
- **Customers & Agents**: CRUD, search, unique codes (`CUST-XXXXXXX`, `AGT-XXXXXXX`), soft-delete.
- **Policies**: auto-generated number (`IPMS-YYYY-XXXXXXX`), premium calculation, state machine, full audit trail.
- **Documents**: PDF upload/download per policy.
- **Notifications**: in-app notification on every policy state change; mark as read.
- **Expiry handling**: expiring-soon query (30 days) and an expiry sweep on application startup.

### Policy lifecycle

```
DRAFT ──activate──▶ ACTIVE ──renew──▶ RENEWED
                     │  ▲
              suspend│  │reinstate
                     ▼  │
                  SUSPENDED

ACTIVE ──(end date passed)──▶ EXPIRED
ANY ──cancel──▶ CANCELLED
```

Invalid transitions are rejected and every valid transition is written to the audit log.

### Premium calculation

Premium = `sumInsured × baseRate × factor`

| Type | Base rate | Factor |
|---|---|---|
| LIFE | 0.5% | Customer age: under 30 → 1.0, 30–50 → 1.2, over 50 → 1.5 |
| HEALTH | 1.2% | Duration: 1 yr → 1.0, 2 yrs → 0.95, 3+ yrs → 0.90 |
| MOTOR | 2.0% | 1.0 |
| PROPERTY | 0.8% | 1.0 |

## Prerequisites

- JDK 21
- PostgreSQL 15+
- Maven (or use the bundled `mvnw` / `mvnw.cmd`)
- Docker (optional, for containerized run)

## Setup

### 1. Clone

```bash
git clone https://github.com/hano0709/ipmsWebsite.git
cd ipmsWebsite
```

### 2. Create the database

```sql
CREATE DATABASE insurance_policy_db;
```

### 3. Configure environment

Create a `.env` file in the project root (loaded by `spring-dotenv`, ignored by git):

```env
JWT_SECRET=replace-with-a-random-string-of-at-least-32-characters
DB_PASSWORD=your-postgres-password
KEYSTORE_PASSWORD=your-keystore-password
```

The JWT secret must be at least 32 characters (HS256 requirement). `application.properties` reads these values:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/insurance_policy_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
```

### 4. Configure document storage

Uploaded PDFs are currently saved to `C:/IPMS_Files` (hardcoded in `DocumentService`). Create this folder before uploading documents, or change the path in the code for Linux/macOS.

### 5. Run

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

Flyway runs migrations from `src/main/resources/db/migration` on startup.

The server starts on **https://localhost:8080** using the bundled self-signed certificate (`keystore.p12`). Your browser or Postman will warn about the certificate; accept it for local development.

All endpoints are prefixed with `/api/v1`.

### 6. Verify

| Check | URL |
|---|---|
| Health | `https://localhost:8080/api/v1/health` → `{"Status":"OK"}` |
| Swagger UI | `https://localhost:8080/api/v1/swagger-ui.html` |
| OpenAPI JSON | `https://localhost:8080/api/v1/v3/api-docs` |

## Creating the first users

`/auth/register` is restricted to Admin, so the very first admin must be inserted directly into the database. Generate a BCrypt hash of your password (for example with an online BCrypt generator or a small Spring test), then:

```sql
INSERT INTO users (email, password_hash, role, failed_attempts)
VALUES ('admin@example.com', '<bcrypt-hash>', 'ADMIN', 0);
```

Log in:

```bash
curl -k -X POST https://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"<your-password>"}'
```

Use the returned `accessToken` as `Authorization: Bearer <token>` on protected endpoints (in Swagger UI, use the **Authorize** button). Admin can then register other users:

```bash
curl -k -X POST https://localhost:8080/api/v1/auth/register \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"email":"agent@example.com","password":"Agent@1234","role":"AGENT"}'
```

## API Overview

Base URL: `/api/v1`. All endpoints require a Bearer token except those under Auth and `/health`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout` |
| Customers | `GET /customers`, `/customers/search`, `/customers/count`, `/customers/{id}`, `/customers/me`, `/customers/{id}/policies`; `POST /customers`; `PUT /customers`; `DELETE /customers/{id}` |
| Agents | `POST /agents`, `GET /agents/{id}`, `/agents/search`, `/agents/me`, `/agents/policies`; `DELETE /agents/{id}` |
| Policies | `GET /policies`, `/policies/{id}`, `/policies/{id}/audit`, `/policies/state-changes`, `/policies/expiring-soon`; `POST /policies`; `PUT /policies/{id}`; `PATCH /policies/{id}/activate`, `/renew`, `/suspend`, `/cancel` |
| Documents | `POST /policies/{id}/documents`; `GET /policies/{id}/documents`; `GET /documents/{docId}/download` |
| Notifications | `GET /notifications`; `PATCH /notifications/{id}/read` |

Full request/response schemas are in Swagger UI.

### Roles

| Role | Access |
|---|---|
| ADMIN | Full access |
| AGENT | Create and manage policies and customers, own portfolio |
| CUSTOMER | View own profile, policies, documents, and notifications |

## Testing

```bash
./mvnw test
```

Unit tests cover the service layer (Auth, Agent, Customer, Policy, Document, Notification, User, startup sync). Coverage target: 60%+.

Code style check (Google checks):

```bash
./mvnw checkstyle:check
```

## Docker

Build the JAR, place it in an `app/` folder, then build the image:

```bash
./mvnw package
mkdir -p app && cp target/*.jar app/
docker build -t ipms .
docker run -p 8080:8080 -e JWT_SECRET=<secret> ipms
```

Inside a container, `localhost` is the container itself, so point `spring.datasource.url` at your database host (for example `host.docker.internal`) when running this way.

## CI/CD

GitHub Actions (`.github/workflows/ci-pipeline.yml`) runs on pushes to `master` and `feat/**`:

1. Compile
2. Checkstyle
3. Security scan (Trivy filesystem scan, Gitleaks secret scan)
4. Unit tests
5. Package, SonarQube scan, and quality gate
6. Docker image build and push to Docker Hub

Required repository configuration: secrets `SONAR_TOKEN`, `DOCKERHUB_TOKEN`; variables `SONAR_HOST_URL`, `DOCKERHUB_USERNAME`. Jobs run on a self-hosted runner.

## Project Structure

```
src/main/java/com/bajaj/IPMS/
├── config/        Security and CORS configuration
├── controller/    REST controllers
├── service/       Business logic and state machine
├── repository/    Spring Data JPA repositories
├── model/         JPA entities
├── DTO/           Request and response DTOs
├── security/      JWT filter, user details, ownership checks
├── exception/     Custom exceptions and global handler
└── util/          JWT and password utilities
```

## Frontend

The Angular app (login, route guards, dashboards, policy workflow, customer/agent management, notification bell) is developed separately. It runs on `http://localhost:4200`, which is the only origin allowed by the backend's CORS configuration.

## Demo Flow

1. Log in as **Admin**, view dashboard KPIs.
2. Create an agent and a customer.
3. Create a policy (DRAFT) and check the auto-generated number and calculated premium.
4. Upload a PDF document to the policy.
5. Activate the policy, then check the audit trail and notifications.
6. Renew, suspend, or cancel to show state transitions and rejected invalid transitions.
7. Log in as **Customer** and show restricted, own-data-only access.

## Out of Scope

Claims processing, payment gateway integration, and a mobile app are out of scope for this phase.
