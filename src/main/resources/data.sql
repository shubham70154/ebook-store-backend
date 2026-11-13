-- Insert default roles
INSERT INTO roles (name)
SELECT 'CUSTOMER'
    WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'CUSTOMER'
);

INSERT INTO roles (name)
SELECT 'ADMIN'
    WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ADMIN'
);

-- Insert default Admin user
INSERT INTO users (
    username,
    email,
    password,
    enabled,
    created_at
)
SELECT
    'admin',
    'admin@ebookstore.com',
    '$2a$10$LP5QSc2MRqQ4AchaG/UnrOM/a6sxT2Dq.yLRzuEFBlEFQWvwZuHxW',
    TRUE,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
       OR email = 'admin@ebookstore.com'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
);