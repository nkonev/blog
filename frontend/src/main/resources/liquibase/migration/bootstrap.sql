-- liquibase formatted sql
-- changeset nkonev:0_initial_post context:main failOnError: true
CREATE TABLE posts.post  (
  id bigserial PRIMARY KEY,
  title character varying(256) NOT NULL,
  text text NOT NULL,
  title_img character varying(256) NOT NULL,
  UNIQUE (title)
);

-- changeset nkonev:1_initial_spring_security context:main failOnError: true
-- https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#appendix-schema
CREATE SCHEMA auth;

SET search_path = auth, pg_catalog;

-- User Schema
create table users(
  id bigserial primary key,
	username varchar(50) not null unique,
	password varchar(50) not null,
	avatar VARCHAR(256),
	enabled boolean not null DEFAULT true
);

create table authorities (
	username varchar(50) not null,
	authority varchar(50) not null,
	constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index ix_auth_username on authorities (username,authority);


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


-- changeset nkonev:2_test_user context:test failOnError: true
SET search_path = auth, pg_catalog;
-- insert data
insert into users(username, password) VALUES
-- 'admin', sha1('admin')
('admin', 'd033e22ae348aeb5660fc2140aec35850c4da997');

insert into authorities(username, authority) VALUES
('admin', 'ROLE_ADMIN');

SET search_path = public, pg_catalog;

