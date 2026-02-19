# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FileHub** — A Korean-language corporate intranet file transfer system. Employees upload files to designated recipients with full download tracking, file expiry (30 days), and preview support. All access requires JWT authentication and is intended for internal network use only.

## Commands

### Backend (from `backend/`)

```bash
./gradlew bootRun        # Start dev server on port 8080
./gradlew build          # Build JAR
./gradlew test           # Run JUnit 5 tests
```

### Frontend (from `frontend/`)

```bash
npm install              # Install dependencies
npm start                # Dev server on port 3000 (proxies /api -> localhost:8080)
npm run build            # Production build
```

### Full Stack

```bash
# From project root — starts MySQL + Spring Boot + React (nginx)
docker-compose up
```

## Architecture

### Backend — Spring Boot 3.2.5 / Java 17

- **Package root:** `com.company.filehub`
- **Layered:** Controller → Service → Repository (Spring Data JPA)
- **Auth:** Fully stateless JWT (HMAC-SHA). `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`. Tokens expire in 24 hours.
- **Database:** MySQL 8.0, `ddl-auto: validate` — schema must be created manually from `backend/src/main/resources/db/schema.sql` before first startup.
- **File storage:** Local filesystem at `./uploads/` using UUID-prefixed filenames. Original name stored in DB only.
- **Scheduled cleanup:** `FileCleanupService` deletes expired files (past `expires_at`) nightly at 02:00 via `@Scheduled(cron = "0 0 2 * * *")`.
- **API envelope:** All responses use `ApiResponse<T>` for consistent shape.

### Frontend — React 18 / Create React App

- **Routing:** React Router 6 with `PrivateRoute`/`PublicRoute` guards in `App.js`.
- **Auth state:** `AuthContext.js` persists token + user to `localStorage`.
- **HTTP:** Single Axios instance in `api/client.js` — attaches Bearer token on every request, auto-redirects to `/login` on 401.
- **File upload:** react-dropzone multi-file with progress bar (`UploadPage.js`).
- **Preview:** Files fetched as blobs via `GET /api/files/{id}/preview`, rendered as `<img>` or `<iframe>` using a local object URL to keep JWT auth in-band.

### Database Schema

Six tables: `departments`, `users` (login key is `employee_id`), `files` (`expires_at` auto-set to +30 days by DB trigger), `file_recipients` (tracks `is_downloaded` per recipient), `download_logs` (immutable audit with IP), `notices` (공지사항, pinned + created_at ordering).

### Required Environment Variables

| Variable | Notes |
|---|---|
| `DB_URL` | Defaults to `jdbc:mysql://localhost:3306/filehub?...` |
| `DB_USERNAME` | Defaults to `root` |
| `DB_PASSWORD` | No default — required |
| `JWT_SECRET` | Base64-encoded HMAC-SHA key — required |

### Key Patterns

- **Paginated file lists** (`/received`, `/sent`): support `keyword` (LIKE on filename + sender), `sortBy` (whitelist: `createdAt`, `originalName`, `fileSize`), `sortDir`, `page`, `size`.
- **Role-based access:** `users.role` is `ENUM('USER','ADMIN')` defaulting to `USER`. Role is returned in the login response and stored in `localStorage` via `AuthContext`. `isAdmin` is derived as `user?.role === 'ADMIN'`. Spring Security enforces ADMIN-only write access to `/api/notices` via `hasRole("ADMIN")` in `SecurityConfig`.
- **Notice board:** ADMIN can create/edit/delete notices (`isPinned` supported). All authenticated users can read. Frontend uses `AdminRoute` guard for write pages.
- **CORS:** Restricted to `http://localhost:3000` in development config (`SecurityConfig.java`).

### Migrations (existing DB)

When adding new schema to an existing database, run these manually (also commented in `schema.sql`):

```sql
-- role 컬럼 추가
ALTER TABLE users ADD COLUMN role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '역할';

-- notices 테이블 생성 (schema.sql 참고)

-- 관리자 지정 예시
UPDATE users SET role = 'ADMIN' WHERE employee_id = 'EMP001';
```

> After granting ADMIN role, the user must **log out and log back in** to refresh the `role` stored in `localStorage`.
