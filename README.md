# Digital Wallet with Fraud Detection

A Spring Boot backend for a digital wallet: users hold a balance and transfer money to each other, and every transfer is screened by a rule-based fraud engine before it settles. Transfers that trip a rule don't complete automatically — they're held and routed to an admin for manual review.

## Why this exists

Most portfolio wallet projects are CRUD with a database behind them — create an account, move some numbers around. This one makes a decision: every transfer runs through fraud rules first, and money only reaches the receiver once the transfer passes cleanly or an admin explicitly clears it. It's a small-scale model of the kind of transfer-risk logic real payment platforms run in production.

## How a transfer works

1. A user submits a transfer request.
2. Both accounts are locked with pessimistic write locks, acquired in a fixed order by account ID (not by sender/receiver) — this prevents a deadlock if two transfers going in opposite directions between the same two accounts happen at the same instant.
3. The transfer is evaluated against the fraud rules:
    - **High amount** — flags any single transfer over ₹50,000
    - **Daily limit** — flags if today's cumulative transfers would exceed the sender's configured daily limit
    - **Velocity** — flags if the sender has made 5 or more transfers in the last 10 minutes
4. **If flagged:** the sender is debited, but the receiver is *not* credited yet. A `FraudFlag` is created and the transaction sits in `PENDING` review.
5. An admin reviews the flag:
    - **Clear** → the receiver is credited and the transaction completes.
    - **Confirm fraud** → the sender is refunded and the transaction fails.
6. **If clean:** the transfer completes immediately — sender debited, receiver credited, in one atomic operation.

## Tech stack

- Java, Spring Boot
- Spring Data JPA / Hibernate
- Spring Security — stateless JWT authentication, role-based method security (`@PreAuthorize`)
- H2 (test profile) — <!-- confirm your dev/prod database here -->
- JUnit 5, Mockito, MockMvc

## Architecture

Standard layered structure, plus a dedicated `fraud` package for the rule engine:

```
controller/   REST endpoints, request/response mapping
service/      business logic — transfers, auth, fraud review
repository/   Spring Data JPA interfaces
entity/       JPA entities
dto/          request/response payloads
fraud/        FraudRule interface + FraudDetectionEngine + individual rule implementations
exception/    custom exceptions + a centralized @RestControllerAdvice handler
security/     JWT authentication filter
```

Each fraud rule (`HighAmountRule`, `DailyLimitRule`, `VelocityRule`) is a separate Spring-managed bean implementing a shared `FraudRule` interface. Spring auto-injects all of them as a `List<FraudRule>` into `FraudDetectionEngine`, which runs them in sequence. Adding a new rule later means adding one new `@Component` class — no existing code has to change.

## API

The full endpoint-by-endpoint contract — request/response shapes, auth requirements, and a couple of integration gotchas (a few responses are plain text rather than JSON) — is in [`API_CONTRACT.md`](./API_CONTRACT.md).

Quick summary:

| Endpoint | Method | Auth |
|---|---|---|
| `/api/auth/register` | POST | Public |
| `/api/auth/login` | POST | Public |
| `/api/accounts/me` | GET | Authenticated |
| `/api/transactions/transfer` | POST | Authenticated |
| `/api/transactions/deposit` | POST | Authenticated |
| `/api/fraud/flags` | GET | Admin |
| `/api/fraud/flags/{id}/review` | PUT | Admin |

## Running it locally

```bash
git clone <repo-url>
cd <repo-folder>
mvn spring-boot:run
```

<!-- adjust if you're using Gradle instead of Maven -->

The app starts on `http://localhost:8080` by default. A default admin account is seeded on startup (`admin@example.com` / `admin123`) — this is fine for local testing but should be changed before deploying anywhere real.

## Tests

```bash
mvn test
```

- **Unit tests** (Mockito) for the individual fraud rules, in isolation from the database
- **Controller tests** (MockMvc) for request/response behavior and auth enforcement
- **One end-to-end integration test** that runs a full flagged transfer through fraud detection, blocks a non-admin from reviewing it, then has an admin clear it and verifies funds are released correctly — against a real (in-memory H2) database

## What's not in here yet

- Freezing an account after confirmed fraud (the `FROZEN` account status exists but nothing sets it yet)
- Deposits aren't run through the fraud engine — only transfers are
- Microservices split and Docker (planned next)

## Frontend
https://github.com/Abhishek-Sirugudu/secure-wallet-frontend.git
