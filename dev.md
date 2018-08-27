# Start develop environment

```bash
(cd docker; docker-compose up -d)
```


# Install maven wrapper

```bash
mvn -N io.takari:maven:wrapper
```

# Run tests and generate report
```bash
./mvnw clean package site
```

# Building (with doc)

```bash
./mvnw clean package
```

# Documentation

 * https://github.com/Swagger2Markup/spring-swagger2markup-demo
 * http://docs.spring.io/spring-restdocs/docs/1.2.1.RELEASE/reference/html5/
 * https://springfox.github.io/springfox/docs/current/

# execute frontend plugin goal

```bash
./mvnw -pl frontend frontend:install-node-and-npm
```


For get cli analogs of any `<configuration>` property see its Expression (Ctrl + Q in IntelliJ IDEA)
and pass it with `-D`:

```bash
./mvnw -Dfrontend.npm.arguments=version -pl frontend frontend:npm
```

```bash
./mvnw -Dfrontend.npm.arguments='run dev' -pl frontend frontend:npm
```

# Docker-compose
```bash
docker-compose -f docker-compose.yml -f docker-compose.demo.yml up -d
```

```bash
- "./mvnw -Dkarma.browsers=Firefox -Dcustom.selenium.browser=FIREFOX -P frontend clean package"
- "./mvnw -Dkarma.browsers=Chrome -Dcustom.selenium.browser=CHROME -P frontend clean package"
```

# Jest
You should clear jest cache after change `.babelrc`
See [here](https://github.com/facebook/jest/issues/2442#issuecomment-269654883).

To see where is jest cache you can
```bash
jest --showConfig  | grep -i cache
```

# Show goals on phase
```bash
mvn help:describe -Dcmd=compile
```

https://stackoverflow.com/questions/1709625/maven-command-to-list-lifecycle-phases-along-with-bound-goals/35610377#35610377
```bash
./mvnw fr.jcgay.maven.plugins:buildplan-maven-plugin:list-phase
```

# Restore dump
```bash
echo 'drop database blog;' | docker exec -i postgresql-blog-dev psql -U postgres
cat ~/blog/$(ls ~/blog | grep backup- | sort | tail -n 1)/blog.sql | docker exec -i postgresql-blog-dev psql -U postgres
```

# Install
```bash
./mvnw -DskipTests clean install
```

# Java 9
https://dou.ua/lenta/articles/problems-with-java-9/
https://stackoverflow.com/questions/43258796/hibernate-support-for-java-9

# Pull up exists database
Add in docker yml
```    command: ["--spring.flyway.baselineOnMigrate=true"]```
```    command: ["--spring.flyway.baselineOnMigrate=true", "--spring.flyway.baselineVersion=32767"]``` if demo profile enabled




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
java -jar frontend/target/frontend-1.0.3-SNAPSHOT-exec.jar
# .. or with pre-generated content
java -jar frontend/target/frontend-1.0.3-SNAPSHOT-exec.jar --spring.profiles.active=demo
```

Test user credentials can be found in `backend/src/main/resources/db/demo/V32767__demo.sql`:4

# Development
## Changing version
```bash
./mvnw -DnewVersion=1.0.0 versions:set versions:commit
```

## Check for update maven dependency versions
```bash
./mvnw -DlogOutput=false -DprocessDependencyManagement=false versions:display-dependency-updates | less
./mvnw -DlogOutput=false versions:display-property-updates | less

./mvnw -DlogOutput=false versions:display-plugin-updates | less

```

# Run `boot-run`
```bash
./mvnw clean spring-boot:run
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

#### clean Jest cache
```bash
# where is it
npm run test -- --showConfig | grep cache
rm -rf /var/tmp/jest_rs
```

### Update js dependencies

https://www.npmjs.com/package/npm-check-updates

```
ncu -u
rm package-lock.json
npm install
```

# FAQ

Q: I suddenly get http 403 error in JUnit mockMvc tests.

A: Add `.with(csrf())` to MockMvcRequestBuilder chain


# Code coverage

1. Add profile `jacoco` `./mvnw -Pjacoco clean package`
2. See coverage HTML report in `./jacoco/target/site/jacoco-aggregate/index.html` directory.

# Elasticsearch
```bash
curl -X GET    -H "Content-Type:application/json"    -d '{
  "query": {
        "prefix": {
          "text": "почт"
        }
  }
}'  'http://127.0.0.1:19200/blog/_search' | jq
```

```bash
curl -X GET    -H "Content-Type:application/json"    -d '{
  "query": {
    "bool":{
      "should": [
         {"match_phrase_prefix": { "text": "почт" }},
         {"match_phrase_prefix": { "title": "почт" }}
      ]
    }
  },
  "_source": ["_"],
  "highlight" : {
        "fields" : {
            "text" : { "fragment_size" : 150, "pre_tags" : ["<em>"], "post_tags" : ["</em>"], "number_of_fragments" : 5 },
            "title" : { "fragment_size" : 150, "pre_tags" : ["<em>"], "post_tags" : ["</em>"], "number_of_fragments" : 5 }
        }
  }
}'  'http://127.0.0.1:19200/blog/_search' | jq
```

```bash
curl -X GET    -H "Content-Type:application/json"    -d '{
  "query": {
    "bool":{
      "should": [
         {"match_phrase_prefix": { "text": "bas" }},
         {"match_phrase_prefix": { "title": "bas" }}
      ]
    }
  },
  "_source": ["_"],
  "highlight" : {
        "fields" : {
            "text" : { "fragment_size" : 150, "pre_tags" : ["<em>"], "post_tags" : ["</em>"], "number_of_fragments" : 5 },
            "title" : { "fragment_size" : 150, "pre_tags" : ["<em>"], "post_tags" : ["</em>"], "number_of_fragments" : 1 }
        }
  }
}'  'http://127.0.0.1:19200/blog/_search' | jq
```