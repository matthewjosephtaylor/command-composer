FROM openjdk:8-jre-alpine

LABEL maintainer "https://twitter.com/matt_taylor"

COPY target/composer-single-jar.jar /app/

WORKDIR /app

ENTRYPOINT ["java", "-jar", "/app/composer-single-jar.jar"]
