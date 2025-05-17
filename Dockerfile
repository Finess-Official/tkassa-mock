FROM gradle:8.6.0-jdk21-graal as builder

WORKDIR /tkassa-mock

COPY build.gradle .
RUN gradle dependencies
COPY . .
RUN gradle clean fatJar -x test

FROM eclipse-temurin:21-jre

COPY --from=builder /tkassa-mock/build/libs/app-0.0.1.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]