# Careway — Healthcare MVP microservices

A Java 21 / Spring Boot 4 learning project with a browser portal for patients and doctors.
The implemented scope is a scheduling MVP: register → sign in → create a profile → find a doctor → book, view, or cancel an appointment. Doctors maintain a directory entry and view or complete their assigned visits.

## Run locally

Requires Java 21 and Python 3.9+; Maven is downloaded by the included wrapper on first use.
From the repository root:

```sh
bash auth-service/mvnw verify
python3 scripts/run_demo.py
```

Open **http://localhost:8080**. Stop with Ctrl+C. All demo records disappear when the processes stop. The launcher checks for occupied ports and stops only its own child processes. Logs are in `.runtime/`.

1. Sign in as `doctor@example.test` / `DemoDoctor123!` and create an available doctor profile using a synthetic license number.
2. Sign out, create a patient account (12–72 character password), and sign in.
3. Save the patient profile, choose the doctor, and book a future visit.
4. Sign in as the doctor to see that appointment. Future visits can be cancelled; the assigned doctor can complete a visit after it has ended.

The token stays in browser memory; refreshing the browser signs out. Patient registration cannot create doctor, reception, or admin accounts. The doctor account above exists only under the explicit `demo` profile.

### Docker alternative

```sh
docker compose up --build
```

Open the same address. This is a loopback-only demo configuration with in-memory databases, not a production deployment. Startup may take a minute; refresh if services are still starting.

### Persistent MySQL development

Set `DB_PASSWORD` and a random `JWT_SECRET` of at least 32 bytes in your environment, then:

```sh
docker compose -f compose.yaml -f compose.mysql.yaml up --build
```

This creates four databases in a persistent MySQL volume and disables the demo account. The local example uses a root database connection for convenience; replace it with a separate least-privilege account for each service before any shared deployment. Existing databases are not reinitialized when a volume is reused.

Staff provisioning is intentionally not exposed over public HTTP. In this local MySQL environment, an operator may register a synthetic patient account and change its role to DOCTOR in the auth database while the account is signed out, then log in again. A reviewed invitation/approval workflow is a follow-up requirement; do not use manual role editing as a production onboarding process.

## Services and API documentation

| Component | Port | Responsibility |
|---|---:|---|
| Gateway + portal | 8080 | Same-origin UI and API routing |
| Auth | 8081 | Password hashing, patient signup, login, JWT issuance |
| User | 8082 | Patient's own demographic profile |
| Doctor | 8083 | Doctor-owned profile, paginated directory |
| Appointment | 8084 | Booking, participant access, cancellation, completion |

Each Java service exposes `/swagger-ui.html`, `/v3/api-docs`, and `/actuator/health` on its own port. The gateway routes business APIs; documentation remains on the individual ports. For example: http://localhost:8083/swagger-ui.html.

Tokens contain `sub` (email), `authUserId`, `role`, issued-at, and expiry. Auth-service signs tokens; downstream services verify the signature and read identity from the token. Every database belongs to one service. Appointment-service obtains doctor information through an HTTP call with short connection and read timeouts.

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/signup` | Public; PATIENT only |
| POST | `/api/auth/login` | Public |
| POST | `/api/user-profiles` | PATIENT, creates own profile |
| GET / PUT | `/api/user-profiles/me` | PATIENT, own profile |
| GET | `/api/doctors?availableOnly=true&page=0&size=20` | Authenticated |
| GET | `/api/doctors/{id}` | Authenticated |
| POST | `/api/doctors` | DOCTOR, own profile |
| GET / PUT | `/api/doctors/me` | DOCTOR, own profile |
| POST | `/api/appointments` | PATIENT |
| GET | `/api/appointments/me?page=0&size=20` | PATIENT / DOCTOR, own visits |
| GET | `/api/appointments/{id}` | Assigned patient or doctor |
| PATCH | `/api/appointments/{id}/cancel` | Assigned participant, future BOOKED visit |
| PATCH | `/api/appointments/{id}/complete` | Assigned doctor, ended BOOKED visit |

Example booking body:

```json
{"doctorId": 1, "startsAt": "2030-01-15T15:00:00Z", "reason": "Synthetic routine checkup"}
```

The caller's patient ID always comes from JWT. Times are absolute instants; visits use fixed 30-minute UTC-aligned slots. Unique doctor-slot and patient-slot database keys reject conflicting writes with HTTP 409, including concurrent requests. Cancellation clears the keys while retaining the appointment history. Optimistic versioning prevents stale status updates. A doctor's availability is checked at booking time; it is not a calendar or working-hours system and is not atomically coordinated with a concurrent profile availability change.

## Verify

```sh
bash auth-service/mvnw verify
python3 scripts/smoke_test.py
```

The Java suite exercises real HTTP controller/security behavior with H2, including forged profile ownership, role restrictions, API docs, appointment access, concurrency, and state transitions. The smoke test launches all four packaged services on ports 18081–18084 and a gateway on 18080, uses actual issued JWTs, and cleans up on exit. CI runs both checks. MySQL-specific behavior and container images need separate environment validation; H2 tests do not establish MySQL compatibility by themselves.

## Read and understand the code

Start with [the learning guide](docs/LEARNING_GUIDE.md), [recovered goals](docs/PROJECT_OBJECTIVES.md), and [the review](docs/CODE_REVIEW.md). New code keeps the existing Controller → Service → Repository → Entity/DTO layout, Lombok constructor injection, response mapping, and explanatory comments. Comments focus on purpose, trust boundaries, and concurrency choices.

## Scope and next steps

This is a demonstrable scheduling MVP using synthetic information. It is not a complete EHR, a clinically validated product, or a production-ready replacement for Epic. Clinical documentation, medication workflows, laboratory integration, billing, interoperability, and operational safeguards remain future work. See [the roadmap](docs/ROADMAP.md) for concrete stages and acceptance criteria.
