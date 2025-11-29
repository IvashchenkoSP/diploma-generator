FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/diploma-generator-1.0.0.jar app.jar
COPY fonts/ ./fonts/
RUN mkdir -p uploads output
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
