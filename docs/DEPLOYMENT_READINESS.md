# TireTrack Deployment Readiness

This document tracks what must be true before TireTrack is used by a real shop or sold as SaaS.

## Current Verdict

One-shop pilot: nearly ready after final manual QA on a deployed staging instance.

Multi-shop paid SaaS: not ready until full endpoint-level tenant isolation, billing/subscription operations, legal/compliance, monitoring, backups, and onboarding are proven in production-like infrastructure.

## Repository Items Now Covered

- Production profile uses Flyway with `ddl-auto=validate`.
- Backend CORS is environment-driven through `APP_CORS_ALLOWED_ORIGINS`.
- Frontend can call either same-origin `/api` or a deployed backend through `VITE_API_BASE_URL`.
- Company settings logo uploads are supported with `company_settings.logo_url MEDIUMTEXT`.
- Settings/logo save has regression coverage in `ProductionReadinessIntegrationTests`.
- Backend tests can be run with `.\mvnw.cmd test`.
- Frontend production bundle can be built with `npm run build` from `frontend`.
- Local production gate can be run with `.\scripts\production-check.ps1`.

## Required Environment Variables

Backend:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://YOUR_MYSQL_HOST:3306/tiretrack?useSSL=true&serverTimezone=UTC
DB_USERNAME=tiretrack_user
DB_PASSWORD=replace-with-a-strong-password
JWT_SECRET=replace-with-base64-encoded-256-bit-or-larger-secret
APP_CORS_ALLOWED_ORIGINS=https://app.yourdomain.com
DDL_AUTO=validate
FLYWAY_ENABLED=true
```

Frontend:

```text
VITE_API_BASE_URL=https://api.yourdomain.com
```

Leave `VITE_API_BASE_URL` blank only when a reverse proxy serves frontend and backend on the same origin.

## One-Shop Pilot Checklist

Run these against a staging deployment connected to a cloned or empty MySQL database:

1. Start backend with `SPRING_PROFILES_ACTIVE=prod`.
2. Confirm Flyway migrations apply cleanly.
3. Build frontend with `npm run build`.
4. Login as SUPER_ADMIN, OWNER/ADMIN, EMPLOYEE, CUSTOMER.
5. Create/update company settings and upload a logo.
6. Create a printable invoice and confirm logo, shop name, phone, address, and terms display.
7. Create customer and vehicle from admin.
8. Login to customer portal and book with that vehicle.
9. Create appointment, convert to work order, mark in progress, mark vehicle ready.
10. Create estimate, approve it, convert it to invoice.
11. Create invoice with tire item and confirm inventory quantity deducts.
12. Record partial payment with due date, then full payment.
13. Confirm accounting revenue and profit/loss update.
14. Create payroll period, generate payroll, approve, mark paid.
15. Confirm payroll cost appears in accounting.
16. Employee login, clock in, clock out, view own attendance.
17. Admin reviews attendance and unresolved absences.
18. Verify dashboard, accounting, reports, notifications, audit logs.
19. Verify light/dark theme across admin, employee, customer, public booking.
20. Confirm unauthorized role access returns 401/403 and no sensitive data leaks.

## Multi-Shop SaaS Checklist

Before selling multiple shops on the same production database:

1. HTTP-level tenant isolation must pass for every endpoint:
   - customers
   - vehicles
   - tires
   - appointments
   - work orders
   - estimates
   - invoices
   - payroll
   - attendance
   - accounting
   - settings
   - notifications
   - audit logs
   - reports
2. SUPER_ADMIN platform flow must be QA-tested:
   - create shop
   - create owner/admin
   - assign admin to shop
   - assign subscription plan
   - deactivate shop
   - deactivate user
3. Shop admins must not access platform endpoints.
4. Shop admins must not see or edit other shops' records.
5. Customer portal must hide wrong-shop records consistently.
6. Subscription gating must be tested:
   - BASIC one active location
   - PREMIUM multiple active locations
   - ENTERPRISE same as premium for now
7. Billing process must exist:
   - manual billing SOP, or
   - Stripe/payment provider integration.
8. Support/onboarding process must exist for new shops.

## Infrastructure Requirements

- Hosted MySQL with automated daily backups.
- Documented restore test from backup.
- Backend host with environment variables, logs, and restart policy.
- Frontend host or reverse proxy.
- Domain and HTTPS certificates.
- Error monitoring and log aggregation.
- Uptime monitoring.
- SMTP/SMS provider if reminders/notices are production features.
- Secure admin access to production database.
- Separate staging database.
- Migration rollback plan.

## Legal And Business Requirements

These require owner/legal review before selling:

- Terms of Service.
- Privacy Policy.
- Refund/cancellation policy.
- Support policy.
- Data retention policy.
- Backup/restore promise.
- Security incident response contact.
- Customer agreement covering business data and customer PII.

## Recommended Launch Path

1. Launch one internal/staging shop.
2. Run the one-shop checklist from top to bottom.
3. Run a real shop pilot with backups enabled.
4. Fix issues found in pilot.
5. Run full multi-shop tenant isolation tests.
6. Add billing/legal/onboarding.
7. Sell to external shops.
