-- Seeds the initial permission catalog. Runs once, automatically, only when the
-- postgres container starts with an empty data volume (see docker-entrypoint-initdb.d
-- in the official postgres image docs) - this happens before the app container ever
-- starts, so the `permission` table does not exist yet. It's created here to match
-- what Hibernate would generate from Permission.java; ddl-auto=update leaves it alone
-- on the app's first boot since the shape already matches.

CREATE TABLE IF NOT EXISTS permission (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL
);

INSERT INTO permission (key, label) VALUES
    ('VIEW_ROLES', 'View roles'),
    ('EDIT_ROLES', 'Create, update and delete roles'),
    ('VIEW_MEMBERSHIPS', 'View organisation members'),
    ('EDIT_MEMBERSHIPS', 'Invite, assign roles to and remove organisation members'),
    ('VIEW_PERMISSIONS', 'View the permission catalog'),
    ('VIEW_EXAMPLE', 'View example entities'),
    ('EDIT_EXAMPLE', 'Create, update and delete example entities')
ON CONFLICT (key) DO NOTHING;
