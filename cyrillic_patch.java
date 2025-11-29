// ИМПОРТЫ - добавить в начало файла с другими импортами:
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.File;

// В методе generateDiplomaFromTemplate ЗАМЕНИТЬ блок с добавлением текста:
public String generateDiplomaFromTemplate(String templatePath, String fullName, 
                                        float posX, float posY, float fontSize) throws IOException {
    System.out.println("🎯 Генерация из шаблона: " + templatePath);
    
    if (!Files.exists(Paths.get(templatePath))) {
        throw new IOException("Шаблон не найден: " + templatePath);
    }

    String outputFileName = UUID.randomUUID() + "_diploma.pdf";
    String outputPath = outputDir + outputFileName;

    // Загружаем шаблон PDF
    try (PDDocument document = PDDocument.load(new File(templatePath))) {
        PDPage page = document.getPage(0);
        
        // Загружаем кириллический шрифт (пробуем разные пути)
        PDType0Font cyrillicFont;
        try {
            cyrillicFont = PDType0Font.load(document, new File("fonts/Roboto-Bold.ttf"));
            System.out.println("✅ Загружен шрифт: Roboto-Bold.ttf");
        } catch (Exception e) {
            System.out.println("❌ Ошибка загрузки шрифта: " + e.getMessage());
            // Fallback на стандартный шрифт
            cyrillicFont = PDType0Font.load(document, new File("/usr/share/fonts/msttcore/arialbd.ttf"));
        }
        
        // Добавляем текст поверх шаблона
        try (PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            // Теперь используем кириллицу напрямую!
            contentStream.setFont(cyrillicFont, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(posX, posY);
            contentStream.showText(fullName); // Прямой текст без конвертации!
            contentStream.endText();
        }

        document.save(outputPath);
        System.out.println("✅ Диплом из шаблона сохранен: " + outputPath);
    }

    return outputPath;
}

// Также ЗАКОММЕНТИРОВАТЬ или УДАЛИТЬ вызов convertToLatin в generateSimpleDiploma:
// В методе generateSimpleDiploma найти и закомментировать:
// String latinName = convertToLatin(fullName); // ЗАКОММЕНТИРОВАТЬ
// И использовать прямо fullName:
contentStream.showText(fullName); // ИСПОЛЬЗОВАТЬ прямое значение
