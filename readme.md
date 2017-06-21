[![Build Status](https://travis-ci.org/nikit-cpp/good-parts.svg?branch=master)](https://travis-ci.org/nikit-cpp/good-parts)

# Requiremants

* Redis 2.8+
* JDK 8

# Building with frontend (just turn on `frontend` profile)
```
./mvnw -P frontend clean verify
```

# Run
```
java -jar frontend/target/frontend-1.0-SNAPSHOT.jar
```

# Changing version
```
./mvnw -DnewVersion=1.0.0 versions:set versions:commit
```

