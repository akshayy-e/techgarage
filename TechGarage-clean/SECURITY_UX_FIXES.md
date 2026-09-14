# TechGarage Security & UX Fixes

## Applied
- Login now requires a verified email in the backend; this rule cannot be disabled by an environment variable.
- Login page clearly tells users that email verification is mandatory.
- Removed demo credentials from the login screen.
- Added a reusable Back button to all routed pages except the public landing page.
- Back button uses browser history and safely falls back to the homepage.
- Removed the duplicate nested project copy from the deliverable; this ZIP contains one canonical `frontend` and `backend`.
- Preserved Vercel SPA routing configuration and production API environment examples from the previous fix.

## Deployment note
Production email verification requires the backend mail settings to be configured so users receive the verification email.
