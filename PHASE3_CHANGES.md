# TechGarage Phase 3 — Account Trust, Disputes & Scope Protection

## Account trust
- Added email verification tokens with 24-hour expiry.
- Added password reset tokens with 30-minute expiry.
- Added secure forgot-password flow that does not reveal whether an email exists.
- Added resend-verification endpoint.
- Production profile can require verified email before login.
- Admin freelancer verification now requires email verification.
- Added SMTP-backed email delivery with a disabled-by-default development mode.
- Added freelancer profile completion and email verification indicators.

## Dispute lifecycle
- Disputes now remember the job status from before the dispute.
- Admin must choose a resolution action when closing a dispute.
- RESUME restores the job to a safe previous state.
- REFUND_AND_CANCEL refunds held simulated payment and closes the job.
- RELEASE_AND_COMPLETE releases held payment and completes the job.
- Both parties receive dispute-resolution notifications.

## Scope protection
- Added ChangeRequest entity and API.
- A pending scope change blocks another pending change.
- Either participant may propose additional work, price, and time.
- Only the other participant can accept/reject it.
- Accepted changes update agreed price and expected completion date.
- Completed/cancelled/disputed jobs cannot receive scope changes.
- Added client/freelancer UI for proposing and responding to scope changes.

## Production configuration
Set these in production:
- FRONTEND_URL
- REQUIRE_EMAIL_VERIFICATION=true
- MAIL_ENABLED=true
- MAIL_FROM
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD

Real payment gateway integration is intentionally not included until provider credentials, merchant/KYC configuration, webhook URL, refund policy, and commission rules are finalized.
