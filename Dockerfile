FROM openjdk:17-alpine
WORKDIR /app

# Клонируем репозиторий (или копируем файлы)
COPY . .
RUN apk add --no-cache maven git && \
    mvn clean package -DskipTests

RUN mkdir -p uploads output fonts

EXPOSE 8080
CMD ["java", "-jar", "target/diploma-generator-1.0.0.jar", "--server.port=8080", "--server.address=0.0.0.0"]
