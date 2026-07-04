# Security Policy & Standard

> This document reflects the **real security posture of this codebase**, derived by reading the code.
> **Current state** = what the code does today (with `file:line`). **Should do (proposal)** = not yet applied.
> The final section (§10) is a ranked list of **actual issues found**, with severity.

---

## 1. Secret & configuration management

**Current state — NOT good.**
- `.env` in the repo working tree contains **real, live secrets**: JWT signing key ([.env:25](.env#L25)), DB password (`123456`), VNPay secret key `JAG53RLKR1XZQF2HEUNVM0UHBZA8YVVB` ([.env:50](.env#L50)), Gmail app-password `hkpntkbyypuuxrwt` ([.env:33](.env#L33)), Cloudinary API secret ([.env:44](.env#L44)), Google Gemini API key `AIzaSy...` ([.env:55](.env#L55)).
- Config files reference these via `${ENV}` placeholders (good pattern — [application.properties](src/main/resources/application.properties), [application-prod.properties](src/main/resources/application-prod.properties)), and `.env` **is** listed in `.gitignore`. But the values themselves are known/committed to the working environment and must be treated as **compromised**.
- Dev config hardcodes a fallback DB password and a weak JWT default (`dev-secret-key-for-testing-only`, [application.properties:11](src/main/resources/application.properties#L11)).

**Must do — immediately:**
1. **Rotate every secret above** (JWT, DB, VNPay, Gmail, Cloudinary, Gemini). Assume all are leaked.
2. Verify `.env` is not in git history (`git log --all -- .env`); if it ever was, purge it.
3. In production, inject secrets from a secret manager / orchestrator env, never from a file in the image or repo.
4. Remove weak hardcoded fallbacks; fail fast if a required secret is absent in prod.

---

## 2. Authentication & authorization

**Current state:**
- **AuthN:** stateless JWT. Access token = `java-jwt` **HMAC256** (symmetric), subject=email, claims `userId`+`roles`, 1h ([JwtService.java:35-54](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/JwtService.java#L35)). Refresh token = random UUID, 30 days, stored in DB, rotated on refresh ([AuthServiceImpl.java:241-251](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L241)).
- Passwords hashed with **BCrypt** ([SecurityConfiguration.java:47-48](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/SecurityConfiguration.java#L47)) — correct.
- Spring Security: CSRF off, `STATELESS`, `permitAll` for `/api/auth/**` + public GETs, else `authenticated()` ([SecurityConfiguration.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/SecurityConfiguration.java)).
- **AuthZ:** a custom `PermissionFilter` checks `(roleIds, httpMethod, path)` against DB-stored permissions ([PermissionFilter.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/PermissionFilter.java)). **No `@PreAuthorize`/method security is used anywhere.**

**NOT good — critical:**
- **Fail-open authorization** ([PermissionFilter.java:91,101-104](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/PermissionFilter.java#L91)): any exception during the permission check → request proceeds; a user with **no roles** → check skipped entirely; a method not in the `HttpMethod` enum (e.g. **HEAD**) → `valueOf` throws → caught → request proceeds. Authorization must be **fail-closed**.
- **Broad skip list** ([PermissionFilter.java:36-54](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/PermissionFilter.java#L36)): `/api/orders/`, `/api/carts/`, `/api/batches/`, `/api/suppliers`, `/api/banners`, `/api/books`, `/api/categories`, `/api/return-requests/`, `/api/notifications/`, `/api/payments/` bypass permission checks entirely (login is the only requirement).
- **Missing ownership checks (IDOR)** — see §10 for the specific endpoints.
- Access-token validity depends on **any** live refresh token of the user, so revoking one session doesn't invalidate access tokens if another session exists ([JwtService.java:74-87](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/JwtService.java#L74)).
- WebSocket has **no CONNECT authentication** ([WebSocketConfig.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/WebSocketConfig.java)).

**Should do:** make the filter fail-closed; remove sensitive paths from the skip list; enforce ownership in services (or adopt `@PreAuthorize` + `@PostAuthorize`); add a STOMP `ChannelInterceptor` that authenticates CONNECT and authorizes per-user topics.

---

## 3. Input validation & sanitization

**Current state:** Bean Validation on some DTOs, surfaced by `@Valid` + the validation handler in `GlobalExceptionHandler` ([:23-40](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L23)). Output is JSON (no server-side HTML templating), so classic reflected XSS surface is low.

**NOT good:** validation is inconsistent — several write endpoints omit `@Valid` and several DTOs have no constraints (see CODING_STANDARD.md §7), so negative prices/quantities and unbounded strings can reach the DB. File upload validates only the client-supplied `Content-Type`, not magic bytes ([CloudinaryServiceImpl.java:37-40](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/service/impl/CloudinaryServiceImpl.java#L37)).

**Should do:** validate every request body; enforce numeric bounds (`@PositiveOrZero`) on price/stock/quantity; validate upload content by magic bytes and cap size (limit exists at 10MB — [CloudinaryServiceImpl.java:43](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/service/impl/CloudinaryServiceImpl.java#L43)).

---

## 4. Injection prevention

**Current state — good.**
- **SQL:** all repository queries use Spring Data derived queries or JPQL with **bound parameters** (`:keyword`, `LIKE LOWER(CONCAT('%', :keyword, '%'))` — [BookRepository.java:56-81](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/repository/BookRepository.java#L56)). **No string-concatenated SQL/JPQL was found.**
- **Command injection:** no `Runtime.exec`/`ProcessBuilder` in application code.

**Watch-outs (not SQL injection, but fragile):**
- Gemini request JSON is built with `String.format` and hand-rolled escaping ([BookEmbeddingServiceImpl.java:242-248](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/service/impl/BookEmbeddingServiceImpl.java#L242)) — use a JSON library.
- `PermissionServiceImpl.matchPath` builds a regex by string replacement without escaping regex metacharacters ([PermissionServiceImpl.java:223-237](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/service/impl/PermissionServiceImpl.java#L223)) — prefer Spring's `AntPathMatcher`.

---

## 5. Dependency & vulnerability management

**Current state — NOT good.**
- No dependency scanning: no OWASP `dependency-check`, no Dependabot config, no `mvn versions`/SCA in CI ([ci.yml](.github/workflows/ci.yml)).
- Spring Boot pinned to `4.0.1` ([pom.xml:8](pom.xml#L8)) — an unusual coordinate set; its patch/support status is unclear and should be verified.
- Third-party libs pinned: `java-jwt 4.4.0`, `cloudinary-http44 1.38.0`, `mapstruct 1.5.5` — need periodic CVE review.

**Should do:** add OWASP dependency-check (or Snyk/Dependabot) as a CI job; establish a cadence for bumping Spring Boot and libraries; confirm the Spring Boot version is a supported GA release.

---

## 6. API security (rate limiting, CORS, headers, CSRF)

**Current state:**
- **CSRF:** disabled ([SecurityConfiguration.java:54](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/SecurityConfiguration.java#L54)) — acceptable for a pure token-based API with no cookie auth.
- **CORS:** profile-split. Prod ([ProdCorsConfig.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/ProdCorsConfig.java)) restricts headers to `Authorization`/`Content-Type` (good) **but** allows `http://localhost:3000/5173` and `https://*.vercel.app` together with `allowCredentials(true)` ([:22-24,37](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/ProdCorsConfig.java#L22)) — any `*.vercel.app` origin (incl. third-party) can call the API with credentials.
- **Rate limiting:** **none** — no throttling on login/register/verify/refresh ([AuthController.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/controller/AuthController.java)).
- **Security headers:** no explicit HSTS/CSP/`X-Content-Type-Options` config (defaults only).
- **Actuator:** `health,info` exposed with `show-details=always` even in prod ([application-prod.properties:55-56](src/main/resources/application-prod.properties#L55)) — leaks infrastructure detail.

**Should do:** tighten prod CORS to exact frontend origins; remove localhost/wildcard from prod; add a rate limiter (e.g. Bucket4j) on auth endpoints; set `show-details=when_authorized` in prod; add security headers.

---

## 7. Sensitive data handling & safe logging

**Current state — NOT good.**
- Password field is `@JsonIgnore` on the entity ([User.java:41](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/model/User.java#L41)) — good; not serialized.
- But: **verification token is logged** ([AuthServiceImpl.java:313-314](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L313)); VNPay signing/secret-length logged ([VnPayServiceImpl.java:164-165](src/main/java/vn/edu/iuh/fit/bookstorebackend/payment/service/impl/VnPayServiceImpl.java#L164)); the verification token is placed in an **email URL query string** ([MailService.java:31](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/MailService.java#L31)).
- User-list responses expose email + phone + all addresses of every user ([UserResponse.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/dto/response/UserResponse.java)), and that endpoint's protection depends on DB-seeded permissions (unverified).

**Standard:** never log tokens/passwords/secrets/PII; put one-time tokens in the path or POST body, not query strings; minimize PII in list responses.

---

## 8. Database security

**Current state:**
- Passwords hashed with **BCrypt** (default strength) — correct ([SecurityConfiguration.java:47](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/SecurityConfiguration.java#L47)).
- Connection uses `useSSL=false` and DB user `root` in dev/compose ([application.properties:3](src/main/resources/application.properties#L3), [docker-compose.yml:23](docker-compose.yml#L23)).
- Schema via `ddl-auto=update` in prod ([application-prod.properties:7](src/main/resources/application-prod.properties#L7)) — Hibernate can alter the prod schema at runtime; no migrations, no rollback.
- Missing DB-level integrity: `User.email` not `unique` ([User.java:30](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/model/User.java#L30)); no unique on `PromotionUsage(user_id, promotion_id)` or `BookVariant(book_id, variant_id)`; no CHECK for non-negative stock.

**Should do:** use a **least-privilege** DB user (not root) in production; enable TLS to the DB; adopt Flyway/Liquibase and set prod `ddl-auto=validate`; add the missing unique/CHECK constraints.

---

## 9. Vulnerability disclosure & handling process

**Current state:** **no standard yet** — there is no `SECURITY.md` policy, no reporting contact, no triage/SLA process in the repo.

**Should do (proposal):**
- Report suspected vulnerabilities privately to the maintainers (add a real contact/email here) — do **not** open a public issue.
- Target acknowledgement within 48h; triage severity (Critical/High/Medium/Low) and target fix windows accordingly.
- Rotate any affected secrets on disclosure; record the fix in the changelog.

---

## 10. Current risks (found in code) — ranked

Severity: 🔴 Critical · 🟠 High · 🟡 Medium. Full detail and remediation order are in `requirement/04-risks.md` and `requirement/07-honest-review.md`.

| # | Sev | Issue | Evidence |
|---|---|---|---|
| 1 | 🔴 | **Fail-open authorization** (error / no-role / HEAD all bypass permission checks) | [PermissionFilter.java:91,101-104](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/PermissionFilter.java#L91) |
| 2 | 🔴 | **IDOR: any user can change any order's status** (no ownership/role check) | [OrderController.java:46](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/controller/OrderController.java#L46), [OrderServiceImpl.java:326](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/service/impl/OrderServiceImpl.java#L326) |
| 3 | 🔴 | **IDOR: cart view/clear/checkout by `{userId}`** without ownership check | [CartServiceImpl.java:38-43,163-168,181-189](src/main/java/vn/edu/iuh/fit/bookstorebackend/cart/service/impl/CartServiceImpl.java#L38) |
| 4 | 🔴 | **IDOR: address get/update/delete** ignore ownership | [AddressController.java:59-83](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/controller/AddressController.java#L59), [AddressServiceImpl.java:114-130](src/main/java/vn/edu/iuh/fit/bookstorebackend/user/service/impl/AddressServiceImpl.java#L114) |
| 5 | 🔴 | **Any logged-in user can mutate catalog/inventory/suppliers/banners** (skip-list + no role check) | [PermissionFilter.java:39-45](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/PermissionFilter.java#L39), [BookServiceImpl.java:224](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/service/impl/BookServiceImpl.java#L224) |
| 6 | 🔴 | **Live secrets present in `.env`** (JWT/DB/VNPay/Gmail/Cloudinary/Gemini) | [.env:25-55](.env#L25) |
| 7 | 🔴 | **Return/return-to-warehouse approve** enforce no SELLER/WAREHOUSE role (comment only) | [ReturnRequestServiceImpl.java:117-169](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/service/impl/ReturnRequestServiceImpl.java#L117) |
| 8 | 🟠 | **JWT HMAC256 with weak default secret**; session revoke not fully effective | [JwtService.java:29-35,74-87](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/JwtService.java#L29) |
| 9 | 🟠 | **Error responses leak raw exception messages** (all mapped to 400) | [GlobalExceptionHandler.java:53](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L53) |
| 10 | 🟠 | **User enumeration on register** ("Email already exists: <email>") | [AuthServiceImpl.java:82-89](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L82) |
| 11 | 🟠 | **No rate limiting** on auth endpoints (brute force / mail spam) | [AuthController.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/controller/AuthController.java) |
| 12 | 🟠 | **WebSocket CONNECT not authenticated**; open test endpoint pushes to any userId | [WebSocketConfig.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/WebSocketConfig.java), [NotificationController.java:29](src/main/java/vn/edu/iuh/fit/bookstorebackend/notification/controller/NotificationController.java#L29) |
| 13 | 🟠 | **Prod CORS allows localhost + `*.vercel.app` with credentials** | [ProdCorsConfig.java:22-24,37](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/config/ProdCorsConfig.java#L22) |
| 14 | 🟠 | **VNPay: no IPN, no amount reconciliation, no refund, no expiry/cleanup job**; deploy has no test gate | [PaymentController.java:41](src/main/java/vn/edu/iuh/fit/bookstorebackend/payment/controller/PaymentController.java#L41), [VnPayServiceImpl.java:193](src/main/java/vn/edu/iuh/fit/bookstorebackend/payment/service/impl/VnPayServiceImpl.java#L193) |
| 15 | 🟠 | **Secrets passed via `docker run -e` on command line** (visible in `ps`/`docker inspect`) | [deploy.yml:31-46](.github/workflows/deploy.yml#L31) |
| 16 | 🟡 | Sensitive data in logs (verification token, VNPay signing) | [AuthServiceImpl.java:313](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L313), [VnPayServiceImpl.java:164](src/main/java/vn/edu/iuh/fit/bookstorebackend/payment/service/impl/VnPayServiceImpl.java#L164) |
| 17 | 🟡 | Actuator `show-details=always` in prod | [application-prod.properties:56](src/main/resources/application-prod.properties#L56) |
| 18 | 🟡 | `ddl-auto=update` in prod; missing unique/CHECK constraints | [application-prod.properties:7](src/main/resources/application-prod.properties#L7) |
| 19 | 🟡 | Upload validated by client Content-Type only | [CloudinaryServiceImpl.java:37-40](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/service/impl/CloudinaryServiceImpl.java#L37) |
| 20 | 🟡 | No dependency/CVE scanning | [ci.yml](.github/workflows/ci.yml) |

**Fix order:** rotate secrets (#6) → make authorization fail-closed + trim skip-list + add ownership checks (#1–5, #7) → harden VNPay (#14) → the rest.
