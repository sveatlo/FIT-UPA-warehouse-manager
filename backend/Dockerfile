FROM openjdk:14-jdk-alpine

WORKDIR /app
VOLUME ["/app"]

RUN apk --no-cache add bash

CMD ./gradlew dependencies && ./gradlew run
