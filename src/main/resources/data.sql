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


-- Insert sample books
INSERT INTO books (
    title, author, description, price, total_quantity, available_quantity, category, cover_image_url, active, created_at, updated_at
)
SELECT
    'Effective Java',
    'Joshua Bloch',
    'Best practices for Java programming.',
    650.00,
    10,
    10,
    'Programming',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1 FROM books
    WHERE title = 'Effective Java'
      AND author = 'Joshua Bloch'
);

INSERT INTO books (
    title, author, description, price, total_quantity, available_quantity, category, cover_image_url, active, created_at, updated_at
)
SELECT
    'Clean Code',
    'Robert C. Martin',
    'A handbook of agile software craftsmanship.',
    550.00,
    15,
    15,
    'Programming',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1 FROM books
    WHERE title = 'Clean Code'
      AND author = 'Robert C. Martin'
);

INSERT INTO books (
    title, author, description, price, total_quantity, available_quantity, category, cover_image_url, active, created_at, updated_at
)
SELECT
    'Spring in Action',
    'Craig Walls',
    'Learn Spring framework and Spring Boot.',
    720.00,
    20,
    20,
    'Programming',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1 FROM books
    WHERE title = 'Spring in Action'
      AND author = 'Craig Walls'
);