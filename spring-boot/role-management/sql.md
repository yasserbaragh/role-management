Here's the full menu of options for getting permissions populated on a fresh clone, roughly ordered from "zero extra code" to "most sophisticated":

1. Nothing extra — just document the manual API calls

Ship a Postman/Insomnia/Bruno collection (or a curl script) in the repo that walks through POST /api/permissions a few times, then POST /api/roles, etc. Zero backend code. Purest expression of "fully admin-managed" but the least frictionless — first-time cloners have to run something external before the app does anything useful.

2. data.sql — Spring Boot's built-in, zero-dependency seed mechanism

Spring Boot will run src/main/resources/data.sql automatically against the datasource on startup if you set spring.sql.init.mode=always (it's off by default for non-embedded databases like your Postgres setup). Write it idempotently:
INSERT INTO permission (key, label, category) VALUES
('clients:view', 'View Clients', 'Clients'),
('clients:edit', 'Edit Clients', 'Clients')
ON CONFLICT (key) DO NOTHING;
No new dependency, no Java code, works the moment someone clones and runs the app. The main caveat: it runs on every boot, so it must stay ON CONFLICT DO NOTHING forever, and it doesn't play well if you later adopt Hibernate ddl-auto=validate/migrations, since data.sql assumes the schema already exists by the time it runs.

3. CommandLineRunner reading a bundled YAML/JSON seed file

Same idea as #2 but in Java: a runner reads src/main/resources/seed/permissions.yml at boot and upserts (insert-if-missing, never overwrite/delete) via your existing PermissionRepository. Slightly more code than data.sql, but easier to make truly non-destructive (real if findByKey(...).isEmpty() logic instead of relying on a DB constraint), and it's just editing a data file, not Java logic, to add a starter permission.

4. Flyway/Liquibase versioned seed migration

A V2__seed_permissions.sql with the same idempotent inserts, run by Flyway automatically as part of schema migration. This is the most "production-grade" answer, but it's a bigger decision than just permissions — you're currently on spring.jpa.hibernate.ddl-auto=update with no migration tool at all, so adopting this means also deciding to move schema management off Hibernate auto-DDL generally. Worth doing eventually for a repo meant to be cloned and run by others (reproducible schema is a real quality signal), but it's a separate architectural step, not something to bolt on just for permissions.

5. @RequiresPermission annotation + startup scanner (the Tier 2 idea from earlier)

This solves a different problem than "ease of cloning" — it keeps the catalog in sync with code as the project grows, so nobody has to remember to also edit a seed file when they add a new protected endpoint. Worth building eventually, but on its own it doesn't give a fresh clone any starter roles, an org, or a login — it only registers permission metadata for endpoints that already exist.

6. Docker Compose + a bootstrap step

You don't have a docker-compose.yml yet at all. Adding one (Postgres + app) turns "clone → configure DB → run" into docker compose up. Combine with #2 or #3 for permissions, and optionally add an init step (a small script, or an entrypoint on a throwaway container) that also registers a demo user, creates a demo organisation, and assigns the Admin role — so docker compose up leaves you with a fully working, loggable-into demo environment, not just an empty schema.

7. Makefile / setup script as the outer wrapper

Not a seeding mechanism itself, just the ergonomic layer on top: make up → starts Docker Compose, waits for health, runs the seed, prints example credentials/curl commands. This is what actually delivers "as easy as possible for people cloning the repo" — one command, no manual steps, no need to read code first to figure out the right order of API calls.

8. Swagger/OpenAPI UI (springdoc-openapi)

Not seeding either, but worth mentioning: exposing an interactive API UI means cloners can explore and call your permission/role CRUD endpoints without needing Postman or reading controller source — lowers friction regardless of which seeding approach you pick.

My recommendation for this project specifically

Given you want low effort now and this to look polished to someone cloning it cold: #2 (data.sql with ON CONFLICT DO NOTHING) + #6 (docker-compose.yml, since you don't have one) + a short README quickstart. That's the smallest amount of new infrastructure that takes a fresh clone from git clone to "logged in, org created, permissions visible" with one command and zero manual API fumbling. Layer Flyway (#4) and the annotation scanner (#5) on top later — both are real upgrades, but they're about long-term schema/catalog correctness, not first-run ergonomics, so they shouldn't block getting a working demo out the door now.
