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