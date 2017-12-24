[![Build Status](https://travis-ci.org/nkonev/blog.svg?branch=master)](https://travis-ci.org/nkonev/blog)
[![codecov](https://codecov.io/gh/nkonev/blog/branch/master/graph/badge.svg)](https://codecov.io/gh/nkonev/blog)

# Requirements

* JDK 8
* Docker 17.05.0-ce +
* docker-compose 1.16.0 +

# Start test environment

```bash
(cd docker; docker-compose up -d)
```

# Building with frontend and build docker image

There is highly recommends to shut down your application on 8080, although tests uses 9080, some of
them can fails, with websocket for example.
```bash
./mvnw -P frontend -P docker clean package
```

As you can see, there is switches via maven profiles.

This will run tests on PhantomJS.
See `.travis.yml` for Firefox and Chrome test examples

## Building without frontend and without webdriver tests
```bash
./mvnw clean package
```

## Build and start docker image for development
```bash
cd docker
docker-compose -f docker-compose.yml -f docker-compose.nginx.yml -f docker-compose.dev.yml up -d --build
```

# Run
```bash
# By default
java -jar frontend/target/frontend-1.0-SNAPSHOT-exec.jar
# .. or with pre-generated content
java -Dliquibase.contexts=main,test -jar frontend/target/frontend-1.0-SNAPSHOT-exec.jar
```

Test user credentials can be found in `frontend/src/main/resources/liquibase/migration/bootstrap.sql`:74

# Development
## Changing version
```
./mvnw -DnewVersion=1.0.0 versions:set versions:commit
```

## Check for update maven dependency versions
```
./mvnw -DlogOutput=false -DprocessDependencyManagement=false versions:display-dependency-updates | less
./mvnw -DlogOutput=false versions:display-property-updates | less

./mvnw -DlogOutput=false versions:display-plugin-updates | less

```

## Frontend development

### Run webpack
```
cd frontend
npm run dev
```

### Run Jest
```
cd frontend
npm run test
```

### Update js dependencies

https://www.npmjs.com/package/npm-check-updates

```
ncu -u
rm package-lock.json
npm install
```

## Embedded documentation

Embedded documentation are available at `http://127.0.0.1:8080/docs/index.html`


## Request version info 

This will available after full package, e. g. after resource filtering of `git.template.json` and renaming result in `target/classes/static` dir to `git.json`

```
curl -i http://127.0.0.1:8080/git.json
```

# Running on Windows without docker.

First you should
install redis, postgres, Rabbit MQ 
and manually setup them (create database, schema user for Postgres, install web stomp plugin and create user for RabbitMQ).

Redis Windows x86 which works on my PC
http://bitsandpieces.it/redis-x86-32bit-builds-for-windows
2.8.2104 http://fratuz610.s3.amazonaws.com/upload/public/redis-builds/x86/redis-windows-x86-2.8.2104.zip

run
```
redis-server.exe --maxheap 8Mb
```


Next you should use localhost IP addresses and disable asciidoctor:
```
mvnw -P local -Dasciidoctor.skip=true clean test
```

# FAQ

Q: I suddenly get http 403 error in JUnit mockMvc tests.

A: Add `.with(csrf())` to MockMvcRequestBuilder chain


# Demo Run / Installation

```bash
docker swarm init
docker network create --driver=overlay proxy_backend
```

First unlock `--compose-file`
add accords https://github.com/moby/moby/issues/30585#issuecomment-280822231

in `/etc/docker/daemon.json` (create it)
```json
{"experimental":true}
```

```bash
sudo systemctl restart docker
```

Second add constraint on database node (It can be manager) for prevent instantiate db containers on another nodes for prevent data loss:
```bash
# Run it on node which will be database
docker node update --label-add 'blog.server.role=db' $(docker node ls -q)

# check it
docker node inspect -f '{{index .Spec.Labels "blog.server.role"}}' $(docker node ls -q)
```

Copy on server
```bash
scp -r /path/to/blog/docker/* user@blog.test:/path/to/blog/
chmod 600 traefik/acme.json
```

## Generating monitoring grafana & prometheus password
```bash
sudo yum install -y httpd-tools
# generate login and hash with replaced $ with $$ sign for able to copy-paste to docker-compose.stack.yml
htpasswd -nb admin admin | sed -e 's/\$/\$\$/g'
```

I strongly recommend copy and rename `docker-compose.template.yml` to `docker-compose.stack.yml`.
And correctly setup next properties:

```
      - liquibase.contexts=main
      - spring.mail.host=smtp.yandex.ru
      - custom.email.from=username@yandex.ru
      - spring.mail.username=username
      - spring.mail.password=password
      - custom.base-url=http://blog.test
 
```
And remove explicit ports definition where it's don't need - postgres, redis, rabbit, because of docker publishes ports by add it to iptables chain.
If you very want, you can skip setting these properties, but you'll have non-working email, wrong links in emails and so on.

Also you need set host in `./docker/prometheus/prometheus.yml:53`

Next I'll use renamed file.

Copy files on our server:
```
.
|-- docker-compose.stack.yml
`-- postgresql
    `-- docker-entrypoint-initdb.d
        `-- init-blog-db.sql
```

Next you can 
```bash
docker stack deploy --compose-file docker-compose.stack.yml BLOGSTACK
docker service scale BLOGSTACK_blog=4
docker service ls
```

See postgres volume
```bash
docker volume inspect BLOGSTACK_postgresql_blog_dev_data_dir
```

See logs of jars
via journalctl (see applied tags in `docker-compose.stack.yml`):
```bash
journalctl -f CONTAINER_TAG=blog
journalctl -f CONTAINER_TAG=blog -o verbose
journalctl -f CONTAINER_TAG=blog CONTAINER_TAG=postgresql CONTAINER_TAG=redis CONTAINER_TAG=rabbitmq
```

or via docker
```bash
docker service logs -f BLOGSTACK_blog
```

Remove
```bash
docker stack rm BLOGSTACK
```

Remove exited containers
```bash
docker rm $(docker ps -aq -f name=BLOGSTACK_blog -f status=exited)
```

# Test

## curl
```bash
curl -H "Host: blog.test" http://127.0.0.1:8088
curl -H "Host: grafana.blog.test" -u "admin:admin" http://127.0.0.1:8088
curl -H "Host: prometheus.blog.test" -u "admin:admin" http://127.0.0.1:8088
```

## Browser

```bash
echo -e '127.0.0.1 blog.test\n127.0.0.1 grafana.blog.test\n127.0.0.1 prometheus.blog.test' | sudo tee --append /etc/hosts
```

# Run `boot-run`
```bash
./mvnw clean spring-boot:run
```

# Maintenance

```bash
docker ps -aq | xargs docker rm
docker volume ls -q | xargs docker volume rm
docker images -q -a | xargs  docker rmi
```

## Restore PostgreSQL backup

```bash
cat /tmp/blog.sql | docker exec -i $(docker ps --filter label=com.docker.swarm.service.name=BLOGSTACK_postgresql -q) psql -U postgres
```

## clean prerender cache

```bash
docker exec -t $(docker ps --filter label=com.docker.swarm.service.name=BLOGSTACK_prerender -q) redis-cli flushdb
```