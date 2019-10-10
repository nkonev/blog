[![Build Status](https://travis-ci.com/nkonev/blog.svg?branch=master)](https://travis-ci.com/nkonev/blog)
[![codecov](https://codecov.io/gh/nkonev/blog/branch/master/graph/badge.svg)](https://codecov.io/gh/nkonev/blog)
[![](https://images.microbadger.com/badges/image/nkonev/blog.svg)](https://microbadger.com/images/nkonev/blog "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/nkonev/blog.svg)](https://microbadger.com/images/nkonev/blog "Get your own version badge on microbadger.com")

# Features
* Zero-downtime update deployment
* Fast page loading due client-side rendering
* Fulltext search by posts
* Updating posts through web STOMP on main page
* Draft posts that visible only for author and administrator
* User locking
* User deletion (with migrating posts to special `deleted` user)
* Pages prerendering for crawlers with [rendertron](https://github.com/nkonev/rendertron-docker)
* Dynamically setting header, subheader and background image without server restart
* Auto cleaning "orphanned" images from PostgreSQL, and "orphaned" posts from Elasticsearch
* Cluster out from the box - simple scale it with `docker service scale BLOGSTACK_blog=4`
* Login through Facebook, Vkontakte OAuth2 providers
* Binding several OAuth2 account to same blog account
* Simply installation with docker swarm
* Applications like Vkontakte/Facebook apps. Example [storage application](https://github.com/nkonev/blog-storage) on Go
* Self-sufficient frontend asset. No CDN used
* Simply [backup](https://github.com/nkonev/blog/blob/master/dev.md#take-dump) of everything to one .sql file

# Requirements

## Run
* Docker 18.06.0+

## Development
* JDK 12
* docker-compose 1.16.1 +
* Google Chrome (as [default](https://github.com/nkonev/blog/blob/master/webdriver-test/src/test/resources/config/application.yml#L88) browser for webdriver-test). Just `dnf install chromium` in latest Fedora.

# FAQ

Q: Can I run it without docker ?

A: Yes, you can achieve it by manually install PostgreSQL, RabbitMQ, Redis, Elasticsearch and configure it's connections in config or through commandline. See Spring Boot documentation https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/html/boot-features-external-config.html.

Q: How to build frontend if I am backend developer ?

A: 
```bash
./mvnw -P frontend generate-resources
```

Q: How to build full jar (with static) ?

A: 
```bash
./mvnw -P frontend clean package
```
It will download java dependencies and nodejs with frontend dependencies.


Q: Why does blog wait for PostgreSQL, Elasticsearch, Redis, RabbltMQ port availability on boot?

A: Primarily for deploy tests runned inside Travis. When there isn' t these waits, I had spirously tests fails due inpredictable time of Elasticsearch boot.


## Embedded API documentation

Embedded documentation are available at `http://127.0.0.1:8080/docs/index.html`


## Request version info 

This will available after full package, e. g. after resource filtering of `git.template.json` and renaming result in `target/classes/static` dir to `git.json`

```
curl -i http://127.0.0.1:8080/git.json
```

# Running on Windows without docker

First you should install Redis, PostgreSQL, Rabbit MQ, Elasticsearch
and manually setup them (create database, schema, user for PostgreSQL, install web stomp plugin and create user for RabbitMQ).

Redis Windows x86 which works on my PC (Windows 7 x86)
http://bitsandpieces.it/redis-x86-32bit-builds-for-windows

2.8.2104 http://fratuz610.s3.amazonaws.com/upload/public/redis-builds/x86/redis-windows-x86-2.8.2104.zip - requires enabled swapfile.

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

I strongly recommend copy and rename `docker-compose.template.yml` to `docker-compose.stack.yml`.
Next I'll use renamed file.



## Copy files on your server:
```bash
scp -r /path/to/blog/docker/* user@blog.test:/path/to/blog/
chmod 600 traefik/acme.json
```


## Manual changes

Let' s assume `cd docker`.

a) `./swarm-init.sh`

b) In `docker-compose.template.yml` or `docker-compose.stack.yml`:.

Change tag in service blog `image: nkonev/blog:current-test` -> `image: nkonev/blog:latest`

Also you can remove demo profile

c) Change next properties:

```
      - SPRING_MAIL_HOST=smtp.yandex.ru
      - CUSTOM_EMAIL_FROM=username@yandex.ru
      - SPRING_MAIL_USERNAME=username
      - SPRING_MAIL_PASSWORD=password
      - CUSTOM_BASE-URL=http://blog.test
 
```
And remove explicit ports definition where it's don't need - postgres, redis, rabbit, because of docker publishes ports by add it to iptables chain.
If you very want, you can skip setting these properties, but you'll have non-working email, wrong links in emails and so on.

d) Generating monitoring grafana & prometheus password
```bash
sudo yum install -y httpd-tools
# generate login and hash with replaced $ with $$ sign for able to copy-paste to docker-compose.stack.yml
htpasswd -nb admin admin | sed -e 's/\$/\$\$/g'
```

e) Set `journald` logging with appropriate tag for all services

```yaml
    logging:
      driver: "journald"
      options:
        tag: blog
```


f) Uncomment & change SSL setting in `./traefik/traefik.toml`

g) Configure notifications in `./alertmanager/alert.yml`

i) For able to http(s) request your domain registrar name with curl from container
ensure that 
```bash
cat /proc/sys/net/ipv4/ip_forward
```
returns non-zero

next 

Option a)
```bash
firewall-cmd --permanent --zone=public --add-port=80/tcp
firewall-cmd --permanent --zone=public --add-port=443/tcp
firewall-cmd --reload
```

Check
```bash
firewall-cmd --list-all-zones
iptables -t nat --line-numbers --numeric --list
```

Option b) insert iptables rule
```bash
iptables -I INPUT -i docker_gwbridge -p tcp -m multiport --dports 80,443 -j ACCEPT
```

If all ok, you should do it persistent by
```bash
chmod +x /etc/rc.local
vim /etc/rc.local
```

```bash
iptables -I INPUT -i docker_gwbridge -p tcp -m multiport --dports 80,443 -j ACCEPT
echo "Successful inserted docker_gwbridge rule"
```


## Starting with docker swarm

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


# SEO
First configure `custom.rendertron.serviceUrl` - setup correct url of Rendertron installation. See also dockerized [build](https://hub.docker.com/r/nkonev/rendertron-docker/).

## How to add SEO metrics scripts

Just prepend `file:` location which contains index.html, and copy modified index.html to there folder.

```yml
spring.resources.static-locations: file:/var/www/, file:backend/src/main/resources/static/, classpath:/static/
```

So firstly Spring Mvc will looking in `/var/www`, next in `$PWD/backend/src/main/resources/static/`...

If your search(Yandex Metrics for example) checks for existence script - request will passed through rendertron, which wipes `<script>` tags.
 
In order to solve it, use `custom.seo.script=file:/var/www/seo.html` - Rendertron filter will inject content of 
this file before closing `</head>`. 

# Grafana

## Fix disk usage in https://grafana.com/dashboards/1860


Set query
```100 - ((node_filesystem_avail_bytes{mountpoint="/rootfs"} * 100) / node_filesystem_size_bytes{mountpoint="/rootfs"})```

Set Instant


# TODO
* re-implement buttons css
* sitemap for SEO
* edit metainfo for SEO by user
* change post owner by admin
* change comment owner by admin
* LDAP
* Google OAuth2 login
* search by comments
