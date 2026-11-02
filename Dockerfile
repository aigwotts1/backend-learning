# Compile the application inside Docker; local Java and Maven are not required.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src/main ./src/main
# Database tests run separately in CI before this image is built.
RUN mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

# The runtime image contains the JRE and application, without Maven or source code.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/task-tracker-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
