-- liquibase formatted sql

-- changeset nkonev:0_drop_schemas context:test failOnError: true
DROP SCHEMA IF EXISTS auth cascade;
DROP SCHEMA IF EXISTS posts cascade;

-- changeset nkonev:0_initial_spring_security context:main failOnError: true
-- https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#appendix-schema
CREATE SCHEMA auth;
CREATE SCHEMA posts;

SET search_path = auth, pg_catalog;

-- User Schema
create table users (
  id bigserial primary key,
	username varchar(50) not null unique,
	password varchar(50) not null,
	avatar VARCHAR(256),
	enabled boolean not null DEFAULT true,
	expired boolean not null DEFAULT false,
	locked boolean not null DEFAULT false
);

create table user_roles (
	user_id bigint not null REFERENCES users(id),
	role_id INT not null, -- enum UserRole value starts with 0
	UNIQUE(user_id, role_id)
);


-- Group Authorities
/*

-- Persistent Login (Remember-Me)
create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);
*/

SET search_path = public, pg_catalog;


-- changeset nkonev:1_initial_post context:main failOnError: true
CREATE TABLE posts.post  (
  id bigserial PRIMARY KEY,
  title character varying(256) NOT NULL,
  text text NOT NULL,
  title_img character varying(256) NOT NULL,
  owner_id bigint NOT NULL,
  UNIQUE (title)
);

-- liquibase formatted sql

-- changeset nkonev:2_test_data context:test failOnError: true
-- insert test data
insert into auth.users(username, password) VALUES
	('admin', 'd033e22ae348aeb5660fc2140aec35850c4da997'), -- sha1('admin')
	('nikita', '5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8'), -- sha1('password')
	('alice', '5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8'),
	('bob', '5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8');
-- insert many test users
INSERT INTO auth.users (username, password)
	SELECT 'generated_user_' || i, '9831ec1837ce68115596609d36785cf4bd77f6c2' -- sha1('generated_user_password')
	FROM generate_series(0, 100000) AS i;

/**
enum UserRole:
0 ROLE_ADMIN
1 ROLE_USER
 */

insert into auth.user_roles(user_id, role_id) VALUES
	((select id from auth.users where username = 'admin'), 0),
	((select id from auth.users where username = 'nikita'), 1),
	((select id from auth.users where username = 'alice'), 1),
	((select id from auth.users where username = 'bob'), 1);
-- insert many test user roles
INSERT INTO auth.user_roles (user_id, role_id)
	SELECT i, 1
	FROM generate_series(5, 100000) AS i;

insert into posts.post (title, text, title_img, owner_id)
	SELECT
		'generated_post_' || i,
		'Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		'https://postgrespro.ru/img/logo_mono.png',
		(select id from auth.users where username = 'nikita')
	FROM generate_series(0, 100000) AS i;

