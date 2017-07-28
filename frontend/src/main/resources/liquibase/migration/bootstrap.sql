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

create table roles (
	user_id bigint not null REFERENCES users(id),
	role_name varchar(50) not null,
	UNIQUE(user_id, role_name)
);


-- Group Authorities
CREATE SEQUENCE groups_id_seq;
create table groups (
	id bigint NOT NULL DEFAULT nextval('groups_id_seq') primary key,
	group_name varchar(50) not null
);
ALTER SEQUENCE groups_id_seq OWNED BY groups.id;
ALTER SEQUENCE groups_id_seq MINVALUE 0 START 0 RESTART 0;

create table group_authorities (
	group_id bigint not null,
	authority varchar(50) not null,
	constraint fk_group_authorities_group foreign key(group_id) references groups(id)
);

CREATE SEQUENCE group_members_id_seq;
create table group_members (
	id bigint NOT NULL DEFAULT nextval('group_members_id_seq') primary key,
	username varchar(50) not null,
	group_id bigint not null,
	constraint fk_group_members_group foreign key(group_id) references groups(id)
);
ALTER SEQUENCE group_members_id_seq OWNED BY group_members.id;
ALTER SEQUENCE group_members_id_seq MINVALUE 0 START 0 RESTART 0;


-- Persistent Login (Remember-Me)
create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);


-- ACL Schema
create table acl_sid(
	id bigserial not null primary key,
	principal boolean not null,
	sid varchar(100) not null,
	constraint unique_uk_1 unique(sid,principal)
);

create table acl_class(
	id bigserial not null primary key,
	class varchar(100) not null,
	constraint unique_uk_2 unique(class)
);

create table acl_object_identity(
	id bigserial primary key,
	object_id_class bigint not null,
	object_id_identity bigint not null,
	parent_object bigint,
	owner_sid bigint,
	entries_inheriting boolean not null,
	constraint unique_uk_3 unique(object_id_class,object_id_identity),
	constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id),
	constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id),
	constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)
);

create table acl_entry(
	id bigserial primary key,
	acl_object_identity bigint not null,
	ace_order int not null,
	sid bigint not null,
	mask integer not null,
	granting boolean not null,
	audit_success boolean not null,
	audit_failure boolean not null,
	constraint unique_uk_4 unique(acl_object_identity,ace_order),
	constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id),
	constraint foreign_fk_5 foreign key(sid) references acl_sid(id)
);
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


insert into auth.roles(user_id, role_name) VALUES
	((select id from auth.users where username = 'admin'), 'ROLE_ADMIN'),
	((select id from auth.users where username = 'nikita'), 'ROLE_USER'),
	((select id from auth.users where username = 'alice'), 'ROLE_USER'),
	((select id from auth.users where username = 'bob'), 'ROLE_USER');
-- insert many test user roles
INSERT INTO auth.roles (user_id, role_name)
	SELECT i, 'ROLE_USER'
	FROM generate_series(5, 100000) AS i;

insert into posts.post (title, text, title_img, owner_id)
	SELECT
		'generated_post_' || i,
		'Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		'https://postgrespro.ru/img/logo_mono.png',
		(select id from auth.users where username = 'nikita')
	FROM generate_series(0, 100000) AS i;

