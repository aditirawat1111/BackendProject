## Password Reset Implementation

This document explains how the **password reset** feature is implemented end‑to‑end in the backend.

---

### 1. Goal

Allow a user to securely reset their password via:

1. **Requesting a reset token** using their email.
2. **Submitting a new password** along with that token.

This matches the PRD requirement for **Password Reset** under User Management.

---

### 2. Data Model & Database

#### 2.1 Entity: `PasswordResetToken`

Location: `model/PasswordResetToken.java`

Extends `BaseModel` and adds:

- `token` (`String`, unique, not null) – random UUID used in the reset link.
- `user` (`User`, `@ManyToOne`) – the user this token belongs to.
- `expiryDate` (`Date`) – time after which the token becomes invalid.
- `used` (`boolean`) – marks tokens as one‑time use.

#### 2.2 Flyway Migration

Location: `db/migration/V6__create_password_reset_token_table.sql`

Creates `password_reset_token` table with:

- `id` (PK)
- `name`, `created_at`, `last_modified`, `is_deleted` (from `BaseModel`)
- `token` (unique)
- `expiry_date`
- `used`
- `user_id` (FK → `user.id`)

This ensures each token is **unique**, linked to a **specific user**, and can be **expired or marked as used**.

---

### 3. Repository

#### 3.1 `PasswordResetTokenRepository`

Location: `repository/PasswordResetTokenRepository.java`

Extends `JpaRepository<PasswordResetToken, Long>` and adds:

- `Optional<PasswordResetToken> findByToken(String token);`

Used to look up a token when the user submits it in the reset request.

---

### 4. DTOs

#### 4.1 `ForgotPasswordRequestDto`

Location: `dto/ForgotPasswordRequestDto.java`

Fields:
- `email` – required, validated with `@NotBlank` and `@Email`.

Used by `POST /auth/forgot-password`.

#### 4.2 `PasswordResetRequestDto`

Location: `dto/PasswordResetRequestDto.java`

Fields:
- `token` – required (the reset token).
- `newPassword` – required (the new password).

Used by `POST /auth/reset-password`.

#### 4.3 `PasswordResetTokenResponseDto`

Location: `dto/PasswordResetTokenResponseDto.java`

Fields:
- `message` – informational message.
- `token` – the generated reset token (for development/testing).

In a production system, this token would normally **not** be returned to the client, but sent via email instead.

---

### 5. Service Layer Logic

All business logic lives in `AuthenticationService`.

#### 5.1 Requesting a Password Reset

Method:
- `PasswordResetTokenResponseDto requestPasswordReset(ForgotPasswordRequestDto request)`

Flow:
1. Look up user by email:
   - If user not found → throw `UserNotFoundException`.
2. Create a new `PasswordResetToken`:
   - `token` = random UUID.
   - `user` = found user.
   - `expiryDate` = now + **1 hour**.
   - `used` = `false`.
   - Set `createdAt`, `lastModified`, `isDeleted = false`.
3. Save the token via `PasswordResetTokenRepository`.
4. Build and return `PasswordResetTokenResponseDto` with:
   - A success message.
   - The token string (for now).

This simulates what in production would be:

> Generate token → store it → send an email with a reset link containing the token.

#### 5.2 Resetting the Password

Method:
- `void resetPassword(PasswordResetRequestDto request)`

Flow:
1. Load `PasswordResetToken` by `request.token`:
   - If not found → throw `InvalidPasswordResetTokenException("Invalid password reset token")`.
2. Validate token:
   - If `token.isUsed()` is true, or
   - `expiryDate` is null or is **before now**  
   → throw `InvalidPasswordResetTokenException("Password reset token is expired or has already been used")`.
3. If valid:
   - Get the associated `User`.
   - Encode `request.newPassword` with `PasswordEncoder` (BCrypt).
   - Update user’s `password` and `lastModified`, then save via `UserRepository`.
   - Mark token as `used = true`, update `lastModified`, and save via `PasswordResetTokenRepository`.

This makes tokens **one‑time use** and enforces an **expiry window**.

---

### 6. Controller Endpoints

Defined in `AuthController` under `/auth`.

#### 6.1 `POST /auth/forgot-password`

Request:

```json
{
  "email": "user@example.com"
}
```

Behavior:

- Public endpoint (no authentication required).
- Delegates to `authenticationService.requestPasswordReset(...)`.
- Responses:
  - `200 OK` with `PasswordResetTokenResponseDto`.
  - `404 Not Found` if the email does not correspond to a user (`UserNotFoundException`).

#### 6.2 `POST /auth/reset-password`

Request:

```json
{
  "token": "<reset-token-uuid>",
  "newPassword": "NewSecurePassword123"
}
```

Behavior:

- Public endpoint (no authentication required).
- Delegates to `authenticationService.resetPassword(...)`.
- Responses:
  - `204 No Content` on success.
  - `400 Bad Request` with an error body if the token is invalid/expired/used (`InvalidPasswordResetTokenException`).

---

### 7. Security Configuration

Location: `config/SecurityConfig.java`

The following endpoints are configured as **public** (no JWT required):

- `/auth/register`
- `/auth/login`
- `/auth/forgot-password`
- `/auth/reset-password`

All other protected endpoints continue to require a valid JWT.

---

### 8. Error Handling

Location: `exception/GlobalExceptionHandler.java`

- `InvalidPasswordResetTokenException` is handled by:
  - Returning an `ErrorResponseDto` with:
    - `status = "Invalid Password Reset Token"`
    - `message = exception.getMessage()`
  - HTTP status: **400 BAD_REQUEST**

Existing handlers still cover:

- `UserNotFoundException` (for missing email on forgot‑password).
- Validation errors (for missing/invalid fields in DTOs).

---

### 9. How to Use the Flow (Manual Testing)

1. **Request a reset token**
   - Call `POST /auth/forgot-password` with:
     ```json
     { "email": "existing.user@example.com" }
     ```
   - Copy the `token` from the JSON response.

2. **Reset the password**
   - Call `POST /auth/reset-password` with:
     ```json
     {
       "token": "<copied-token>",
       "newPassword": "NewSecurePassword123"
     }
     ```
   - Expect `204 No Content` on success.

3. **Login with new password**
   - Call `POST /auth/login` using the same email and the new password.

This fully implements the **password reset** requirement for the backend, with proper token management, expiry, and error handling. 


