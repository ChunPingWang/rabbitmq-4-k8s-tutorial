FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY build/libs/rabbitmq-poc-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
