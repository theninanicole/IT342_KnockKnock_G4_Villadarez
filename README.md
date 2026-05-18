# Knock Knock

Knock Knock is a visitor management system for condominiums. It replaces paper visitor logbooks with a web and mobile workflow for registering visits, checking visitors in and out, tracking visit history, sending QR-based visit details, and helping condominium administrators monitor building access.

## Overview

The project contains three main applications:

- `backend/` - Spring Boot REST API for authentication, visits, condos, notifications, files, email, and admin workflows.
- `web/knockknock/` - React + Vite web app for visitors and condo administrators.
- `mobile/` - Android app written in Kotlin for mobile visitor and admin workflows.

## Features

- Visitor and condo administrator registration
- Email/password and Google sign-in flows
- JWT-protected API access
- Visitor profile management
- Visit creation with file upload support
- Visit check-in, check-out, cancellation, and status history
- QR generation and QR email delivery
- Admin dashboard, all-visits list, and status history views
- Notifications for visit activity
- Supabase-backed PostgreSQL database and file storage

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.5
- Spring Web, Spring Security, Spring Data JPA, Validation, Mail
- PostgreSQL through Supabase
- Supabase Storage
- JWT authentication
- Google ID token verification
- Maven

### Web

- React 19
- Vite 7
- React Router
- Tailwind CSS
- Axios
- Supabase JavaScript client
- Vitest and Testing Library
- ESLint

### Mobile

- Kotlin
- Android Gradle Plugin 8.3
- AndroidX, AppCompat, Material Components
- Retrofit and OkHttp
- Glide
- AndroidX Security Crypto
- View Binding

## Repository Structure

```text
.
|-- backend/          # Spring Boot API
|-- docs/             # Project documents and reports
|-- mobile/           # Android app
|-- web/knockknock/   # React + Vite web app
`-- README.md
```

## Prerequisites

- Java 17
- Maven, or the included Maven wrapper in `backend/`
- Node.js and npm
- Android Studio for mobile development
- A Supabase project with PostgreSQL and Storage configured
- Google OAuth client ID
- SendGrid API key for email delivery

## Environment Variables

Create environment files locally. Do not commit secrets.

### Backend

The backend reads values from environment variables or a local `.env` file.

```env
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>

SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_KEY=<supabase-service-or-publishable-key>
SUPABASE_STORAGE_BUCKET=kk_files

GOOGLE_CLIENT_ID=<google-client-id>
SENDGRID_API_KEY=<sendgrid-api-key>
JWT_SECRET=<long-random-secret>
```

### Web

Create `web/knockknock/.env`.

```env
VITE_API_URL=http://localhost:8080/api
VITE_SUPABASE_URL=https://<project-ref>.supabase.co
VITE_SUPABASE_PUBLISHABLE_DEFAULT_KEY=<supabase-publishable-key>
VITE_GOOGLE_CLIENT_ID=<google-client-id>
```

### Mobile

The Android app can read Supabase values from `mobile/local.properties`, the repository `.env`, or `web/knockknock/.env`.

```properties
VITE_SUPABASE_URL=https://<project-ref>.supabase.co
VITE_SUPABASE_PUBLISHABLE_DEFAULT_KEY=<supabase-publishable-key>
SUPABASE_STORAGE_BUCKET=kk_files
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/theninanicole/IT342_KnockKnock_G4_Villadarez.git
cd IT342_KnockKnock_G4_Villadarez
```

### 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default.

### 3. Run the Web App

```bash
cd web/knockknock
npm install
npm run dev
```

The Vite development server prints the local web URL, usually `http://localhost:5173`.

### 4. Run the Android App

Open `mobile/` in Android Studio, sync Gradle, and run the `app` configuration on an emulator or physical device.

## Useful Commands

### Backend

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

### Web

```bash
cd web/knockknock
npm run dev
npm run build
npm run lint
npm run test:run
```

### Mobile

```bash
cd mobile
./gradlew test
./gradlew connectedAndroidTest
./gradlew assembleDebug
```

## API Areas

The backend exposes endpoints under `/api`, including:

- `/api/auth` - registration, login, Google OAuth, and current user
- `/api/users` - profile and password management
- `/api/condos` - condo lookup
- `/api/visits` - visit creation, lookup, QR, files, check-in, check-out, and cancellation
- `/api/admin/visits` - admin visit management
- `/api/admin/visits-history` - admin status history
- `/api/notifications` - user notifications

## Branch Naming Convention

All branch names must clearly identify the type, scope, and short description.

### Format

```text
<type>/<scope>/<description>
```

### Rules

- Use lowercase letters.
- Use hyphens for word separation.
- Keep descriptions short and meaningful.
- Avoid vague names like `test`, `temp`, or `new-branch`.
- Do not include personal names.
- Delete branches after merging.

### Types and Scopes

| Type | Prefix | Scope Options | Description | Example |
|------|--------|---------------|-------------|---------|
| Feature | `feature/` | `web/`, `backend/`, `mobile/` | New user-facing functionality | `feature/web/user-profile-ui` |
| Bug Fix | `fix/` | `web/`, `backend/`, `mobile/` | Fixing bugs or unexpected behavior | `fix/backend/auth-endpoint-bug` |
| Technical | `tech/` | `web/`, `backend/`, `mobile/` | Refactoring, optimization, internal improvements | `tech/web/optimize-image-loading` |
| Chore | `chore/` | `web/`, `backend/`, `mobile/` | Maintenance, documentation, minor updates | `chore/backend/update-dependencies` |
| Setup | `setup/` | `web/`, `backend/`, `mobile/`, `infra/`, `deps/` | Initial setup, configuration, environment | `setup/backend/add-flyway-migration` |

### Example

```bash
git checkout -b feature/backend/user-authentication
git add .
git commit -m "feature(backend): Add JWT-based user authentication"
git push origin feature/backend/user-authentication
git branch -d feature/backend/user-authentication
git push origin --delete feature/backend/user-authentication
```

## Documentation

Additional project documents are available in `docs/`, including system design, test planning, phase reports, regression reports, and refactoring documentation.
