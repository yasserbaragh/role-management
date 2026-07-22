# Role Management

A backend for managing organisations, members, and role-based permissions. Users can create organisations, invite or add other users, assign them roles, and each role carries a set of permissions that gate what its members can do (view/edit roles, memberships, permissions, etc.).

## Stack

- NestJS + TypeORM + PostgreSQL
- Redis (caching)
- JWT auth via httpOnly cookies
- Optional email (SMTP) for "added to org" and "forgot password" notifications

## Running it

### With Docker (easiest)

```bash
cp .env.example .env      # fill in DB_PASSWORD and JWT_SECRET at least
docker compose up --build
```

This starts Postgres, Redis, and the app together. On first run it also seeds the base permission catalog automatically. The app is reachable at `http://localhost:3000`.

To stop: `docker compose down` (add `-v` to also wipe the database volume).

### Without Docker

You need a local Postgres and Redis running yourself.

```bash
cp .env.example .env      # point DB_HOST/REDIS_HOST at your local instances
npm install
npm run start:dev
```

The database schema is created automatically on boot (`synchronize: true`) — no manual migrations needed for local dev. You will need to seed the `permission` table yourself (see `db/init/01-seed-permissions.sql`) if you're not using Docker.

## Project structure

Each domain lives in its own folder under `src/`, following the same pattern: `*.module.ts`, `*.controller.ts`, `*.service.ts`, `entities/`, `dto/`.

- `auth` / `user-table` — accounts, login, JWT
- `organisation` — create/select/delete an organisation, add members
- `organisation-membership` — who belongs to which org, with what role
- `role` / `permission` — roles and the permission catalog
- `memberhsip-invitation` — invite by link or direct add
- `email` — SMTP sending, no-ops if disabled
- `common/guards/roles` — the `RolesGuard` that checks the `@Permissions(...)` decorator on every request

## Making changes

- **Adding an endpoint that needs a permission check:** add `@Permissions('SOME-KEY')` above the controller method. The key must already exist in the `permission` table (add it to `db/init/01-seed-permissions.sql` too, so fresh Docker setups get it).
- **New entity:** add it to the relevant module's `TypeOrmModule.forFeature([...])` — `synchronize: true` will create the table for you, no migration needed.
- **Cross-module access:** most services just inject another module's entity repository directly rather than importing the whole module — follow that existing pattern instead of introducing new module dependencies.
- **Caching:** a few read-heavy services (roles, permissions, the permission guard lookup) cache in Redis via `@Inject(CACHE_MANAGER)`. If you add a write path that changes cached data, make sure to evict the relevant key — see `role.service.ts` for the pattern.
- **Tests:** `npm run test` (Jest, unit tests colocated as `*.spec.ts`).

## Environment variables

See `.env.example` for the full list. Everything except `DB_PASSWORD` and `JWT_SECRET` has a sane default; `SMTP_*` can stay empty as long as `EMAIL_ENABLED=false`.
