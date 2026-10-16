# Understand the MVP one request at a time

## Start with the existing pattern

Every service follows the same separation:

```text
Browser → Gateway → JWT filter → Controller → Service → Repository → Database
                                              ↓
                                       response DTO → JSON
```

The controller understands HTTP, validates request fields, and obtains the principal. The service makes business decisions. The repository loads and saves data. An entity represents persistence; a DTO defines the API contract. Constructor injection (`@RequiredArgsConstructor` with final fields) makes dependencies explicit and testable.

## 1. Signing in

Read `auth-service/.../controller/AuthController.java`, then `service/AuthService.java` and `service/JwtService.java`.

1. Login normalizes the email, finds the user, and compares the raw password against its BCrypt hash.
2. JWT generation puts email, stable account ID, role, and expiration inside a signed token.
3. Other services verify that signature using the configured key. They do not trust a plain ID in a request body.

Explain: hashing versus encryption; authentication versus authorization; a JWT payload is readable, so it must not contain passwords or clinical details. A shared HMAC key makes this demo simple but grants every verifier signing capability; a future asymmetric-key design separates issuer and verifier privileges.

## 2. Creating a patient profile

Read `user-service/.../security/JwtAuthenticationFilter.java`, `controller/UserProfileController.java`, and `service/UserProfileService.java`.

The filter creates a `CustomUserPrincipal`. The controller obtains it with `@AuthenticationPrincipal` and sends the verified ID to the service. The request contains only editable demographic fields. A unique auth-user column prevents a second profile even if requests arrive together.

Explain: why a comment saying “we only trust JWT” is insufficient when the service actually uses `request.getAuthUserId()`; behavior and tests must enforce the intent.

## 3. Publishing a doctor profile

Read `doctor-service/.../controller/DoctorController.java` and `service/DoctorService.java`.

The DOCTOR role is required for creation and updates. The `/me` endpoint selects the caller's profile without accepting an owner ID. The paginated directory filters availability in SQL instead of loading every row and filtering in Java. Response DTOs omit the license field.

Explain: how Spring Data derives `findByAvailableTrue`; why pagination matters; why API responses should not return JPA entities directly.

## 4. Booking and double-booking

Read `appointment-service/.../service/AppointmentService.java`, `entity/Appointment.java`, and `client/DoctorDirectoryClient.java`.

The service validates a future, aligned time, asks doctor-service whether the doctor is available, and creates a 30-minute visit. It derives unique doctor and patient slot keys from trusted IDs and the appointment time. Database uniqueness decides which concurrent booking succeeds. An application-only “check then insert” would allow both requests to observe an empty slot.

Explain: transactions do not automatically serialize competing requests; database constraints protect the invariant. Fixed duration and alignment are deliberate scope choices. Arbitrary intervals would require overlap-aware locking or an explicit inventory of bookable slots.

## 5. Cancellation and completion

Cancellation requires a participant, a future visit, and BOOKED status. It preserves history and releases the unique keys. Completion requires the assigned doctor and an appointment that has ended. `@Version` detects competing updates to the same record.

Explain: ownership and role are different checks; DOCTOR alone must not grant access to every doctor's appointments. Database state transitions are authoritative even when the browser hides a button.

## 6. Failure boundaries

Doctor lookup has connect/read timeouts. An unavailable directory becomes HTTP 503; the booking is not saved. Conflicts return 409, invalid input 400, absent identity 401, insufficient privileges 403, missing records 404. The gateway routes requests, but each service still verifies JWTs.

Explain: why separate databases prevent cross-service JPA joins, and why doctor availability can change between lookup and booking. A future slot-inventory protocol would make availability ownership explicit.

## 7. Tests worth reading

Start with `ProfileApiTests.profileBelongsToTokenIdentity`, then `AppointmentApiTests.concurrentRequestsCannotDoubleBookDoctor`. Those tests protect business behavior rather than checking that a mocked method was called. `scripts/smoke_test.py` goes further by using real HTTP and auth-issued JWTs across all four processes.

Interview practice: draw the request flow without notes; identify the authorization boundary; explain how two requests race; describe the failure response when doctor-service is down; distinguish what H2 proves from what a real MySQL integration test would prove.
