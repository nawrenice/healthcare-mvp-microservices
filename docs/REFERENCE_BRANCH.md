# Comparing reference with your dev work

This branch includes your dev checkpoint `9a83981` in its history. Main and dev are unchanged. The generated implementation originally started from main (`4e4382a`); it has now been reconciled with your later doctor-service and Docker work.

Shared doctor implementation classes use the tested reference version. Your original create/update DTOs and exception classes remain for study, and your commented exception advice now handles the new business exceptions. The original Swagger configuration filename is retained to avoid a case-sensitive CI collision. The duplicated authUserId JWT claim has been consolidated.

The reference doctor API uses one DoctorProfileRequest with specialty and clinicAddress rather than the original separate create/update request contracts. See the reference DTO and README for the actual API. Your original implementations remain accessible on dev and at commit 9a83981.

The old docker-compose.yml delegates to compose.yaml so both standard Compose entry points launch the same demo. The documented workflow uses the root Dockerfile; service-local Dockerfiles from dev remain as learning references.

Continue your own implementation on dev. Compare individual methods and tests with reference; do not merge the entire reference branch as a shortcut. To view an original file use git show 9a83981:path/to/file, or compare the branches on GitHub.

The reference keeps patient profile access restricted to PATIENT, matching the portal and tests. The user-service retains dev's non-null update logic and supports both PUT (portal) and PATCH (existing dev clients) for /me. Both currently use the existing validation DTO, so PATCH still requires the mandatory name, address and gender fields. This is not a general JSON Merge Patch API. The extra identity debug print was removed.
