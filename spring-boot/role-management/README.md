# Role Management

A backend that lets any app add "organizations, roles and permissions" to itself, without hardcoding who's allowed to do what.

## What it actually does

Most apps end up with permission checks like `@PreAuthorize("hasAuthority('EDIT_CLIENTS')")` sprinkled everywhere, and a fixed list of permissions baked into the code. This project flips that: **permissions and roles live in the database, not in the code.**

- **Organizations** are tenants — think "workspace" or "company," but generic. A user can belong to several.
- **Roles** belong to one organization and hold a set of **permissions** (e.g. `EDIT_ROLES`, `VIEW_MEMBERSHIPS`).
- **Permissions** are just rows in the database (`key`, `label`) — an admin can look at what exists, but new ones are added directly in the database, not through the app. What roles get built from that catalog, and who holds which role, is fully dynamic.
- One special case: an organization's **owner** role bypasses permission checks entirely and can do anything in their org — including using permissions that get added to the system after their role was created.

On top of that, the app has normal account stuff: register/login (JWT-based), optional email verification, password reset, inviting people into an organization (by email or by a shareable link), and managing who's a member of what.

## How it works, mechanically

1. You log in and get a JWT (stored as an `httpOnly` cookie).
2. You pick which organization you're acting in (also a cookie) — most endpoints are scoped to "the organization you're currently in."
3. On each request, the app looks up your role in that organization, resolves it to a list of permissions, and checks that against what the endpoint requires. This lookup is cached in **Redis** so it's not hitting the database on every single request.
4. **Postgres** is the actual source of truth for everything (users, organizations, roles, permissions, memberships, invitations).

## Running it (the easy way — Docker)

You need [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running. Nothing else.

```bash
cp .env.example .env
```
Open `.env` and fill in `POSTGRES_PASSWORD` and `JWT_SECRET` with your own values (anything works for local use, just don't leave them as `changeme`). Leave the `MAIL_*` lines empty and `EMAIL_VERIFICATION_ENABLED=false` unless you actually have an SMTP server to point at.

Then:
```bash
docker compose up --build
```

First run takes a few minutes (downloading images, building the app, Maven pulling dependencies). Once you see `Started RoleManagementApplication` in the logs, it's ready at **http://localhost:8080**.

To stop it:
```bash
docker compose down
```
Add `-v` to that if you also want to wipe the database (useful if you want to start completely fresh).

This spins up three things automatically: the app itself, a Postgres database, and Redis — you don't need to install any of them yourself.

## Running it locally without Docker (for active development)

If you're editing code and want faster feedback than rebuilding a Docker image every time:

1. Have Postgres and Redis running somewhere reachable (locally installed, or just run `docker compose up postgres redis` to get only those two from the compose file, without the app).
2. Fill in `src/main/resources/application-local.properties` with your local DB connection details.
3. Run the app from your IDE (IntelliJ, VS Code, etc.) or with `./mvnw spring-boot:run`.

## Project layout, if you want to change something

It's a fairly standard Spring Boot app, organized by feature rather than by layer:

- `organisation/` — the tenant entity itself
- `organisationMemberhsip/` — who belongs to which organization, with what role
- `organisationInvitation/` — inviting people in, by email or by link
- `role/` and `permission/` — the role/permission system described above
- `userTable/` — user accounts: register, login, profile, password reset
- `config/` — security, JWT, Redis, and other cross-cutting setup
- `entityExample/` — not a real feature, just a small worked example showing how a brand-new module should plug into the org/role/permission system. Good place to look if you're adding your own module.

Permissions themselves aren't created through the API — new permission keys are added directly to the `permission` table in the database (see `db/init/01-seed-permissions.sql` for the starting set, and the seed script comment for why it's done that way).

## Requirements if you're not using Docker at all

- Java 25
- Postgres
- Redis
- Maven (or just use the included `./mvnw` wrapper, no separate install needed)
