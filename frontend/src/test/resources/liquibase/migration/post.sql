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


insert into auth.authorities(username, authority) VALUES
('admin', 'ROLE_ADMIN'),
('nikita', 'ROLE_USER'),
('alice', 'ROLE_USER'),
('bob', 'ROLE_USER');
-- insert many test user roles
INSERT INTO auth.authorities (username, authority)
  SELECT 'generated_user_' || i, 'ROLE_USER'
  FROM generate_series(0, 100000) AS i;

insert into posts.post (title, text, title_img, owner_id)
 SELECT
   'generated_post_' || i,
   'Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
   'https://postgrespro.ru/img/logo_mono.png',
   (select id from auth.users where username = 'nikita')
 FROM generate_series(0, 100000) AS i;