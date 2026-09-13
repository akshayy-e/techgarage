# Production Runbook

## Backend
Set at minimum:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- strong `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS=https://your-frontend-domain`
- `FRONTEND_URL=https://your-frontend-domain`
- `RAZORPAY_ENABLED=true` only after test-mode validation
- Razorpay key/secret/webhook secret
- `MAIL_ENABLED=true` and SMTP settings
- `UPLOAD_DIR` on persistent/object storage strategy
- `AI_FEATURES_ENABLED=true` only with a valid provider key

## Payment go-live
1. Test every payment state in Razorpay test mode.
2. Verify signatures server-side.
3. Verify captured status server-side.
4. Test duplicate callbacks/webhooks.
5. Test failed payment and refund paths.
6. Configure HTTPS webhook endpoint.
7. Move to live keys only after reconciliation tests.

## Security
- Never commit `.env` files or secrets.
- Never put payment secrets in frontend code.
- Never put JWTs in URLs.
- Do not accept admin role during public registration.
- Do not permit secrets in AI prompts or problem attachments.
- Configure rate limiting/WAF at the edge before public launch.
- Put uploads behind access-controlled object storage and malware scanning.

## Reliability
- Enable database backups and point-in-time recovery where supported.
- Monitor `/actuator/health`.
- Centralize application logs.
- Alert on payment webhook failures, 5xx rates, database errors and email failures.
- Run CI with Java 17, Maven, Node and `npm ci` on every change.
