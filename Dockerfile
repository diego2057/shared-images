FROM soytul/openjdk:11

COPY ./.docker/entrypoint.sh /entrypoint.sh
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ./entrypoint.sh