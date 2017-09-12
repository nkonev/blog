[![Build Status](https://travis-ci.org/nikit-cpp/blog.svg?branch=master)](https://travis-ci.org/nikit-cpp/blog)

# Requirements

* JDK 8
* Docker 1.12.3 +
* docker-compose 1.16.0 +

# @Before

```bash
(cd docker; docker-compose up -d)
```

# Building with frontend (just turn on `frontend` profile)

There is highly recommends to shut down your application on 8080, although tests uses 8090, some of
them can fails, with websocket for example.
```bash
./mvnw -P frontend clean package
```

This will run tests on PhantomJS.
See `.travis.yml` for Firefox and Chrome test examples

## Building without frontend and without webdriver tests
```bash
./mvnw clean package
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

### Run KarmaJS with PhantomJS browser (Chrome will be if not specified)
```
cd frontend
npm run unit
# or
npm run unit -- --browsers=Chrome
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

First you should install redis, postgres, Rabbit MQ and setup them (create database, schema user for Postgres, install web stomp plugin and create user for RabbitMQ).

Redis Windows x86 which works on my PC
http://bitsandpieces.it/redis-x86-32bit-builds-for-windows
2.8.2104 http://fratuz610.s3.amazonaws.com/upload/public/redis-builds/x86/redis-windows-x86-2.8.2104.zip

run
```
redis-server.exe --maxheap 8Mb
```

Next you can run with redefine IP addessses to localhost and disable asciidoctor:
```
mvnw -Dasciidoctor.skip=true -Dcustom.stomp.broker.host=127.0.0.1 -Dspring.datasource.url=jdbc:postgresql://127.0.0.1:5432/blog?connectTimeout=10 -Dspring.redis.url=redis://127.0.0.1:6379/0 clean package -e
```

Or you can use shortcut
```
mvnw -P local clean test
```

# FAQ

Q: I suddenly get http 403 error in JUnit mockMvc tests.

A: Add `.with(csrf())` to MockMvcRequestBuilder chain

