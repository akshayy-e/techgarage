# TechGarage Performance & UX Pass

- Removed email verification from registration/login. New accounts are active immediately and verification-only email/token code is no longer used.
- Removed verification-only login messaging and verification route.
- Added React route-level code splitting with Suspense to reduce initial JavaScript payload.
- Added 5-second in-memory GET caching and request deduplication to prevent repeated dashboard/API calls.
- Reduced API timeout to 8 seconds so stalled requests fail quickly instead of blocking the UI.
- Added responsive loading state and stronger keyboard/focus/interaction UX.
- Enabled Spring response compression and HTTP/2.
- Tuned Hikari connection pooling and Hibernate batching.
- Disabled Open Session in View to reduce unnecessary persistence overhead.

## Deployment note
If the backend is hosted on a sleeping/free Render service, cold starts can still dominate first-request latency. Use a paid always-on instance for production or configure an external health monitor/cron to hit the public health endpoint.
