# TechGarage — Your Software Repair Garage

TechGarage is a specialized marketplace where clients post software/technical problems and
verified freelance "Tech Mechanics" diagnose, propose, and fix them — like a garage for code
instead of cars.

This repository contains a **fully working MVP**: a Spring Boot + MySQL backend and a React +
Vite frontend, wired together with real REST APIs and JWT authentication (no mock buttons, no
static-only screens).

---

## 1. Project Overview

- Clients post problems (bugs, API errors, DB issues, deployment failures, etc.), set a budget
  and priority, and review incoming proposals.
- Freelancers browse open problems, filter by technology/category/budget, and submit proposals.
- When a client accepts a proposal, a **Job** is created automatically; the freelancer works the
  job through a real status pipeline, chats with the client, submits a solution, and gets paid
  (simulated escrow) once the client approves.
- Admins verify freelancers, monitor all problems/jobs, and resolve disputes.

## 2. Features

- JWT authentication with BCrypt password hashing and role-based authorization (CLIENT,
  FREELANCER, ADMIN)
- Problem posting with a rule-based **AI issue classifier** (auto-detects category / technology /
  priority when left blank — no external API key required, but structured so a real LLM call can
  be dropped in later)
- Proposal submission, accept/reject workflow (accepting one proposal auto-rejects the rest and
  creates a Job)
- Full job lifecycle: `ASSIGNED → IN_PROGRESS → SUBMITTED → (REVISION_REQUESTED ↺) → COMPLETED`,
  with a visual progress tracker in the UI
- Job-scoped chat (client ↔ assigned freelancer only), persisted in MySQL
- Reviews & ratings that roll up into the freelancer's average rating
- Database-backed notifications for every major event
- Simulated escrow-style **Prototype Payment System** (`PENDING → HELD → RELEASED`), designed so
  Stripe/Razorpay can be swapped in later via the `PaymentService` interface
- File uploads (screenshots, logs, PDFs) stored locally, designed so S3/Cloudinary can be swapped
  in via the `FileStorageService` interface
- Admin console: users, freelancer verification, problems, jobs, disputes, platform stats
- Seed data so the whole app can be demoed immediately after first boot

## 3. Architecture

```
Controller → Service → Repository → Entity → MySQL
```

- DTOs are used for all request/response payloads (entities are never exposed directly, except
  for a couple of read-only admin list endpoints where the `password` field is `@JsonIgnore`d).
- A `GlobalExceptionHandler` turns all known failure modes (not found, forbidden, validation,
  duplicate email, bad credentials, etc.) into consistent JSON error responses.

## 4. Technology Stack

**Backend:** Java 17, Spring Boot 3.2, Spring Web, Spring Data JPA, Spring Security, JWT
(jjwt), MySQL, Maven, Bean Validation, Lombok

**Frontend:** React 18, Vite, JavaScript, Axios, React Router — plain CSS design system (no UI
kit), custom "workshop ticket" visual identity

**Database:** MySQL (database name: `techgarage`)

---

## 5. Prerequisites

- Java 17+ and Maven 3.9+ (or use the included `mvn` from your IDE)
- Node.js 18+ and npm
- MySQL 8+ running locally (or accessible via network)

## 6. MySQL Setup

```sql
CREATE DATABASE IF NOT EXISTS techgarage;
```

The backend also auto-creates the database on first connect thanks to
`createDatabaseIfNotExist=true` in the JDBC URL, so the step above is optional if your MySQL user
has permission to create databases. Tables are created/updated automatically by Hibernate
(`spring.jpa.hibernate.ddl-auto=update` in the `dev` profile).

## 7. Environment Variables

Copy `.env.example` to your own `.env` (or export the variables in your shell / IDE run
configuration). **Never commit real credentials.**

```env
DB_URL=jdbc:mysql://localhost:3306/techgarage?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=YOUR_MYSQL_PASSWORD

JWT_SECRET=YOUR_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS
JWT_EXPIRATION_MS=86400000

SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173

UPLOAD_DIR=uploads
SPRING_PROFILES_ACTIVE=dev
```

The frontend has its own `.env.example` under `frontend/`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 8. Backend Setup

```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=$(openssl rand -hex 32)

mvn clean install
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. On first boot (with the `dev` profile active, which
is the default), `DataSeeder` populates demo data automatically — see [Test Accounts](#11-test-accounts)
below.

To run the backend test suite (uses an in-memory H2 database, no MySQL needed):

```bash
mvn test
```

## 9. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** and talks to the backend at
`http://localhost:8080/api` (override via `VITE_API_BASE_URL` in `frontend/.env`).

To build a production bundle:

```bash
npm run build
```

## 10. Database Setup Summary

| Step | Command |
|---|---|
| Create DB (optional, auto-created otherwise) | `CREATE DATABASE techgarage;` |
| Run backend (creates/updates tables + seeds demo data) | `mvn spring-boot:run` |

Tables created: `users`, `freelancer_profiles`, `problems`, `proposals`, `jobs`, `messages`,
`reviews`, `notifications`, `disputes`.

---

## 11. Test Accounts

All seed accounts use these **local-development-only** passwords (never use these in
production — set your own via registration or change them after seeding):

| Role | Email | Password |
|---|---|---|
| Admin | `admin@techgarage.com` | `Admin@123` |
| Client | `client1@techgarage.com` | `Client@123` |
| Client | `client2@techgarage.com` | `Client@123` |
| Freelancer (React) | `react.dev@techgarage.com` | `Freelancer@123` |
| Freelancer (Java/Spring Boot) | `java.dev@techgarage.com` | `Freelancer@123` |
| Freelancer (Python) | `python.dev@techgarage.com` | `Freelancer@123` |
| Freelancer (AWS/DevOps) | `devops.dev@techgarage.com` | `Freelancer@123` |
| Freelancer (Full Stack) | `fullstack.dev@techgarage.com` | `Freelancer@123` |

