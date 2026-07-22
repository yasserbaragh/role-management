# Role Management

Imagine you're building an app where different companies (or teams, or workspaces) sign up, and each one needs to control who on their team can do what. One person should be able to edit things, another should only be able to view them, and the boss should be able to do everything.

That's what this project solves. It gives you:

- **Organizations** — a company, team, or workspace that people belong to. One person can be part of several.
- **Roles** — like "Admin", "Editor", "Viewer" — created by whoever owns the organization.
- **Permissions** — the actual things a role is allowed to do (view members, edit roles, etc.).

The key idea: none of this is hardcoded. An organization's owner can create new roles, decide what each role can and can't do, and invite people in — all without a developer having to write new code for it.

On top of that, it also handles the everyday basics: signing up, logging in, inviting people by email or by a shareable link, and resetting a forgotten password.

## Two versions, same idea

This project exists twice, built with two different technologies, so you can pick whichever fits your stack:

- **`spring-boot/`** — built with Java and Spring Boot
- **`nestjs/`** — built with TypeScript and NestJS

Both do the exact same job in the exact same way. Pick one, open its folder, and check its own README for how to run it.
