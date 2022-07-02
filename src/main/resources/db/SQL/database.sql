-- Table: users
CREATE TABLE users (
    login VARCHAR(255) NOT NULL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Table: roles
CREATE TABLE roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Table for mapping user and roles: user_roles
CREATE TABLE user_roles (
    user_login VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY (user_login) REFERENCES users (login),
    FOREIGN KEY (role_id) REFERENCES roles (id),
    UNIQUE (user_login, role_id)
);

INSERT INTO roles (id, name) VALUES
(1, 'Admin'),
(2, 'User');

INSERT INTO users (login, username, password) VALUES
('wer', 'Spring', 'S2p3ring');

INSERT INTO user_roles (user_login, role_id) VALUES
('wer', 1),
('wer', 2);

INSERT INTO roles (id, name) VALUES
(3, 'Operator'),
(4, 'Analyst');

INSERT INTO users (login, username, password) VALUES
    ('1', '1', 'S2p3ring11'),
    ('2', '2', 'S2p3ring22'),
    ('3', '3', 'S2p3ring33');

INSERT INTO user_roles (user_login, role_id) VALUES
('1', 1),
('1', 2),
('1', 3),
('2', 4),
('2', 2),
('2', 3),
('3', 2),
('3', 4);