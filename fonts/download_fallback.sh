#!/bin/bash
# Скачиваем базовые шрифты с известных CDN
wget -O dejavu.ttf "https://sourceforge.net/projects/dejavu/files/dejavu/2.37/dejavu-fonts-ttf-2.37.tar.bz2/download" && \
tar -xf dejavu.ttf 2>/dev/null && \
find . -name "*.ttf" -exec cp {} . \; 2>/dev/null || \
echo "Попробуем другой способ..."

# Проверим что скачалось
ls -la *.ttf 2>/dev/null || echo "Нужен ручной способ"
