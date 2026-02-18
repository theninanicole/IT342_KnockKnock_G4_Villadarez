# 🚪 Knock Knock

**Knock Knock** is a modern visitor log system designed for condominiums. It helps security personnel and property managers efficiently manage, track, and monitor visitor entries in a secure and organized way.

---

## 📌 Overview

Knock Knock digitizes the traditional visitor logbook by providing a web and mobile-based solution that:

- Registers visitor details
- Tracks entry and exit times
- Associates visitors with specific residents/units
- Maintains a searchable visitor history
- Improves security and transparency within residential communities

---

## 🛠 Tech Stack

### Frontend
- React.js – Modern, responsive user interface

### Backend
- Spring Boot – RESTful API and business logic layer

### Database
- To be decided (Planned support for relational or NoSQL database)

---

## 🚀 Features

- Visitor check-in & check-out
- Real-time visitor tracking
- Search & filter visitor logs
- Role-based access (e.g., security, admin, visitors)
- Dashboard & reporting

---

## 🏗 Architecture

Frontend (React.js)  
↓  
REST API (Spring Boot)  
↓  
Database (TBD)

---

## 📦 Installation (Development Setup)

### 1. Clone the Repository

```bash
git clone https://github.com/theninanicole/IT342_KnockKnock_G4_Villadarez.git
cd knock-knock
```

---

## 🌳 Branch Naming Convention

All branch names must clearly identify the **Type**, **Scope**, and a short **Description**.

### Format
```
<type>/<scope>/<description>
```

### Rules
- Use **lowercase letters**
- Use **hyphens (-)** for word separation
- Keep descriptions **short and meaningful**
- Avoid vague names like `test`, `temp`, or `new-branch`
- Do not include personal names
- Delete branches after merging

### Types and Scopes

| Type | Prefix | Scope Options | Description | Example |
|------|--------|---------------|-------------|---------|
| **Feature** | `feature/` | `web/`, `backend/`, `mobile/` | New user-facing functionality | `feature/web/user-profile-ui` |
| **Bug Fix** | `fix/` | `web/`, `backend/`, `mobile/` | Fixing bugs or unexpected behavior | `fix/backend/auth-endpoint-bug` |
| **Technical** | `tech/` | `web/`, `backend/`, `mobile/` | Refactoring, optimization, internal improvements | `tech/web/optimize-image-loading` |
| **Chore** | `chore/` | `web/`, `backend/`, `mobile/` | Maintenance, documentation, minor updates | `chore/backend/update-dependencies` |
| **Setup** | `setup/` | `web/`, `backend/`, `mobile/`, `infra/`, `deps/` | Initial setup, configuration, environment | `setup/backend/add-flyway-migration` |

### Example Usage

```bash
# Create a new feature branch
git checkout -b feature/backend/user-authentication

# Make changes and commit
git add .
git commit -m "Add JWT-based user authentication"

# Push to remote
git push origin feature/backend/user-authentication

# After merging, delete the branch
git branch -d feature/backend/user-authentication
git push origin --delete feature/backend/user-authentication
```
