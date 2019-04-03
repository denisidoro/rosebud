FROM clojure:openjdk-8-lein-2.9.1-alpine AS build-env
WORKDIR /app
COPY server/project.clj /app/project.clj
RUN lein deps
COPY server/ /app
RUN lein uberjar

FROM openjdk:8-jre-alpine
WORKDIR /myapp
COPY --from=build-env /app/target/rosebud.jar /app/rosebud.jar
ENTRYPOINT ["java", "-jar", "/app/rosebud.jar"]
