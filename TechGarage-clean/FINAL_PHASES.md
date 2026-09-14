# TechGarage — Final Phases Delivered

This package continues the previous Phase 4 build through the remaining product phases.

## Phase 5 — Communication & reliability
- Server-Sent Events notification stream with JWT Authorization header.
- Notification service publishes new notifications immediately to connected users.
- Navbar subscribes to the stream and keeps a polling fallback.
- Spring Boot Actuator health/info endpoints.
- Health endpoints are public; detailed health remains protected by Actuator policy.
- Existing REST chat remains the source of truth. Message notifications are delivered in real time.

## Phase 6 — AI diagnosis & emergency repair
- `/api/ai/diagnose` provides first-pass software triage.
- Uses Anthropic when configured and a deterministic fallback otherwise.
- AI prompt explicitly forbids requesting passwords, API keys, tokens or secrets.
- New protected `/ai-diagnosis` frontend page.
- Emergency repair queue based on the existing `EMERGENCY` priority.
- `/api/problems/emergency` endpoint and freelancer emergency page.

## Phase 7 — Launch UX & policy foundation
- Terms of Service template.
- Privacy Policy template.
- Refund Policy template.
- Footer navigation to policy pages.
- Production launch checklist remains in README and previous phase documents.

## Still required before a public production launch
These cannot responsibly be completed without the operator's real accounts/business decisions:
1. Production Razorpay account, keys and webhook configuration.
2. Marketplace payout/settlement setup and required KYC/business onboarding.
3. Production SMTP provider and verified sender domain.
4. Production database, object storage and backups.
5. Secret manager/environment configuration.
6. Legal review of Terms/Privacy/Refund policies.
7. Domain, TLS, monitoring and alerting.
8. Full Maven and Vite builds in a CI environment with network access.
9. Security penetration testing and load testing.

The application deliberately does not fake any of these external production controls.
