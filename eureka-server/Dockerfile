FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the JAR from the local target folder (built on host)
COPY target/*.jar app.jar
# Clean up potential multiple jars (if any) and keep only the executable one
RUN if [ -f app.jar ]; then echo "JAR copied successfully"; else exit 1; fi
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
