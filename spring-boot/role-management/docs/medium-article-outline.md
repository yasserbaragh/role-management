# Medium Article Outline — "Building Dynamic Role & Permission Management for Multi-Tenant Apps"

> Points to touch on, roughly in narrative order. Not a draft — an outline of
> what's worth covering, based on what was actually designed/built/broken/fixed
> in this project. The concrete bugs/decisions below make better article
> material than generic RBAC theory, since they're specific and verifiable.

## 1. Hook — the problem with the "obvious" approach

- Most tutorials model permissions as a Java `enum` or TypeScript union type.
  Works until: a customer asks for a custom role, or "just one more
  permission" requires a code change + redeploy for every tenant.
- The real requirement in multi-tenant SaaS: *who* holds *what* permission
  must be 100% admin-managed at runtime. Contrast with: *what permissions
  exist as concepts* — that's more nuanced than "also make it dynamic," see §3.

## 2. The core data model

- `Organization` (tenant), `Role` (per-org), `Permission` (global catalog),
  `Membership` (user + org + role, one role per pair).
- Why Permission is a **global** catalog but Role is **per-org**: permissions
  are "what capabilities the software has," roles are "how one tenant
  organizes who gets which capabilities." Worth a diagram.
- Multi-tenancy plumbing: how "which org is this request for" gets resolved
  (cookie/header/JWT claim) and propagated through a request-scoped context
  instead of threading an orgId parameter through every layer.

## 3. What "dynamic permissions" actually means (and doesn't)

- The honest nuance: individual endpoints still hardcode *which* permission
  key they require (`@PreAuthorize("hasAuthority('clients:view')")`) — that's
  unavoidable, code has to call *something*. What's dynamic is the catalog:
  creating, renaming, categorizing, deleting permissions, and who holds them.
- This is exactly how AWS IAM, GitHub, and Stripe do it — the set of possible
  *actions* is fixed by what code exists; *who* can do *what* is fully
  runtime-configurable. Worth naming this explicitly so readers don't expect
  magic.
- Brief mention of the further-out alternative — a fully data-driven
  Rule/ABAC policy engine (Casbin, OPA, Cerbos) where even resource
  types/actions are runtime-registered — and why that's overkill for "admins
  manage who holds pre-existing permissions," the far more common need.

## 4. The system-role bootstrap trap (great concrete gotcha)

- The `isSystemRole` flag exists so an org owner doesn't need every new
  permission explicitly granted — a permission created today should be usable
  by owners immediately.
- The trap: if that bypass works by enumerating "everything currently in the
  permission table," a fresh install with an *empty* table means even the
  owner has zero authorities — including the authority needed to create the
  first permission. Nobody can bootstrap.
- Two ways out (true wildcard bypass vs. a minimal seed set vs. leaving
  bootstrap endpoints open to system-role only) — walk through the tradeoff
  instead of presenting one as "the" answer. This is a genuinely good
  "I hit this, here's the fix" section.

## 5. Caching authorization — and why invalidation is the hard part

- Resolving permissions from the DB on every single request is wasteful;
  caching the resolved (user, org) → permission-set is the obvious fix.
- The part that's actually hard: **invalidation**, not caching. A stale
  permission cache after a role's permissions change (or a user's role is
  reassigned) isn't a performance bug, it's a security bug — a revoked
  permission that's still honored for N more minutes.
- Concrete rule of thumb used here: broad invalidation (clear the whole
  cache) on catalog-level changes (role/permission edited), targeted
  invalidation (evict one user+org key) on membership-level changes
  (role assigned/revoked) — cheap to reason about, correct by construction.

## 6. Two real bugs worth a "lessons learned" section

- **Cookie built, never sent.** `ResponseCookie.from(...).build()` produces an
  object — it does nothing until you actually attach it to the response
  headers. Compiles fine, "works" in a manual test if you're not checking
  response headers, silently does nothing. Found this bug *twice* in the same
  codebase (org-selection cookie, then login) — a good prompt for "test your
  auth flows by inspecting actual HTTP responses, not just status codes."
- **Cross-tenant ID access (IDOR).** An org-scoped resource fetched by ID
  alone (`GET /roles/:id`) without checking the resource's org matches the
  caller's active org — an authenticated user from Org A could read/mutate
  Org B's role by guessing an ID. Easy to get the *list* endpoint right
  (it's naturally org-filtered) and forget the *by-id* endpoints, which is
  exactly what happened here. Also worth noting: internal service-to-service
  lookups (e.g. an invitation service loading a role by ID from a request
  body) need the same check — it's not just a controller-layer concern.

## 7. Making it safe to open-source / self-host

- Feature flags for anything that needs external config (SMTP for email
  verification) — default off, safe empty defaults, so a fresh clone boots
  and is usable with zero configuration. Contrast with failing hard at
  startup because a required property is unset.
- General principle: "opt-in with a sane off-state" beats "required config"
  for anything not core to the product running at all.

## 8. Closing — what this pattern is (and isn't) good for

- This (Tier-1/2 dynamic-catalog RBAC) covers the vast majority of real SaaS
  permission needs. Reach for a full policy engine only when tenants need to
  define entirely new *kinds* of protected things at runtime, not just manage
  who holds existing permissions — that's a materially bigger project
  (rule conflict resolution, safe condition/expression evaluation, per-tenant
  rule-set isolation).
- CTA: link to repo / offer to write the "when you actually need ABAC"
  follow-up.
