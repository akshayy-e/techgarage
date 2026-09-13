# TechGarage QA Fixes Applied

## Functional / UX fixes
- Fixed AI Diagnosis layout by adding missing `.page-section`, `.form-grid`, `.full`, `.diagnosis-result`, `.error`, `.muted`, `.legal-page`, and `.mb-32` styles.
- Added responsive AI Diagnosis form layout and mobile support.
- Added AI Diagnosis client-side trimming/required validation.
- Fixed Admin Users verification UX: email verification is visible, profile Verify is disabled until email verification is complete, and verification/suspend actions have confirmation and loading states.
- Kept the backend security rule that an unverified email cannot be admin-profile-verified.
- Added read-only Admin access to problem details from the Admin Problems page.
- Added Admin Problems search/status/priority filters and result count.
- Added Admin Jobs search/status filters and payment-status visibility.
- Added Admin dispute action confirmation and loading states.
- Added Cancel Job UI with confirmation; the existing backend cancellation/refund rules remain authoritative.
- Made Admin Job Details chat read-only so admins cannot trigger a participant-only send action.
- Made Job Details layout responsive on smaller screens.
- Added a max length to job chat messages in the UI.

## Performance fixes
- Parallelized independent Job Details API requests with `Promise.all`.
- Parallelized Freelancer Problem Details API requests.
- Parallelized Client Problem Details proposal/job requests where applicable.
- Removed verbose Hibernate SQL logging from the development profile.
- Changed application logging from DEBUG to INFO in development.
- Added Hikari connection-pool defaults and shorter connection/validation timeouts for faster/fewer stalled DB requests.
- Admin stats now use database count queries for client/freelancer totals instead of loading full user lists.
- Admin user mapping now loads freelancer profiles once instead of issuing one profile query per user.
- Added a non-blocking backend health warm-up from the frontend to reduce first-request delay on sleeping hosted backends.

## Test alignment
- Updated the full workflow integration test so it verifies that a freelancer cannot start work before client payment is held, matching the current payment business rule. Real Razorpay gateway verification remains a sandbox/manual integration concern rather than being faked in the workflow test.

## Packaging
- Removed bundled `frontend/node_modules` and `backend/target` from the delivery ZIP. Dependencies/build artifacts should be regenerated with `npm ci` and Maven on the target machine/CI environment.
