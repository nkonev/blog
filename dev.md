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