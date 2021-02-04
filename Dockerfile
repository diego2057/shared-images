FROM soytul/openjdk:11.0.8

COPY ./.docker/entrypoint.sh /entrypoint.sh
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ./entrypoint.sh