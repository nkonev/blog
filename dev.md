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

# Call goal in sub-project

```
./mvnw -pl frontend frontend:install-node-and-npm
```
