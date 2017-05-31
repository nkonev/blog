# Requiremants

* Redis 2.8+
* JDK 8

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

# Run

```
java -jar checklist/target/checklist-1.0-SNAPSHOT.jar
```

# Embedded documentation

Embedded documentation are available at http://127.0.0.1:8080/docs/index.html


# Documentation

 * https://github.com/Swagger2Markup/spring-swagger2markup-demo
 * http://docs.spring.io/spring-restdocs/docs/1.2.1.RELEASE/reference/html5/
 * https://springfox.github.io/springfox/docs/current/
