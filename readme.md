[![Build Status](https://travis-ci.org/nikit-cpp/good-parts.svg?branch=master)](https://travis-ci.org/nikit-cpp/good-parts)

# Requiremants

* Redis 2.8+
* JDK 8

# Building with frontend (just turn on `frontend` profile)
```
./mvnw -P frontend clean package
```

# Building without frontend and without webdriver tests
```
./mvnw clean package
```


# Run
```
java -jar frontend/target/frontend-1.0-SNAPSHOT-exec.jar
```

# Changing version
```
./mvnw -DnewVersion=1.0.0 versions:set versions:commit
```

# Check for update maven dependency versions
```
./mvnw -DlogOutput=false versions:display-dependency-updates | less
```

# Frontend development

## Run webpack
```
cd frontend
./webpack.sh
```

## Run KarmaJS with PhantomJS browser (Chrome will be if not specified)
```
cd frontend
./karma.sh start --browsers=PhantomJS
```

## Update js dependencies

https://www.npmjs.com/package/npm-check-updates

```
ncu -u
rm package-lock.json
./npm.sh install
```
