Prompt: Dynamic Role & Permission Management Module (Spring Boot)

Context — how the existing system works today

This is a Spring Boot + Spring Security + JPA backend for a multi-tenant business app. Tenants are called Entreprise ("enterprise/company"). The current role/permission model:

- Entreprise — the tenant entity. A user can belong to multiple entreprises.
- EntrepriseMembership — join entity: user + entreprise + role. This is how a user's role is scoped per-tenant (a user can be ADMIN in one entreprise and VIEWER in another).
- Role — id, name, belongs to one Entreprise (@ManyToOne), and holds a Set<Permission> via a role_permissions join table.
- Permission — id, name (a plain string like VIEW_CLIENTS, EDIT_ROLES). It's a normal JPA entity, not an enum, so in principle new permissions can be inserted as rows.
- The active tenant for a request is read from a cookie (entrepriseId), not a path variable or header.
- UserPermissonService.getUserPermissionsFromCacheOrDb(email, entrepriseId) loads the user's EntrepriseMembership for that entreprise, pulls the role's permissions, and returns
  their names as a flat list — this becomes the user's Spring Security authorities for that request. It's @Cacheable and evicted on role/permission changes.
- Every controller endpoint is guarded with @PreAuthorize("hasAuthority('VIEW_CLIENTS')")-style annotations — one hardcoded permission-name string per endpoint, repeated across
  ~15 different controllers (clients, commandes, factures, credits, marketing, etc.).
- On startup, a DataSeeder (@Profile("docker")) inserts a fixed, hardcoded list of ~24 permission names (VIEW_CLIENTS, EDIT_CLIENTS, VIEW_ROLES, EDIT_ROLES, ...) into the       
  Permission table. This is the actual problem: even though Permission is a DB row and not a Java enum, the complete set of permissions that will ever exist is fixed at compile   
  time in this seeder list. There's no way for an admin to define a brand-new permission or a brand-new role/permission category without a developer editing this Java list and    
  redeploying.         

    - Role CRUD (RoleService/RoleController) and Permission CRUD (PermissionService/PermissionRestController) already exist as REST APIs, scoped to the entreprise via the cookie.

  The task

  Build a new version of this role/permission system that fixes two things:

    1. Generalize the tenant concept. Rename Entreprise → Organization (or similar generic name — not tied to "company/enterprise" specifically) throughout: the entity, repository,
       service, controller, EntrepriseMembership → OrganizationMembership, the entrepriseId cookie, and all FK columns. Organization should carry no business-specific fields beyond    
       being a tenant boundary (id, name, type, timestamps).
    2. Make permissions truly dynamic, not coded. Remove the hardcoded seeder list entirely. The complete set of permissions that can exist must be fully admin-managed at runtime   
       through a CRUD API — creatable, renamable, categorized, and deletable without ever touching Java code or redeploying. Specifically:
    - Permission gets a key (unique, module:action format, e.g. clients:view), a label, and a category/module field so a frontend can render permissions grouped into checkbox     
      grids (per module) rather than a flat list.    

  - Full CRUD REST API for permissions (create/list-grouped-by-category/update/delete with cascade-safe removal from roles).
      - Full CRUD REST API for roles scoped to an organization (create/update with a set of permission keys, list, delete with reassignment/blocking if members still hold the role).
      - Endpoints to assign/revoke a role on a user's OrganizationMembership.
      - Authorization must still resolve permissions dynamically per-request the way UserPermissonService already does (load from DB/cache by (user, organization), never from a     
        compiled list of authorities) — keep that pattern.
      - Add an isSystemRole (or similar) flag on Role so an organization-owner role automatically passes every permission check without needing every new permission explicitly      
        granted — so a permission created today is usable immediately by owners with zero code changes.
      - It's fine for individual endpoints to still declare which permission key they require in code (that's unavoidable) — what must never require a code change is the set of     
        permissions that exist, their metadata, and who holds them.

  API surface to implement                                                                                                                                                         
  POST   /api/permissions                                                                                                                                                          
  GET    /api/permissions            (grouped by category)                                                                                                                         
  PATCH  /api/permissions/{id}                                                                                                                                                     
  DELETE /api/permissions/{id}

  POST   /api/roles                                                                                                                                                                
  GET    /api/roles                  (scoped to active organization)                                                                                                               
  GET    /api/roles/{id}                                                                                                                                                           
  PATCH  /api/roles/{id}                                                                                                                                                           
  DELETE /api/roles/{id}               

POST   /api/organizations/{id}/memberships/{userId}/role   (assign role)                                                                                                         
GET    /api/organizations/{id}/memberships

Non-goals                                                                                                                                                                        
Don't introduce enum-based role categorization (a plain string/boolean flag is enough). Don't bake any "entreprise/company" business assumptions into Organization.              
                                                                                                                                                                      