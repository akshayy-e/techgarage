# TechGarage Phase 4 — Real Payments

## Implemented
- Razorpay order creation from a client job.
- Razorpay checkout verification using HMAC signature **and** server-side gateway status lookup.
- Razorpay webhook endpoint with signature verification.
- Payment transaction ledger with amount, platform fee, freelancer share, gateway IDs, status and refund metadata.
- Incremental payment support for accepted scope changes: only the outstanding amount is charged.
- Refund processing for every captured transaction on a job.
- Freelancer earnings now use the net amount after the configured platform fee.
- Freelancer cannot start a job until the required payment amount is captured and verified.
- Client payment/transaction history page.

## Important production model
Razorpay standard checkout collects funds into the TechGarage merchant account. `HELD` and `RELEASED` are TechGarage's internal job/ledger states; they are **not legal escrow**. Automatic freelancer bank payouts are intentionally not claimed as implemented. Add Razorpay Route/Payouts or another compliant marketplace payout flow after business/KYC onboarding.

## Environment
Backend:
- `RAZORPAY_ENABLED=true`
- `RAZORPAY_KEY_ID=...`
- `RAZORPAY_KEY_SECRET=...`
- `RAZORPAY_WEBHOOK_SECRET=...`
- `PLATFORM_FEE_PERCENT=10.0`

Frontend:
- No secret is needed in the browser. The checkout key is returned by the authenticated backend order endpoint.

## Razorpay dashboard
Configure a webhook to:
`POST https://YOUR_BACKEND/api/payments/webhook`

Use the same webhook secret configured in `RAZORPAY_WEBHOOK_SECRET`.
Recommended events:
- `payment.captured`
- `payment.failed`
- `refund.processed`

Use test keys first. Do not commit secrets to Git.
