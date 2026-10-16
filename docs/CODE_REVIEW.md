# Repository review and changes

## Baseline

The supplied checkout contained auth-service and user-service, Java 21 / Spring Boot 4.0.5, JWT, JPA/MySQL, Swagger configuration, and one context-load test per service. There was no README, doctor-service, appointment-service, gateway, portal, Compose setup, or CI workflow. The existing `auth-service.zip` was left untouched and is not used by the build.

## Issues addressed

| Finding | Impact | Change |
|---|---|---|
| Auth JWT omitted `authUserId` | Downstream profile service could not build its expected identity | Include the stable account ID in issued tokens |
| Profile service accepted the request body's owner ID | A patient could create a profile under another account | Remove the ID from the request DTO and use the authenticated argument throughout |
| Public signup accepted ADMIN / DOCTOR / RECEPTION | Caller could assign staff privileges | Restrict public signup to PATIENT; isolate synthetic doctor seed to demo profile |
| Profile filter printed Authorization | Bearer tokens appeared in logs | Remove token and authentication debug prints |
| Loose `Bearer` prefix check | Malformed headers could fail outside the intended parsing path | Require `Bearer ` and reject absent/invalid identity |
| Auth used springdoc 2.x with Boot 4 | Incompatible API documentation libraries | Align to springdoc 3.0.2, consistent with the existing user-service and official Boot 4.0 compatibility guidance |
| Hardcoded signing key | Deployments would share a known key | Require environment configuration; explicit demo/test overrides only |
| SQL value tracing and automatic persistence context during web rendering | Potential data exposure and implicit database queries | Disable verbose SQL and open-in-view in main configuration |
| No behavioral tests | Identity mismatch and escalation went undetected | Add controller/security/database tests and cross-process smoke checks |

## Design choices

Keep the original packages, DTOs, constructor injection, and entity-to-response mapping style. Independent service security code is explicit for learning; a carefully versioned security library could reduce duplication later. The gateway uses Nginx in Compose and a standard-library local development proxy. No service discovery infrastructure is needed for four local services.

Booking uses fixed 30-minute aligned slots. Unique database keys prevent both doctor and patient conflicts. Cancellation releases the slot but preserves history; optimistic locking detects racing status changes. Doctor directory reads use bounded pagination. Appointment-service calls the doctor's API with explicit timeouts.

## Verification and remaining limits

The implementation was built with Java 21. The Java suite and a real HTTP smoke workflow were executed locally with H2. Both demo and optional MySQL Compose configurations were parsed successfully. Docker's daemon was unavailable, so images, Nginx runtime, and MySQL-backed behavior were not verified here. CI configuration was added but has not run on GitHub until these local changes are published.

HMAC issuer verification, staff identity approval, password recovery, token revocation, database migrations, audit trails, clinical features, accessibility review, and cloud operations remain follow-ups. This review is a development review, not an external security assessment or compliance certification.

Official compatibility reference: https://springdoc.org/ (Spring Boot 4.0.x / springdoc 3.0.x).

Branch reconciliation: the baseline described above was main. The reference branch now also includes dev commit 9a83981 and its doctor-service work. See REFERENCE_BRANCH.md for the reconciled API and configuration differences.
