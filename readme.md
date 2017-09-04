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