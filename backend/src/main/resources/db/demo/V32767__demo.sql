-- insert test data
INSERT INTO auth.users(username, password, avatar, email) VALUES
	('admin', '$2a$10$HsyFGy9IO//nJZxYc2xjDeV/kF7koiPrgIDzPOfgmngKVe9cOyOS2', 'https://cdn3.iconfinder.com/data/icons/rcons-user-action/32/boy-512.png', 'admin@example.com'), -- bcrypt('admin', 10)
	('nikita', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', 'https://cdn3.iconfinder.com/data/icons/users-6/100/654853-user-men-2-512.png', 'nikita@example.com'), -- bcrypt('password', 10)
	('alice', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', 'https://cdn3.iconfinder.com/data/icons/rcons-user-action/32/girl-512.png', 'alice@example.com'),
	('bob', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', NULL, 'bob@example.com'),
	('John Smith', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', NULL, 'jsmith@example.com')
;
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


UPDATE auth.users SET role = 'ROLE_ADMIN' WHERE id = (SELECT id FROM auth.users WHERE username = 'admin');

-- insert additional users and roles
INSERT INTO auth.users(username, password, avatar, email) VALUES
	('forgot-password-user', '$2a$10$e3pEnL2d3RB7jBrlEA3B9eUhayb/bmEG1V35h.4EhdReUAMzlAWxS', NULL, 'forgot-password-user@example.com');


INSERT INTO posts.post (title, text, title_img, owner_id)
	SELECT
		'generated_post_' || i,
		'Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		'https://postgrespro.ru/img/logo_mono.png',
		(SELECT id FROM auth.users WHERE username = 'nikita')
	FROM generate_series(0, 100) AS i;

INSERT INTO posts.comment (text, post_id, owner_id)
	SELECT
		'generated_comment' || i || ' Lorem Ipsum - это текст-"рыба", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной "рыбой" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.',
		(SELECT id from posts.post ORDER BY id DESC LIMIT 1), -- get last id
    CASE
      WHEN i%2 = 0 THEN (SELECT id FROM auth.users WHERE username = 'nikita')
      ELSE (SELECT id FROM auth.users WHERE username = 'alice')
    END
	FROM generate_series(0, 500) AS i;

-- insert additional post with comment and images for delete
INSERT INTO posts.post (title, text, title_img, owner_id) VALUES
	('for delete with comments', 'text. This post will be deleted.', 'https://postgrespro.ru/img/logo_mono.png', (SELECT id FROM auth.users WHERE username = 'nikita'));
INSERT INTO posts.comment (text, post_id, owner_id) VALUES
	('commment', (SELECT id from posts.post ORDER BY id DESC LIMIT 1), (SELECT id FROM auth.users WHERE username = 'alice'));
