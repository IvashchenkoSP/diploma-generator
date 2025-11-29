#!/bin/bash
echo "🔍 Поиск кириллических шрифтов в системе..."

# Ищем в стандартных расположениях
FONT_PATHS=(
    "/usr/share/fonts"
    "/usr/local/share/fonts" 
    "/home/$USER/.local/share/fonts"
    "/home/$USER/.fonts"
)

for path in "${FONT_PATHS[@]}"; do
    if [ -d "$path" ]; then
        echo "📁 Проверяем: $path"
        find "$path" -name "*.ttf" -o -name "*.otf" 2>/dev/null | \
        while read font; do
            if fc-query "$font" 2>/dev/null | grep -qi "cyrillic"; then
                echo "✅ Кириллический: $font"
                # Копируем в текущую директорию
                cp "$font" ./
                echo "📋 Скопирован: $(basename "$font")"
            fi
        done
    fi
done

echo "📊 Найдены файлы:"
ls -la *.ttf *.otf 2>/dev/null || echo "❌ Шрифты не найдены"
