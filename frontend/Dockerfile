# will be copied to target directory
# docker build -t blog:latest .
# docker run blog:latest
FROM java:openjdk-8-jre
ARG APP_HOME=/opt/blog
RUN mkdir -p ${APP_HOME}
WORKDIR ${APP_HOME}
COPY ./target/*-exec.jar ${APP_HOME}/blog.jar
ENTRYPOINT ["java", "-jar", "blog.jar"]

