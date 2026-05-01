INSERT INTO roles (name, description)
SELECT 'LEARNER', 'Learner role'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'LEARNER');

INSERT INTO roles (name, description)
SELECT 'CONTENT_MANAGER', 'Content manager role'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CONTENT_MANAGER');

INSERT INTO roles (name, description)
SELECT 'ADMIN', 'Administrator role'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (name, description)
SELECT 'SUPER_ADMIN', 'System owner role'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SUPER_ADMIN');

INSERT INTO permissions (code, description)
SELECT 'USER_READ', 'Read users'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'USER_READ');

INSERT INTO permissions (code, description)
SELECT 'USER_MANAGE_STATUS', 'Lock and unlock users'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'USER_MANAGE_STATUS');

INSERT INTO permissions (code, description)
SELECT 'DECK_CREATE', 'Create decks'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DECK_CREATE');

INSERT INTO permissions (code, description)
SELECT 'DECK_READ', 'Read decks'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DECK_READ');

INSERT INTO permissions (code, description)
SELECT 'DECK_UPDATE_OWN', 'Update own decks'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DECK_UPDATE_OWN');

INSERT INTO permissions (code, description)
SELECT 'DECK_DELETE_OWN', 'Delete own decks'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DECK_DELETE_OWN');

INSERT INTO permissions (code, description)
SELECT 'DECK_APPROVE', 'Approve public decks'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'DECK_APPROVE');

INSERT INTO permissions (code, description)
SELECT 'FLASHCARD_CREATE', 'Create flashcards'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'FLASHCARD_CREATE');

INSERT INTO permissions (code, description)
SELECT 'FLASHCARD_UPDATE_OWN', 'Update own flashcards'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'FLASHCARD_UPDATE_OWN');

INSERT INTO permissions (code, description)
SELECT 'LEARNING_REVIEW', 'Submit learning reviews'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'LEARNING_REVIEW');

INSERT INTO permissions (code, description)
SELECT 'ADMIN_DASHBOARD_READ', 'Read admin dashboard'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_DASHBOARD_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'DECK_CREATE',
    'DECK_READ',
    'DECK_UPDATE_OWN',
    'DECK_DELETE_OWN',
    'FLASHCARD_CREATE',
    'FLASHCARD_UPDATE_OWN',
    'LEARNING_REVIEW'
)
WHERE r.name = 'LEARNER'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'DECK_CREATE',
    'DECK_READ',
    'DECK_UPDATE_OWN',
    'DECK_DELETE_OWN',
    'DECK_APPROVE',
    'FLASHCARD_CREATE',
    'FLASHCARD_UPDATE_OWN',
    'LEARNING_REVIEW'
)
WHERE r.name = 'CONTENT_MANAGER'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('ADMIN', 'SUPER_ADMIN')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
