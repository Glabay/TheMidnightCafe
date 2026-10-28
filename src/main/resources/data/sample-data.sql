-- Sample data for The Midnight Cafe
-- 10 Users, 2 Employees, 8 Customers

-- Roles
INSERT INTO roles (id, role) VALUES
(X'11111111111111111111111111111101', 'ROLE_ADMIN'),
(X'11111111111111111111111111111102', 'ROLE_MANAGER'),
(X'11111111111111111111111111111103', 'ROLE_BARISTA'),
(X'11111111111111111111111111111104', 'ROLE_EMPLOYEE');

-- Role Permissions
INSERT INTO role_permissions (role_id, permissions) VALUES
(X'11111111111111111111111111111102', 'CUSTOMER_READ'),
(X'11111111111111111111111111111102', 'CUSTOMER_WRITE'),
(X'11111111111111111111111111111102', 'CUSTOMER_EDIT'),
(X'11111111111111111111111111111102', 'CUSTOMER_CREATE'),
(X'11111111111111111111111111111102', 'EMPLOYEE_READ'),
(X'11111111111111111111111111111102', 'EMPLOYEE_EDIT'),
(X'11111111111111111111111111111102', 'PRODUCT_READ'),
(X'11111111111111111111111111111102', 'PRODUCT_WRITE'),
(X'11111111111111111111111111111102', 'PRODUCT_EDIT'),
(X'11111111111111111111111111111102', 'PRODUCT_CREATE'),
(X'11111111111111111111111111111103', 'CUSTOMER_READ'),
(X'11111111111111111111111111111103', 'PRODUCT_READ'),
(X'11111111111111111111111111111103', 'PRODUCT_EDIT'),
(X'11111111111111111111111111111104', 'CUSTOMER_READ'),
(X'11111111111111111111111111111104', 'PRODUCT_READ'),
(X'11111111111111111111111111111104', 'PRODUCT_EDIT');

-- 10 Users (Password: Password123! -> $2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG)
INSERT INTO users (id, identity, password_hash, enabled) VALUES
(X'A0000000000000000000000000000001', 'alice.manager@midnightcafe.dev', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000002', 'bob.barista@midnightcafe.dev', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000003', 'charlie.brown@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000004', 'diana.prince@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000005', 'evan.wright@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000006', 'fiona.gallagher@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000007', 'george.clark@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000008', 'hannah.abbott@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000009', 'ian.malcolm@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1),
(X'A0000000000000000000000000000010', 'julia.roberts@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5I2U4H/x7L1s0r1k1s2.Ueqo2HOKG', 1);

-- Users Roles Mapping
INSERT INTO users_roles (user_id, roles_id) VALUES
(X'A0000000000000000000000000000001', X'11111111111111111111111111111102'),
(X'A0000000000000000000000000000002', X'11111111111111111111111111111103');

-- 2 Employees (linked to User 1 and User 2)
INSERT INTO employees (id, user_id, customer_id, first_name, last_name, email, phone_number, job_title, role, status, hire_date, hourly_rate, created_at, updated_at) VALUES
(
    X'E0000000000000000000000000000001',
    X'A0000000000000000000000000000001',
    NULL,
    'Alice',
    'Manager',
    'alice.manager@midnightcafe.dev',
    '+1-555-0101',
    'General Manager',
    'ROLE_MANAGER',
    'ACTIVE',
    '2024-01-15',
    28.50,
    datetime('now'),
    datetime('now')
),
(
    X'E0000000000000000000000000000002',
    X'A0000000000000000000000000000002',
    NULL,
    'Bob',
    'Barista',
    'bob.barista@midnightcafe.dev',
    '+1-555-0102',
    'Senior Barista',
    'ROLE_BARISTA',
    'ACTIVE',
    '2024-03-01',
    18.75,
    datetime('now'),
    datetime('now')
);

-- 8 Customers (linked to Users 3 through 10)
INSERT INTO customers (id, user_id, first_name, last_name, email, phone_number, preferred_name, birthday, loyalty_points, marketing_opt_in, created_at, updated_at) VALUES
(
    X'C0000000000000000000000000000001',
    X'A0000000000000000000000000000003',
    'Charlie',
    'Brown',
    'charlie.brown@example.com',
    '+1-555-0201',
    'Chuck',
    '1992-04-12',
    150,
    1,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000002',
    X'A0000000000000000000000000000004',
    'Diana',
    'Prince',
    'diana.prince@example.com',
    '+1-555-0202',
    'Di',
    '1988-11-20',
    320,
    1,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000003',
    X'A0000000000000000000000000000005',
    'Evan',
    'Wright',
    'evan.wright@example.com',
    '+1-555-0203',
    'Evan',
    '1995-07-08',
    45,
    0,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000004',
    X'A0000000000000000000000000000006',
    'Fiona',
    'Gallagher',
    'fiona.gallagher@example.com',
    '+1-555-0204',
    'Fee',
    '1990-01-25',
    210,
    1,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000005',
    X'A0000000000000000000000000000007',
    'George',
    'Clark',
    'george.clark@example.com',
    '+1-555-0205',
    'Geo',
    '1985-09-30',
    500,
    1,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000006',
    X'A0000000000000000000000000000008',
    'Hannah',
    'Abbott',
    'hannah.abbott@example.com',
    '+1-555-0206',
    'Han',
    '1998-03-14',
    80,
    0,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000007',
    X'A0000000000000000000000000000009',
    'Ian',
    'Malcolm',
    'ian.malcolm@example.com',
    '+1-555-0207',
    'Ian',
    '1979-12-05',
    640,
    1,
    datetime('now'),
    datetime('now')
),
(
    X'C0000000000000000000000000000008',
    X'A0000000000000000000000000000010',
    'Julia',
    'Roberts',
    'julia.roberts@example.com',
    '+1-555-0208',
    'Jules',
    '1994-06-18',
    125,
    1,
    datetime('now'),
    datetime('now')
);
