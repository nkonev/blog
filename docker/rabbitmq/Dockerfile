# https://github.com/docker-library/rabbitmq/blob/master/3.6/debian/management/Dockerfile
# https://www.rabbitmq.com/web-stomp.html
FROM rabbitmq:3.6.12-management
RUN rabbitmq-plugins enable --offline rabbitmq_web_stomp
EXPOSE 15674 61613