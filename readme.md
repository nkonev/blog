[![Build Status](https://travis-ci.org/nikit-cpp/good-parts.svg?branch=master)](https://travis-ci.org/nikit-cpp/good-parts)

# Requiremants

* Redis 2.8+
* JDK 8

# Building with frontend (just turn on `frontend` profile) and integration tests (`verify`)
```
./mvnw -P frontend clean package verify
```

# Run
```
java -jar frontend/target/frontend-1.0-SNAPSHOT-exec.jar
```

# Changing version
```
./mvnw -DnewVersion=1.0.0 versions:set versions:commit
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
