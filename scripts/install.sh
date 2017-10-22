rm -rf blog
mkdir blog
cd blog
rm -rf postgresql
mkdir -p postgresql/docker-entrypoint-initdb.d
cd postgresql/docker-entrypoint-initdb.d
curl -S -s -O https://raw.githubusercontent.com/nkonev/blog/master/docker/postgresql/docker-entrypoint-initdb.d/init-blog-db.sql
cd ../..
curl -S -s -O https://raw.githubusercontent.com/nkonev/blog/master/docker/docker-compose.stack.yml
