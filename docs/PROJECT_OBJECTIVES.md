# Recovered project objectives

## Evidence reviewed

The repository supplied by Naren: `https://github.com/nawrenice/healthcare-mvp-microservices`.
Three accessible project conversations were inspected:

- **Project Restart Motivation** — Java/backend learning and the later goal of landing a software engineering job; a demonstrable system with auth, profiles, doctors, appointments, gateway, MySQL, Docker, Swagger, JUnit, integration tests, CI, deployment, and monitoring.
- **Healthcare Project Development** — doctor-service business logic, querying available doctors in the repository, mapping entities to response DTOs, and explanatory comments.
- **DoctorController Design Guide** — unresolved `/v3/api-docs` and Swagger configuration errors.

This is a reconstruction from the accessible recent turns of those chats, not a claim to have read every previous conversation or recovered code from the user's computer. Doctor-service was discussed in the chats but was absent from this repository checkout.

## User-directed goals

1. Build a healthcare application; the current long-term ambition is an experience better than Epic.
2. Preserve the existing coding style and add comments that make purpose and behavior easy to understand.
3. Recover previous learning and architecture choices rather than replace the work with an unrelated stack.
4. From the prior chats: understand the engineering well enough to debug it and discuss it in Java/backend interviews.

## Scope chosen for this implementation

Complete the scheduling MVP inferred from the prior discussions: a patient can register, maintain a profile, find a doctor, book a visit, view visits, and cancel. A doctor can maintain an available directory profile and view/complete assigned visits. Provide a portal, independently runnable services, local demo, container setup, tests, CI, and learning documentation.

This scope is an implementation decision based on the evidence above. It does not establish a complete product requirements agreement for every hospital, clinical role, or Epic workflow.

## Make “better” measurable

The following are proposed success criteria to validate with patients and clinicians, not measured results:

- A new patient can book a visit without entering the same demographic information repeatedly.
- A returning patient can find the next appointment and cancel it without searching across screens.
- Doctors can distinguish upcoming and completed appointments and act only on assigned visits.
- Critical workflows have clear errors and recover safely from concurrency and service failures.
- Representative patients and clinicians can finish the chosen tasks more reliably and with less time than their existing workflow.

Choose one clinical setting and interview its users before expanding beyond scheduling. A focused, measured improvement is the first milestone toward the broader ambition.
