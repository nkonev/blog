ALTER TABLE auth.users RENAME COLUMN role TO roles;

alter table auth.users
    alter roles drop default,
    alter roles type auth.user_role[] using array[roles],
    alter roles set default ARRAY['ROLE_USER']::auth.user_role[];