# docker build -t chat:latest .
# docker run chat:latest
FROM openjdk:9-jre
ARG APP_HOME=/opt/chat
RUN mkdir -p ${APP_HOME}
WORKDIR ${APP_HOME}
# TODO disabled due not works
# HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3010/actuator/health || exit 1
COPY ./target/*-exec.jar ${APP_HOME}/chat.jar
ENTRYPOINT ["java", "-jar", "chat.jar"]

