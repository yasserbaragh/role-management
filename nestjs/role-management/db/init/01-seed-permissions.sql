-- Seeds the initial permission catalog. Runs once, automatically, only when the
-- postgres container starts with an empty data volume (see docker-entrypoint-initdb.d
-- in the official postgres image docs) - this happens before the app container ever
-- starts, so the "permission" table does not exist yet. It's created here to match
-- what TypeORM would generate from Permission entity; synchronize:true leaves it
-- alone on the app's first boot since the shape already matches.

-- Column types and constraint names below are pinned to match exactly what
-- TypeORM's synchronize generates for this entity (verified against a
-- synchronize-created table: unbounded "character varying", no explicit
-- length). Any mismatch makes synchronize see a diff and try to drop/re-add
-- the "key" column on boot, which fails once rows exist.
CREATE TABLE IF NOT EXISTS permission (
    id SERIAL,
    key character varying NOT NULL,
    label character varying NOT NULL,
    CONSTRAINT "PK_3b8b97af9d9d8807e41e6f48362" PRIMARY KEY (id),
    CONSTRAINT "UQ_20ff45fefbd3a7c04d2572c3bbd" UNIQUE (key)
);

INSERT INTO permission (key, label) VALUES
    ('VIEW-ROLE', 'View roles'),
    ('EDIT-ROLE', 'Create, update and delete roles'),
    ('VIEW-MEMBERSHIP', 'View organisation members'),
    ('EDIT-MEMBERSHIP', 'Invite, assign roles to and remove organisation members'),
    ('VIEW-PERMISSION', 'View the permission catalog'),
    ('VIEW-EXAMPLE', 'View example entities'),
    ('EDIT-EXAMPLE', 'Create, update and delete example entities')
ON CONFLICT (key) DO NOTHING;
