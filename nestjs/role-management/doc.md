# NestJS Rewrite — Completeness Checklist

> Use this to self-check the NestJS version once you've built it. Written from
> what was actually built (and broken, then fixed) in this Spring Boot
> implementation — the items under "Bugs we actually hit" are the ones worth
> testing for explicitly, not just implementing and assuming they work.

## 1. Core domain model

- [ ] `Organization` (or your chosen generic name) carries no business fields —
      just id, name, type, timestamps. Nothing tenant-specific baked in.
- [ ] `Role`: name, `isSystemRole` boolean, belongs to one Organization,
      many-to-many with Permission.
- [ ] `Permission`: unique `key` (e.g. `clients:view`), `label`. No category/enum —
      the full set of permissions must be creatable/renamable/deletable at
      runtime with zero code changes.
- [ ] `OrganizationMembership`: user + organization + role, **one role per
      (user, organization) pair** (unique constraint) — decide up front if you
      ever need multiple roles per membership; retrofitting is painful.

## 2. Multi-tenancy / active-org resolution

- [ ] Decide cookie vs header vs JWT claim for "which org is this request for"
      — a cookie needs an interceptor/middleware to read it every request; a
      JWT claim needs re-issuing the token on org switch. Pick one and be
      consistent everywhere (don't mix cookie-read in some places and a path
      param in others).
- [ ] A request-scoped holder (NestJS: `REQUEST`-scoped provider or
      `AsyncLocalStorage`) exposing the active org id to services/controllers
      without threading it through every method signature manually.
- [ ] Every org-scoped resource lookup **by ID** (not just list endpoints)
      verifies the resource actually belongs to the caller's active org. See
      "Bugs we actually hit" below — this is the one that's easy to forget on
      `GET /:id`, `PATCH /:id`, `DELETE /:id` while getting the list endpoint
      right.

## 3. Dynamic permission catalog

- [ ] Full CRUD: create, list (flat, or grouped if you keep a category field),
      rename, delete.
- [ ] Delete is cascade-safe: strip the permission from every role that holds
      it *before* deleting the row (don't rely on the DB FK to fail loudly, or
      to silently orphan a join-table row).
- [ ] No seeder, no hardcoded enum, no compile-time ceiling on what permissions
      can exist.

## 4. Role CRUD (scoped to organization)

- [ ] Create/list/get-by-id/update/delete, all org-scoped.
- [ ] Delete blocks (409/Conflict, not a raw FK error) if any membership still
      holds the role. Decide: hard block, or offer reassignment-on-delete —
      either is fine, just pick one and document it.
- [ ] Update evicts/refreshes any cached authorization state (see §6).

## 5. Membership + invitations

- [ ] Assign-role and list-memberships endpoints, org-scoped.
- [ ] Revoke-membership endpoint actually exposed via a controller (easy to
      write the service method and forget to route it — happened here).
- [ ] If you add invitations: direct (email-targeted) and link-based
      (multi-use, token, optional expiry, usage cap) are meaningfully
      different enough to model as a `type` field, not two separate tables.
- [ ] Invitation acceptance re-validates status, expiry, and (for direct
      invites) that the accepting email matches the invited email.

## 6. Authorization resolution (the actual security-critical part)

- [ ] Authorities/permissions are computed **per request from the DB/cache**,
      never from a compiled list. A guard or interceptor rebuilds them fresh
      each time the active-org context is established.
- [ ] `isSystemRole` bypass: decide explicitly whether it means "grant
      everything currently in the permission table" (safe, but see the
      bootstrap trap below) or "grant a true wildcard regardless of DB state."
      Both are legitimate — just know which one you're building and why.
- [ ] **Bootstrap trap**: if system-role bypass is DB-driven (`findAll()`
      style) and you later lock down the permission-management endpoints
      themselves behind a permission check, a fresh install with an empty
      permission table can deadlock — nobody, including the owner, can create
      the first permission. Either seed a minimal bootstrap set, leave
      permission-management endpoints open to system-role only via a separate
      check (not the dynamic permission list), or use a true wildcard bypass.
      Decide this consciously, don't discover it in production.

## 7. Caching

- [ ] Cache the per-(user, org) resolved permission set (Redis or equivalent),
      not the raw DB rows.
- [ ] Invalidate on every write that can change the result: role's permission
      set changes, permission renamed/deleted (any role could reference it —
      broad invalidation is fine here, don't over-optimize), membership's role
      reassigned or revoked (targeted invalidation by the specific
      user+org key).
- [ ] Cache invalidation, not cache TTL, is the part worth testing explicitly —
      a stale-permission bug is a security bug (privilege not revoked in
      time), not just a UX bug.

## 8. Auth mechanics

- [ ] If using cookies (JWT-in-cookie or org-selection cookie): the cookie is
      **actually attached to the HTTP response** (`Set-Cookie` header set),
      not just constructed and left unused. See "Bugs we actually hit."
- [ ] Login/registration endpoints are excluded from the tenant-context
      guard/interceptor (they run before any org is selected).

## 9. Optional: opt-in email verification

- [ ] Gated behind a single config flag, default **off**, so the project boots
      and is usable with zero SMTP configuration out of the box.
- [ ] SMTP settings have safe empty defaults so an unconfigured deployment
      doesn't crash at startup — only fails (loudly) if someone actually
      triggers a send with the feature turned on but SMTP unset.
- [ ] Login rejects unverified users only when the flag is on (403, not a
      generic auth error, so a frontend can distinguish "wrong password" from
      "verify your email").

## 10. Bugs we actually hit (test for these explicitly)

- [ ] **Cookie built, never sent**: `ResponseCookie`/equivalent constructed
      but never added to the response headers — code compiles, endpoint
      "works," but nothing is ever set client-side. Caught twice in this repo
      (org-selection cookie, then the JWT login cookie) — check *every*
      cookie-setting endpoint, not just one.
- [ ] **Cross-tenant ID access**: an org-scoped resource's `GET/PATCH/DELETE
      /:id` endpoint fetched by primary key alone, without checking the
      resource's org matches the caller's active org. An authenticated user
      from org A could read/mutate org B's role by guessing/incrementing an
      ID. Test this by creating two orgs and cross-probing IDs.
  - [ ] Also check any **service-to-service** call path that fetches a
        resource by ID (e.g. an invitation service loading a role by
        `roleId` from a request body) — these bypass the controller-level org
        check unless the service itself re-validates against the org it's
        currently operating on.
- [ ] Unguarded management endpoints: permission/role/membership CRUD
      endpoints themselves need their own `@PreAuthorize`/guard — it's easy to
      build the CRUD and forget it's exactly as sensitive as the resources it
      manages.