Seed data also includes 4 sample problems (with realistic titles like *"Spring Boot login API
throws NullPointerException"*) and several pending proposals, so you can log in as `client1` and
immediately see proposals waiting to be accepted.

## 12. API Documentation

Base URL: `http://localhost:8080/api`. All endpoints except `/auth/**` require a
`Authorization: Bearer <token>` header obtained from login/register.

| Area | Method & Path |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Problems | `POST /problems`, `GET /problems`, `GET /problems/mine`, `GET /problems/{id}`, `PUT /problems/{id}`, `DELETE /problems/{id}` |
| Proposals | `POST /problems/{problemId}/proposals`, `GET /problems/{problemId}/proposals`, `GET /proposals/mine`, `PUT /proposals/{id}/accept`, `PUT /proposals/{id}/reject` |
| Jobs | `GET /jobs`, `GET /jobs/{id}`, `PUT /jobs/{id}/status`, `POST /jobs/{id}/submit-solution`, `POST /jobs/{id}/revision`, `POST /jobs/{id}/complete`, `POST /jobs/{id}/cancel` |
| Messages | `GET /jobs/{jobId}/messages`, `POST /jobs/{jobId}/messages` |
| Reviews | `POST /jobs/{jobId}/review`, `GET /freelancers/{id}/reviews` |
| Freelancer profile | `GET /freelancers`, `GET /freelancers/me`, `GET /freelancers/{userId}`, `PUT /freelancers/me` |
| Notifications | `GET /notifications`, `GET /notifications/unread-count`, `PUT /notifications/{id}/read` |
| Disputes | `POST /jobs/{jobId}/disputes` |
| Files | `POST /files/upload` (multipart) |
| Admin | `GET /admin/stats`, `GET /admin/users`, `GET /admin/clients`, `GET /admin/freelancers`, `GET /admin/problems`, `GET /admin/jobs`, `GET /admin/disputes`, `PUT /admin/disputes/{id}/resolve`, `PUT /admin/freelancers/{id}/verify`, `PUT /admin/users/{id}/suspend`, `PUT /admin/users/{id}/reactivate` |

A ready-to-import Postman collection is included at
[`TechGarage.postman_collection.json`](./TechGarage.postman_collection.json) covering the full
acceptance-test flow (register → post problem → propose → accept → work → complete → review).

All error responses share this shape:

```json
{ "success": false, "message": "Problem not found", "timestamp": "2026-08-11T10:15:00" }
```

## 13. Project Structure

```
techgarage/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/techgarage/
│       ├── config/          # Security & web (CORS, static uploads) config
│       ├── controller/      # REST controllers
│       ├── dto/             # Request/response DTOs, grouped by feature
│       ├── entity/          # JPA entities + enums
│       ├── exception/       # Custom exceptions + GlobalExceptionHandler
│       ├── mapper/          # (entity↔DTO mapping is inlined in service impls)
│       ├── repository/      # Spring Data JPA repositories
│       ├── security/        # JWT filter, util, user details service
│       ├── service/         # Service interfaces
│       ├── service/impl/    # Service implementations (business logic)
│       └── util/            # DataSeeder
├── frontend/
│   └── src/
│       ├── components/      # Navbar, cards, tracker, chat bits, etc.
│       ├── pages/            # client/, freelancer/, admin/, shared pages
│       ├── context/          # AuthContext
│       ├── services/         # One Axios module per resource
│       └── utils/             # Formatting helpers
└── TechGarage.postman_collection.json
```

## 14. CORS

The backend allows `http://localhost:5173` by default (`app.cors.allowed-origins`), so the Vite
dev server can call the API directly during local development. Change
`CORS_ALLOWED_ORIGINS` for other setups.

## 15. Final Acceptance Test

The scenario below is exercised end-to-end by `FullWorkflowIT` in the backend test suite, and can
also be walked through manually or via the Postman collection:

1. Register as Client → 2. Login → 3. Post a React website bug → 4. Register/Login as
   Freelancer → 5. View the posted bug → 6. Submit a proposal → 7. Login as Client → 8. View
   proposal → 9. Accept proposal → 10. Job is created → 11. Login as Freelancer → 12. Update job
   to `IN_PROGRESS` → 13. Send a chat message → 14. Submit solution → 15. Login as Client →
   16. Approve (or request revision) → 17. Complete the job → 18. Submit a 5-star review →
   19. Login as Admin → 20. Confirm the job, users, and review appear in the admin dashboard.

## 16. Future Improvements

- Real payment gateway integration (Stripe/Razorpay) behind the existing `PaymentService`
  interface
- Cloud file storage (S3/Cloudinary) behind the existing `FileStorageService` interface
- Real LLM-backed issue classification behind the existing `AIClassificationService` interface
- WebSocket-based live chat/notifications (REST polling is used for the MVP, as scoped)
- Pagination & full-text search on the problem board
- Refresh tokens / token revocation list

## 17. Screenshots

Run the frontend locally and visit `http://localhost:5173` — the landing page, dashboards, and
job detail/chat views are all live and screenshot-able once the backend is running.

---

### A note on security

- Passwords are never stored in plain text (BCrypt) and are excluded from every API response.
- No real credentials are committed anywhere in this repository — `.env` files are gitignored,
  and `.env.example` only contains placeholders.
- Seed account passwords are for **local development only** and are documented above rather than
  hidden, precisely so they are never mistaken for production secrets.
