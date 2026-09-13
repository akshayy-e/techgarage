# TechGarage QA Fixes Applied

Date: 2026-09-13

## Scope

QA hardening was applied across backend API performance/security and frontend UX based on a static review of the supplied project.

## Backend fixes

1. **Removed proposal-count N+1 queries**
   - Added `ProposalRepository.countByProblemId(Long)`.
   - Problem and admin problem responses now use `COUNT` instead of loading every proposal.

2. **Moved freelancer directory filtering/sorting to the database**
   - Added a repository query for search, verification, availability, minimum rating, technology, and name/rating ordering.
   - Suspended/disabled users are excluded from the public freelancer directory.

3. **Protected public freelancer profile data**
   - Public profile responses no longer expose email, email-verification state, or total earnings.
   - The logged-in freelancer's own profile retains those fields.

4. **Added database performance indexes**
   - `users(role, enabled)`
   - `problems(status, created_at)`
   - `problems(client_id, created_at)`
   - `proposals(problem_id, created_at)`
   - `jobs(created_at)`
   - `notifications(user_id, is_read, created_at)`

5. **Hardened uploaded-file validation**
   - Added file-signature checks for PNG/JPEG/WEBP/PDF/ZIP.
   - Added image decoding validation for PNG/JPEG.
   - TXT/LOG files are rejected when the initial bytes contain binary NUL data.
   - Existing extension, MIME, size, random storage-name and normalized-path checks remain in place.

6. **Production JPA behavior**
   - Explicitly disabled Open Session in View in the production profile.

## Frontend fixes

1. **Freelancer directory search loading state**
   - Search button now shows `Searching…` and is disabled while the request is running.

2. **Invitation action handling**
   - Accept/Decline actions now show loading state, prevent duplicate clicks, and display API errors.

3. **AI Diagnosis UI polish**
   - Added a clear diagnosis-report header and status badge.
   - Improved spacing, result hierarchy, focus states, and mobile behavior.
   - Kept the security warning about not submitting secrets in logs.

4. **Responsive/accessibility improvements**
   - Improved small-screen navbar wrapping/scroll behavior.
   - Added visible keyboard focus states.
   - Disabled-button styling is consistent.
   - Form grids collapse to one column on smaller screens.

## Verification performed

- Source-level regression checks were performed after editing.
- Confirmed all previous proposal-count `.size()` usages in problem/admin response mapping were removed.
- Confirmed the new performance index migration is present as Flyway `V5__add_performance_indexes.sql`.
- Frontend build could not be executed in this environment because the supplied `node_modules` has no usable Vite executable and package installation timed out.
- Backend Maven tests could not be executed because Maven is not installed in this environment and package installation timed out.

## Remaining production work

The following should still be completed in CI/staging before public launch:

- Run `npm ci && npm run build`.
- Run `mvn clean test` with Java 17.
- Add API pagination to large list endpoints.
- Add rate limiting for login, registration, password reset, AI, and uploads.
- Add payment idempotency/webhook integration tests.
- Add security integration tests for cross-user and cross-role access.
- Consider Redis/shared messaging if SSE is deployed on multiple backend instances.
