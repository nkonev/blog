-- ALTER SYSTEM SET max_connections = 400;
-- Uncomment if you need to view the full postgres logs (SQL statements, ...) via `docker logs -f postgresql-test`
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET synchronous_commit = 'off'; -- https://postgrespro.ru/docs/postgrespro/9.5/runtime-config-wal.html#GUC-SYNCHRONOUS-COMMIT
-- ALTER SYSTEM SET shared_buffers='512MB';
ALTER SYSTEM SET fsync=FALSE;
ALTER SYSTEM SET full_page_writes=FALSE;
ALTER SYSTEM SET commit_delay=100000;
ALTER SYSTEM SET commit_siblings=10;
-- ALTER SYSTEM SET work_mem='50MB';
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';

create user blog with password 'blogPazZw0rd';
create database blog with owner blog;
\connect blog;

-- https://www.postgresql.org/docs/current/static/pgstatstatements.html
create extension if not exists "pg_stat_statements";
create extension if not exists "uuid-ossp";
-- create extension if not exists "hstore" schema pg_catalog;

