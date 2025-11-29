FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем исходный код
COPY . .

# Устанавливаем Maven и собираем проект
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Создаем необходимые папки
RUN mkdir -p uploads output fonts

# Копируем шрифты если они есть
COPY fonts/ ./fonts/

# Открываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "target/diploma-generator-1.0.0.jar"]
