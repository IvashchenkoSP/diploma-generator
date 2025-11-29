#!/bin/bash
echo "🚀 Подготовка к деплою на Railway..."

# Проверяем сборку
echo "🔨 Проверяем сборку..."
mvn clean package

if [ ! -f "target/diploma-generator-1.0.0.jar" ]; then
    echo "❌ Ошибка: JAR файл не создан"
    exit 1
fi

echo "✅ Сборка успешна!"
echo ""
echo "📋 Инструкция по деплою:"
echo "1. Создайте аккаунт на https://railway.app"
echo "2. Нажмите 'New Project' -> 'Deploy from GitHub repo'"
echo "3. Подключите ваш GitHub репозиторий"
echo "4. Railway автоматически определит Java приложение"
echo "5. После деплоя получите публичный URL"
echo ""
echo "📁 Файлы для деплоя:"
echo "✅ pom.xml"
echo "✅ railway.json" 
echo "✅ system.properties"
echo "✅ target/diploma-generator-1.0.0.jar"
echo ""
echo "🌐 После деплоя приложение будет доступно по ссылке типа:"
echo "   https://your-project-name.up.railway.app"
