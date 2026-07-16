# Idea: Fully Dynamic Authorization (data-driven policy engine)

> Status: not planned for this project. This app uses the simpler model (Tier 1/2 below).
> Saved here as a design note to revisit for a future project that needs true no-code,
> runtime-defined resource/action authorization.

## Context: three tiers of "dynamic" permissions

**Tier 1 — dynamic catalog, code-declared enforcement.**
Permission rows (key/label/category) are fully admin-managed via CRUD — creatable,
renamable, deletable at runtime. Each protected endpoint still hardcodes which
permission key it checks (e.g. `@PreAuthorize("hasAuthority('clients:view')")`).
This is how AWS IAM, GitHub, and Stripe do it: the set of possible *actions* is
fixed by what code exists; *who* can do *what* is fully dynamic. Legitimate,
industry-standard default.

**Tier 2 — self-registering permission catalog.**
Replace the hardcoded seeder list with a custom annotation
(`@RequiresPermission(key, label, category)`) on each protected endpoint. A
startup scanner walks all controllers, reads the annotations, and upserts each
into the `Permission` table (insert if missing, never delete, so admin edits to
labels survive redeploys). The moment a developer writes a protected endpoint,
its permission exists in the DB — no separate list to maintain, self-documenting.
This is the approach chosen for this project.

**Tier 3 — fully dynamic authorization.** Described below.

## Core idea

Nothing about *what a permission means or what it applies to* is compiled —
not the permission catalog, not the resource types, not the rules connecting
them. Only the single act of "ask the engine before doing X" stays as code,
because a running program has to call *something* at the point where access
matters.

## Core model shift: from `Role → Permission` to `Rule(Subject, Resource, Action, Effect)`

Instead of a role holding a flat set of permission keys, store standalone rules:

| subject (role/group) | resourceType | action  | effect | condition (optional)          | priority |
|-----------------------|--------------|---------|--------|--------------------------------|----------|
| Manager                | invoice      | approve | allow  | `resource.amount < 10000`      | 10       |
| Manager                | invoice      | approve | deny   | `resource.status == "locked"`  | 20       |

- **resourceType** and **action** are plain strings, not enums, not Java
  classes — an org (or the consuming app, at onboarding) registers its own
  vocabulary of resource types and actions at runtime. Nothing in the engine's
  code knows what "invoice" or "approve" mean.
- **effect** is allow/deny, with an explicit conflict-resolution rule
  (typically: deny wins over allow, higher priority wins ties) — this is what
  turns a bag of rules into a deterministic decision.
- **condition** is optional and is what upgrades this from RBAC to ABAC — a
  small, safe expression language evaluated against a context object (the
  resource's current fields, maybe the requester's attributes) rather than
  Java code. This is the genuinely hard part: it needs a tiny, sandboxed
  expression evaluator (JSON-logic-style, or a restricted grammar) — never
  `eval` a string as code.

## How code still talks to it (the one unavoidable code touchpoint)

Every consuming app calls a single generic entry point instead of an
annotation tied to one permission string:

```
POST /authorize
{ subject: "user:42", org: "org:7", resourceType: "invoice", action: "approve", context: {...} }
→ { allow: true|false, matchedRule: "..." }
```

That call site is still "code," but it's *the same call everywhere* — the
thing that varies (resourceType/action/rules) is 100% data. This is what makes
it usable from any language/stack over HTTP, not just from one app's own
codebase — a real standalone product rather than a library.

## Making the catalog self-populating

Since resourceType/action strings aren't hardcoded, the engine has no idea
what vocabulary exists until something registers it. Two options:

- **Explicit registration API** — consuming apps declare their resource
  types/actions once (like an OpenAPI spec) so the admin UI has something to
  build rules against.
- **Auto-vivify on first use** — the first `/authorize` call with an unseen
  `resourceType`/`action` pair silently creates a catalog entry (unlabeled),
  and an admin later fills in the friendly label/category — same
  self-registering idea as Tier 2, just applied to resource+action instead of
  just permission keys.

## Why this is a bigger project than Tier 1/2

- Needs a real conflict-resolution algorithm (deny-overrides vs priority vs
  first-match) — get this wrong and it's silently insecure.
- Needs a rule *compiler/cache*: evaluating a growing rule table row-by-row
  per request doesn't scale — precompute/cache a resolved rule set per
  (subject, org) and invalidate on writes. Harder than flat-permission caching
  because rule counts grow faster than permission counts.
- Needs a safe condition/expression language — a security-sensitive component
  in its own right (injection risk if done carelessly).
- Needs multi-tenant isolation of rule sets and resource-type vocabularies so
  one org's schema doesn't leak into another's.

## Prior art worth studying before building this

- **Open Policy Agent (OPA/Rego)** — general-purpose policy-as-code engine,
  the reference design for "decouple policy from application."
- **Casbin** — explicitly separates *model* (how subjects/resources/actions
  combine) from *policy* (the actual rule rows); supports ACL/RBAC/ABAC under
  one framework.
- **Cerbos** — resource-centric YAML policies, purpose-built as a standalone
  authorization microservice (closest in spirit to the network-callable PDP
  described above).
- **OpenFGA / Google Zanzibar** — relationship-based access control (ReBAC), a
  different dynamic model worth knowing about (permission derived from graph
  relationships rather than rule tables) — e.g. "can view if owner OR is a
  member of a team that has access."

## When to reach for this

Only when a project's actual requirement is "admins/tenants define entirely
new kinds of protected things at runtime" — not just "admins manage who holds
pre-existing permissions," which is what Tier 1/2 already solves well without
the added complexity above.