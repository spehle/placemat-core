-- Add audit columns required by AuditableEntity to the roles table.

ALTER TABLE roles
    ADD COLUMN created_on TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_on TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX ix_roles_created_by ON roles (created_by);
CREATE INDEX ix_roles_updated_by ON roles (updated_by);