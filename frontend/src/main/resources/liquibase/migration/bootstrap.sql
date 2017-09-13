-- liquibase formatted sql

-- changeset nkonev:0_drop_schemas context:test failOnError: true
DROP SCHEMA IF EXISTS auth cascade;
DROP SCHEMA IF EXISTS posts cascade;
DROP SCHEMA IF EXISTS historical cascade;

-- changeset nkonev:0_initial_spring_security context:main failOnError: true
-- https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#appendix-schema
CREATE SCHEMA auth;
CREATE SCHEMA posts;
CREATE SCHEMA historical;

SET search_path = auth, pg_catalog;

-- User Schema
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
	username VARCHAR(50) NOT NULL UNIQUE,
	password VARCHAR(100) NOT NULL,
	avatar VARCHAR(256),
	enabled BOOLEAN NOT NULL DEFAULT TRUE,
	expired BOOLEAN NOT NULL DEFAULT FALSE,
	locked BOOLEAN NOT NULL DEFAULT FALSE,
	email VARCHAR(100) NOT NULL
);

CREATE TABLE user_roles (
	user_id BIGINT NOT NULL REFERENCES users(id),
	role_id INT NOT NULL, -- enum UserRole value starts with 0
	UNIQUE(user_id, role_id)
);

/*
-- Persistent Login (Remember-Me)
CREATE TABLE persistent_logins (
	username VARCHAR(64) NOT NULL,
	series VARCHAR(64) PRIMARY KEY,
	token VARCHAR(64) NOT NULL,
	last_used TIMESTAMP NOT NULL
);
*/

SET search_path = public, pg_catalog;


-- changeset nkonev:1_initial_post context:main failOnError: true
CREATE TABLE posts.post (
  id BIGSERIAL PRIMARY KEY,
  title CHARACTER VARYING(256) NOT NULL,
  text TEXT NOT NULL,
  title_img TEXT NOT NULL,
  owner_id BIGINT NOT NULL REFERENCES auth.users(id),
  UNIQUE (title)
);

CREATE TABLE posts.comment (
  id BIGSERIAL PRIMARY KEY,
  text TEXT NOT NULL,
  post_id BIGINT NOT NULL REFERENCES posts.post(id),
  owner_id BIGINT NOT NULL REFERENCES auth.users(id)
);

CREATE TABLE historical.password_reset_token (
	uuid UUID PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES auth.users(id),
	expired_at timestamp NOT NULL
);

CREATE TABLE posts.post_title_image (
	post_id BIGINT PRIMARY KEY REFERENCES posts.post(id),
	img BYTEA
);

-- changeset nkonev:2_test_data context:test failOnError: true
-- insert test data
INSERT INTO auth.users(username, password, avatar, email) VALUES
	('admin', '$2a$10$HsyFGy9IO//nJZxYc2xjDeV/kF7koiPrgIDzPOfgmngKVe9cOyOS2', 'https://cdn3.iconfinder.com/data/icons/rcons-user-action/32/boy-512.png', 'admin@example.com'), -- bcrypt('admin', 10)
	('nikita', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', 'https://cdn3.iconfinder.com/data/icons/users-6/100/654853-user-men-2-512.png', 'nikita@example.com'), -- bcrypt('password', 10)
	('alice', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', 'https://cdn3.iconfinder.com/data/icons/rcons-user-action/32/girl-512.png', 'alice@example.com'),
	('bob', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', NULL, 'bob@example.com');
-- insert many test users
INSERT INTO auth.users (username, password, avatar, email)
	SELECT
    'generated_user_' || i,
    '$2a$10$0nGRZ4Quy0hW2W.prjc.AOyUkNqgFulVckZQ.gFsOly5ESntrW7E.', -- bcrypt('generated_user_password', 10)
    CASE
      WHEN i % 2 = 0 THEN 'https://cdn3.iconfinder.com/data/icons/avatars-9/145/Avatar_Alien-512.png'
      ELSE NULL
    END,
		'generated' || i || '@example.com'
	FROM generate_series(0, 1000) AS i;

/**
enum UserRole:
0 ROLE_ADMIN
1 ROLE_USER
 */

INSERT INTO auth.user_roles(user_id, role_id) VALUES
	((SELECT id FROM auth.users WHERE username = 'admin'), 0),
	((SELECT id FROM auth.users WHERE username = 'nikita'), 1),
	((SELECT id FROM auth.users WHERE username = 'alice'), 1),
	((SELECT id FROM auth.users WHERE username = 'bob'), 1);
-- insert many test user roles
INSERT INTO auth.user_roles (user_id, role_id)
	SELECT i, 1
	FROM generate_series(5, 1000) AS i;

-- insert additional users and roles
INSERT INTO auth.users(username, password, avatar, email) VALUES
	('forgive-password-user', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', NULL, 'forgive-password-user@example.com');
INSERT INTO auth.user_roles(user_id, role_id) VALUES
	((SELECT id FROM auth.users WHERE username = 'forgive-password-user'), 1);


INSERT INTO posts.post (title, text, title_img, owner_id)
	SELECT
		'generated_post_' || i,
		'Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		'https://postgrespro.ru/img/logo_mono.png',
		(SELECT id FROM auth.users WHERE username = 'nikita')
	FROM generate_series(0, 2000) AS i;

INSERT INTO posts.comment (text, post_id, owner_id)
	SELECT
		'generated_comment' || i || ' Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		(SELECT id from posts.post ORDER BY id DESC LIMIT 1), -- get last id
    CASE
      WHEN i%2 = 0 THEN (SELECT id FROM auth.users WHERE username = 'nikita')
      ELSE (SELECT id FROM auth.users WHERE username = 'alice')
    END
	FROM generate_series(0, 500) AS i;

-- insert additional post with comment for delete
INSERT INTO posts.post (title, text, title_img, owner_id) VALUES
	('for delete with comments', 'text. This post will be deleted.', 'https://postgrespro.ru/img/logo_mono.png', (SELECT id FROM auth.users WHERE username = 'nikita'));

INSERT INTO posts.comment (text, post_id, owner_id) VALUES
	('commment', (SELECT id from posts.post ORDER BY id DESC LIMIT 1), (SELECT id FROM auth.users WHERE username = 'alice'));