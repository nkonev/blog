# docker build -t blog:latest .
# docker run blog:latest
FROM openjdk:9-jre
ARG APP_HOME=/opt/blog
RUN mkdir -p ${APP_HOME}
WORKDIR ${APP_HOME}
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3010/actuator/health || exit 1
COPY ./target/*-exec.jar ${APP_HOME}/blog.jar
ENTRYPOINT ["java", "-jar", "blog.jar"]

