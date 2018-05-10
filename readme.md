[![Build Status](https://travis-ci.org/nkonev/blog.svg?branch=master)](https://travis-ci.org/nkonev/blog)
[![codecov](https://codecov.io/gh/nkonev/blog/branch/master/graph/badge.svg)](https://codecov.io/gh/nkonev/blog)
[![](https://images.microbadger.com/badges/image/nkonev/blog.svg)](https://microbadger.com/images/nkonev/blog "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/nkonev/blog.svg)](https://microbadger.com/images/nkonev/blog "Get your own version badge on microbadger.com")

# Requirements

* JDK 10
* Docker 17.09.1-ce +
* docker-compose 1.16.1 +


# FAQ

Q: Can I run it without docker ?

A: Yes, you can bu manually install PostgreSQL, RabbitMQ, Redis and configure it's connections in config or by commandline. 



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


# Demo Run / Installation

```bash
cd docker
./swarm-init.sh
```

Next unlock `--compose-file`
add accords https://github.com/moby/moby/issues/30585#issuecomment-280822231

in `/etc/docker/daemon.json` (create it)
```json
{"experimental":true}
```

```bash
sudo systemctl restart docker
```


I strongly recommend copy and rename `docker-compose.template.yml` to `docker-compose.stack.yml`.
Next I'll use renamed file.



## Copy files on your server:
```bash
scp -r /path/to/blog/docker/* user@blog.test:/path/to/blog/
chmod 600 traefik/acme.json
```


## Manual changes
In `docker-compose.template.yml` or `docker-compose.stack.yml`:

a) Change tag in service blog `image: nkonev/blog:current-test` -> `image: nkonev/blog:latest`

Also you can remove demo profile

b) Change next properties:

```
      - SPRING_MAIL_HOST=smtp.yandex.ru
      - CUSTOM_EMAIL_FROM=username@yandex.ru
      - SPRING_MAIL_USERNAME=username
      - SPRING_MAIL_PASSWORD=password
      - CUSTOM_BASE-URL=http://blog.test
 
```
And remove explicit ports definition where it's don't need - postgres, redis, rabbit, because of docker publishes ports by add it to iptables chain.
If you very want, you can skip setting these properties, but you'll have non-working email, wrong links in emails and so on.

c) Generating monitoring grafana & prometheus password
```bash
sudo yum install -y httpd-tools
# generate login and hash with replaced $ with $$ sign for able to copy-paste to docker-compose.stack.yml
htpasswd -nb admin admin | sed -e 's/\$/\$\$/g'
```

d) Set `journald` logging with appropriate tag for all services

```yaml
    logging:
      driver: "journald"
      options:
        tag: blog
```


e) Uncomment & change SSL setting in `./traefik/traefik.toml`

f) Configure notifications in `./alertmanager/alert.yml`

g) For able to http(s) request your domain registrar name with curl from container
ensure that 
```bash
cat /proc/sys/net/ipv4/ip_forward
```
returns non-zero

next insert iptables rule
```bash
iptables -I INPUT -i docker_gwbridge -p tcp -m multiport --dports 80,443 -j ACCEPT
```

If all ok, you can do it persistent by
```bash
chmod +x /etc/rc.local
vim /etc/rc.local
```

```bash
iptables -I INPUT -i docker_gwbridge -p tcp -m multiport --dports 80,443 -j ACCEPT
echo "Successful inserted docker_gwbridge rule"
```


## Starting with docker

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



# Test on local machine

## curl
```bash
curl -H "Host: blog.test" http://127.0.0.1:8088
curl -H "Host: grafana.blog.test" -u "admin:admin" http://127.0.0.1:8088
curl -H "Host: prometheus.blog.test" -u "admin:admin" http://127.0.0.1:8088
curl -H "Host: alertmanager.blog.test" -u "admin:admin" http://127.0.0.1:8088
```

## Browser

We add domains to /etc/hosts for browser sends correct Host header
```bash
sudo tee --append /etc/hosts <<'EOF'
127.0.0.1 blog.test
127.0.0.1 grafana.blog.test
127.0.0.1 prometheus.blog.test
127.0.0.1 alertmanager.blog.test
EOF
```



# Maintenance

```bash
docker ps -aq | xargs docker rm
docker volume ls -q | xargs docker volume rm
docker images -q -a | xargs  docker rmi
```


## Open PostgreSQL
```bash
docker exec -it $(docker ps --filter label=com.docker.swarm.service.name=BLOGSTACK_postgresql -q) psql -U blog
docker exec -it $(docker ps --filter label=com.docker.swarm.service.name=TESTBLOGSTACK_postgresql -q) psql -U blog
```

## Open blog
```bash
docker exec -it $(docker ps --filter label=com.docker.swarm.service.name=BLOGSTACK_blog -q | head -n 1) bash
```

## Restore PostgreSQL backup

```bash
cat /tmp/blog.sql | docker exec -i $(docker ps --filter label=com.docker.swarm.service.name=BLOGSTACK_postgresql -q) psql -U postgres
```

# SEO
First configure `custom.prerender.prerenderServiceUrl` - setup correct url of Rendertron installation. See also dockerized [build](https://hub.docker.com/r/nkonev/rendertron-docker/).

## How to add SEO metrics scripts

Just prepend `file:` location which contains index.html, and copy modified index.html to there folder.

```yml
spring.resources.static-locations: file:/var/www/, file:backend/src/main/resources/static/, classpath:/static/
```

So firstly Spring Mvc will looking in `/var/www`, next in `$PWD/backend/src/main/resources/static/`...

If your search(Yandex Metrics for example) checks for existence script - request will passed through rendertron, which wipes `<script>` tags.
 
In order to solve it, use `custom.seo.script=file:/var/www/seo.html` - Rendertron filter will inject content of 
this file before closing `</head>`. 