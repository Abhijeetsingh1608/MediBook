FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build

COPY . .
RUN mvn -pl notification-service -am clean package -DskipTests
RUN cp "$(find /build/notification-service/target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" /tmp/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
