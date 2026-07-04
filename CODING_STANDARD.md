# Coding Standard

> This document describes the coding standard **as actually practiced in this codebase**, derived by reading the code — not an external ideal.
> Sections marked **Current state** describe what the code does today (with `file:line` evidence). Sections marked **Should do (proposal)** are recommendations **not yet applied**.

---

## 0. How to read this document

There are **two kinds of rules** in this project:

| Kind | Enforced by | Status here |
|---|---|---|
| **Tool-enforced** | Checkstyle / Spotless / PMD / SpotBugs / SonarQube / git hooks | **NONE.** No such tool exists in the repo (verified: no `checkstyle.xml`, `spotbugs`, `pmd`, `.editorconfig`, Spotless/`fmt` plugin, `sonar-project.properties`, or git hooks; `pom.xml` has only the compiler + Spring Boot plugins). |
| **Implicit convention** | Repeated patterns, no automated check | **Everything below.** All conventions rely on developer discipline and review only. |

**Consequence:** every rule in this document is currently *implicit*. It can be — and in several places already is — violated without any build failure. The "Areas for improvement" section (§14) proposes which tools to add.

---

## 1. Stack overview & philosophy

- **Language:** Java 21 ([pom.xml:30](pom.xml#L30)).
- **Framework:** Spring Boot `4.0.1` ([pom.xml:8](pom.xml#L8)). ⚠️ This version/coordinate set (`spring-boot-starter-webmvc`, `-webmvc-test`) is unusual vs. the mainstream 3.x line and should be verified by the team.
- **Build tool:** Maven, via the wrapper `./mvnw` ([pom.xml](pom.xml), [Dockerfile:5](Dockerfile#L5)).
- **Persistence:** Spring Data JPA + Hibernate on MySQL 8 (`mysql-connector-j`).
- **Boilerplate reduction:** Lombok + MapStruct 1.5.5 ([pom.xml:99-101](pom.xml#L99-L101)).

**Philosophy observed in the code:** a layered, DTO-based REST backend organized **by feature**. Controllers are thin, business logic lives in `*ServiceImpl`, persistence in Spring Data repositories, and Entity↔DTO conversion in MapStruct mappers. Responses are wrapped in a uniform envelope. The intent is sound; the gaps are consistency and enforcement.

---

## 2. Naming conventions

**Current state** — consistent across the codebase:

| Element | Convention | Example |
|---|---|---|
| Package | lowercase, feature-first | `vn.edu.iuh.fit.bookstorebackend.order.service.impl` |
| Entity class | Singular PascalCase | `Book`, `Order`, `PurchaseOrder` ([Book.java:13](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/model/Book.java#L13)) |
| DB table | Plural snake_case, explicit `@Table` | `@Table(name = "books")` ([Book.java:12](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/model/Book.java#L12)) |
| DB column | snake_case, explicit `@Column` | `@Column(name = "stock_quantity")` ([Book.java:40](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/model/Book.java#L40)) |
| Service interface | `XxxService` | `OrderService`, `BookService` |
| Service impl | `XxxServiceImpl` in `service/impl` | `OrderServiceImpl` |
| Controller | `XxxController`, `@RequestMapping("/api/...")` | `OrderController` → `/api/orders` |
| Repository | `XxxRepository extends JpaRepository` | `BookRepository` |
| Mapper | `XxxMapper`, `@Mapper(componentModel = "spring")` | `BookMapper` ([BookMapper.java:17](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/mapper/BookMapper.java#L17)) |
| Request DTO | `VerbNounRequest` | `CreateBookRequest`, `UpdateOrderStatusRequest` |
| Response DTO | `NounResponse` | `BookResponse`, `OrderResponse` |
| Enum | PascalCase type, UPPER_SNAKE values | `OrderStatus.PENDING_PAYMENT` ([OrderStatus.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/common/OrderStatus.java)) |
| Method / variable | camelCase | `createOrder`, `totalAmount` |
| REST path | kebab/plural nouns | `/api/return-requests`, `/api/book-variants` |

**Known violations (fix on sight):**
- Typo baked into a shared type name: `RestRespone` (should be `RestResponse`) and `FormatRestRespone` ([RestRespone.java:10](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/model/RestRespone.java#L10), [FormatRestRespone.java:15](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/FormatRestRespone.java#L15)) — propagated to every import.
- Env var typo `PRING_JPA_HIBERNATE_DDL_AUTO` ([.env:19](.env#L19)) makes the variable dead.

---

## 3. Package structure & organization

**Current state:** **Feature-based (package-by-feature)**, and applied consistently. Each feature package contains its own layer sub-packages:

```
vn.edu.iuh.fit.bookstorebackend.<feature>/
├── controller/       @RestController
├── service/          interfaces
│   └── impl/          @Service implementations
├── repository/       Spring Data JPA
├── model/            @Entity
├── dto/request/      inbound DTOs
├── dto/response/     outbound DTOs
└── mapper/           MapStruct
```

Features: `auth, book, cart, order, payment, inventory, supplier, marketing, notification, user`. Cross-cutting code lives in `shared/` (`config`, `common` enums, `exception`, `util`, `model`, `service`).

This is a genuine strength — the layout is predictable and easy to navigate.

**Watch-out:** feature boundaries leak at the service layer. `OrderServiceImpl` directly uses book/promotion/notification repositories, and `book`/`cart`/`order` all read-write `Book.stockQuantity` directly. There is **no single inventory owner** — see §14.

---

## 4. Layering & responsibilities

**Current state:**

- **Controller** — thin; parses the request, delegates to the service, returns `ResponseEntity`. Example: [OrderController.java:26-31](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/controller/OrderController.java#L26). This is followed well; there is little business logic in controllers.
- **Service (`*ServiceImpl`)** — owns business logic, transactions, validation of business rules. Interface + impl split is universal.
- **Repository** — Spring Data JPA interfaces; JPQL via `@Query` where needed, always parameter-bound (e.g. [BookRepository.java:56-81](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/repository/BookRepository.java#L56)).
- **DTO vs Entity** — the API generally speaks DTOs (`*Request`/`*Response`), and MapStruct maps to/from entities. **Entities are not (intentionally) returned** from controllers; mappers produce `*Response`.
- **Mapper** — MapStruct, `@Mapper(componentModel = "spring")`, `@Mapping(..., ignore = true)` for fields not to be bound, `@Named` for custom conversions ([BookMapper.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/mapper/BookMapper.java)).

**Not good (back it with evidence):**
- Entities use Lombok `@Data` ([Book.java:11](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/model/Book.java#L11)). `@Data` on JPA entities generates `equals`/`hashCode`/`toString` over all fields including lazy associations — a known footgun (can trigger lazy loads / recursion). **Should do:** prefer `@Getter/@Setter` on entities, avoid `@Data`.
- Mapper swallows exceptions and returns an empty list, hiding lazy-init errors ([BookMapper.java:58-64](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/mapper/BookMapper.java#L58)).

---

## 5. Dependency injection

**Current state — done right and consistently.** Constructor injection via Lombok `@RequiredArgsConstructor` with `private final` fields. **38 classes use `@RequiredArgsConstructor`; there are zero `@Autowired` field injections in `src/main`** (verified by grep). Example: [OrderController.java:20-24](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/controller/OrderController.java#L20).

**Standard:** keep it this way — always constructor injection, `private final` dependencies, no field/setter `@Autowired`.

---

## 6. Exception handling

**Current state:**
- A single `@RestControllerAdvice` at `HIGHEST_PRECEDENCE` ([GlobalExceptionHandler.java:18-20](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L18)).
- Bean-validation errors → 400 with a `field → message` map ([:23-40](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L23)).
- One custom exception exists: `IdInvalidException` (a **checked** exception extending `Exception` — [IdInvalidException.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/IdInvalidException.java)), forcing `throws` to propagate.
- Uniform response envelope `RestRespone{error, message, statusCode, data}` wraps all 2xx responses via a `ResponseBodyAdvice` ([FormatRestRespone.java:35-39](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/FormatRestRespone.java#L35)).

**Not good:**
- The catch-all handler maps **every** exception to **HTTP 400** ([GlobalExceptionHandler.java:46,70](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L46)) — server faults masquerade as client errors.
- It returns the raw `exception.getMessage()` to the client ([:53](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/exception/GlobalExceptionHandler.java#L53)) — internal detail leak (see SECURITY.md).
- Business errors are thrown as bare `RuntimeException(...)` throughout the services (e.g. [OrderServiceImpl.java:94,128,227](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/service/impl/OrderServiceImpl.java#L94), `AuthServiceImpl`, `PurchaseOrderServiceImpl`), so HTTP status cannot be derived from the exception type.

**Should do (proposal):** define a small hierarchy of business exceptions (`NotFoundException` → 404, `ValidationException` → 400, `ForbiddenException` → 403, `ConflictException` → 409), map them explicitly in the advice, and never return the raw exception message to clients.

---

## 7. Validation

**Current state:** Bean Validation (`spring-boot-starter-validation`) is used on **some** request DTOs (`@NotBlank`, `@Email`, `@Size`, `@PositiveOrZero`, e.g. [RegisterRequest.java](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/dto/request/RegisterRequest.java), [CreateBookRequest.java:35-37](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/dto/request/CreateBookRequest.java#L35)), triggered by `@Valid` at the controller.

**Not good — inconsistent:**
- Many write endpoints omit `@Valid`, so DTO constraints never run: `BookController.updateBook` ([:96](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/controller/BookController.java#L96)), `RoleController.updateRole`, `PermissionController.createPermission`, `PurchaseOrderController` (approve/cancel/pay), `StockRequestController` (approve/reject).
- Several DTOs carry **no constraints at all**: `UpdateBookRequest`, `UpdateBookVariantRequest`, `BannerRequest`, `CreatePermissionRequest` — allowing e.g. negative price/stock.
- `confirmPassword` matching is checked manually in the service ([AuthServiceImpl.java:112-118](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L112)) instead of a class-level constraint.

**Standard to converge on:** every `*Request` DTO carries its constraints; every controller method taking a body annotates it `@Valid`. Input validation belongs at the **controller boundary**; business-rule validation stays in the service.

---

## 8. Transaction management

**Current state:** `@Transactional` is applied at the **service-impl method** level (152 occurrences across 25 files). Write methods are transactional; read methods commonly use `@Transactional(readOnly = true)` (e.g. `OrderServiceImpl`, `PermissionServiceImpl`). `spring.jpa.open-in-view=false` ([application.properties:58](src/main/resources/application.properties#L58)) — a good choice, but it means lazy access must occur inside a transaction.

**Not good:**
- Missing `@Transactional` on multi-step writers: `AuthServiceImpl.changePassword` ([:271](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L271)), `UserServiceImpl.createUser/updateUser`, several `RoleServiceImpl`/`AddressServiceImpl` methods — combined with `open-in-view=false` this risks `LazyInitializationException`.
- Side effects (email, WebSocket/notification) are performed **inside** the transaction, before commit, in inventory/procurement services ([StockRequestServiceImpl.java:52](src/main/java/vn/edu/iuh/fit/bookstorebackend/inventory/service/impl/StockRequestServiceImpl.java#L52), [PurchaseOrderServiceImpl.java:70](src/main/java/vn/edu/iuh/fit/bookstorebackend/supplier/service/impl/PurchaseOrderServiceImpl.java#L70)). `NotificationServiceImpl` does it **correctly** with `afterCommit` ([NotificationServiceImpl.java:109-115](src/main/java/vn/edu/iuh/fit/bookstorebackend/notification/service/impl/NotificationServiceImpl.java#L109)) — that is the pattern to follow everywhere.

**Standard:** `@Transactional` on every service method that writes; `readOnly = true` on reads; publish external side effects with `TransactionSynchronization.afterCommit`.

---

## 9. Logging

**Current state:** SLF4J via Lombok `@Slf4j`. Levels roughly appropriate; `application-prod.properties` lowers Spring/security/mail logging to INFO and `show-sql=false`.

**Not good — sensitive data in logs:**
- Verification token logged ([AuthServiceImpl.java:313-314](src/main/java/vn/edu/iuh/fit/bookstorebackend/auth/service/impl/AuthServiceImpl.java#L313)).
- VNPay signing detail / secret length logged at INFO ([VnPayServiceImpl.java:164-165](src/main/java/vn/edu/iuh/fit/bookstorebackend/payment/service/impl/VnPayServiceImpl.java#L164)).
- Dev config logs Security at DEBUG and `show-sql=true` ([application.properties:8,14](src/main/resources/application.properties#L8)).

**Standard:** never log tokens, passwords, secrets, or PII. Use parameterized logging (`log.info("... {}", x)`), not string concatenation.

---

## 10. JPA / Hibernate conventions

**Current state:**
- All `@ManyToOne`/`@ManyToMany` associations are `FetchType.LAZY` (good default).
- Collections use `cascade = ALL, orphanRemoval = true` where a true parent-child aggregate exists (`Book.bookVariants`, `Order`→details, etc.).
- Queries are JPQL with bound parameters — **no string-concatenated queries** were found (good; see SECURITY.md §4).
- Pagination helper `PaginationUtil` + `PageResponse` (1-based page numbers, [PaginationUtil.java:13](src/main/java/vn/edu/iuh/fit/bookstorebackend/shared/util/PaginationUtil.java#L13)).

**Not good:**
- **N+1** on list endpoints because mappers walk lazy associations without a fetch-join/entity-graph: list books ([BookServiceImpl.java:371-375](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/service/impl/BookServiceImpl.java#L371)), `getAllPurchaseOrders`, `getAllImportStocks`, `getAllBatches`.
- Unpaginated `findAll()` in 14 places (e.g. `/api/books/all`, `notifyActiveCustomers` loading every user — [PromotionServiceImpl.java:254](src/main/java/vn/edu/iuh/fit/bookstorebackend/marketing/service/impl/PromotionServiceImpl.java#L254)).
- **Money stored as `double`/`Double` everywhere** (e.g. [Order.java:27](src/main/java/vn/edu/iuh/fit/bookstorebackend/order/model/Order.java#L27), [Book.java:38](src/main/java/vn/edu/iuh/fit/bookstorebackend/book/model/Book.java#L38)) — no `BigDecimal` in the codebase. This is a correctness standard, not just style: **money must be `BigDecimal` (or integer minor units)**.
- No optimistic (`@Version`) or pessimistic (`@Lock`) locking anywhere → lost updates on stock/promotion counters.
- Schema managed by `ddl-auto=update` in **both** dev and prod ([application.properties:7](src/main/resources/application.properties#L7), [application-prod.properties:7](src/main/resources/application-prod.properties#L7)); no migration tool.

**Should do:** `BigDecimal` for money; fetch-joins/`@EntityGraph` for list reads; `@Version` on stock/counter entities; add Flyway/Liquibase and set prod `ddl-auto=validate`.

---

## 11. Testing

**Current state:** effectively **none**. The only test is `contextLoads()` and it is `@Disabled("Requires running database...")` ([BookstorebackendApplicationTests.java:13-18](src/test/java/vn/edu/iuh/fit/bookstorebackend/BookstorebackendApplicationTests.java#L13)). Test starters are present in `pom.xml` (JUnit/Mockito/Spring test, Spring Security test) but unused.

**No standard yet** for unit vs. slice vs. integration tests, mocking, or coverage.

**Should do (proposal):** unit-test services with Mockito (money math, state-machine transitions, authorization checks first); `@DataJpaTest` for repository queries; `@WebMvcTest` for controllers; a JaCoCo coverage gate in CI (start at a realistic floor and raise it).

---

## 12. Formatting, imports, style tooling

**Current state:** **No formatter or linter is configured** — no `.editorconfig`, no Spotless/google-java-format, no Checkstyle. Formatting is whatever each IDE produced; indentation and import order are inconsistent across files. `.gitattributes` only normalizes line endings for `mvnw`/`.cmd` ([.gitattributes](.gitattributes)).

**Should do (proposal):** adopt **Spotless + google-java-format** (or Checkstyle with the Google config) and wire it into CI as a required check, so formatting stops being a review topic.

---

## 13. Git: commits, branching, MRs

**Current state (observed from history & CI):**
- Branches: `main` (production, deployed by [deploy.yml](.github/workflows/deploy.yml)), `dev` (integration, tested by [ci.yml](.github/workflows/ci.yml)), plus short-lived `fix/*` branches (e.g. `fix/vnpay-real-ip`).
- Commit messages already follow **Conventional Commits**: `fix(order): ...`, `fix(payment): ...`, `feat: ...` — this is a good de-facto standard; keep it.
- PRs merge into `dev`; `dev` → `main` promotes to production.

**Not good:** CI's only gate is `mvn -q clean test` ([ci.yml:57](.github/workflows/ci.yml#L57)), which runs **no real tests** (all disabled) → the gate is empty. `deploy.yml` builds and runs on push to `main` with **no test step at all**.

**Standard to converge on:** protect `main` and `dev`; require the CI check to pass; keep Conventional Commits; add real tests + formatting + dependency scan to the required checks before `deploy.yml` can run.

---

## 14. Areas for improvement (inconsistencies → target standard)

| # | Current inconsistency / gap | Target standard |
|---|---|---|
| 1 | No linter/formatter/coverage/static analysis at all | Add Spotless + Checkstyle + JaCoCo + (optionally) SpotBugs/Sonar as required CI checks |
| 2 | Money as `double`/`Double` everywhere | `BigDecimal` (or integer minor units) for all monetary fields and math |
| 3 | Two inventory sources (`Book.stockQuantity`/`BookVariant.stockQuantity` vs `Batch.remainingQuantity`) mutated from multiple features | One `InventoryService` as the single writer/source of truth |
| 4 | `@Valid` present on some write endpoints, missing on others; some DTOs have no constraints | `@Valid` on every body; constraints on every `*Request` |
| 5 | Business errors thrown as bare `RuntimeException`; advice maps all to 400 and leaks messages | Typed business exceptions mapped to correct HTTP status; generic client message |
| 6 | Side effects inside transactions in some services, `afterCommit` in others | Always publish side effects with `afterCommit` |
| 7 | Entities annotated `@Data` | `@Getter/@Setter` on entities; never expose entities from controllers (already mostly true) |
| 8 | No locking; no unique constraints on `email`, `(user_id, promotion_id)`, `(book_id, variant_id)` | `@Version` on counters; add the missing unique constraints |
| 9 | No tests | Layered test strategy + coverage gate (§11) |
| 10 | `ddl-auto=update` in prod, no migrations | Flyway/Liquibase; prod `validate` |
| 11 | Misspelled shared type `RestRespone`/`FormatRestRespone` | Rename to `RestResponse`/`ResponseWrapper` |
