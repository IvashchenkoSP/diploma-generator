package com.diplomagenerator.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DiplomaService {

    private final String uploadDir = "uploads/";
    private final String outputDir = "output/";

    public DiplomaService() {
        createDirectories();
        checkFonts();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(outputDir));
            System.out.println("✅ Директории созданы");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директории", e);
        }
    }

    private void checkFonts() {
        System.out.println("🔍 Проверка шрифтов...");
        File fontFile = new File("fonts/NotoSans-Regular.ttf");
        if (fontFile.exists()) {
            System.out.println("✅ Кириллический шрифт найден: " + fontFile.getName() + " (" + fontFile.length() + " bytes)");
        } else {
            System.out.println("❌ Кириллический шрифт не найден, будет использоваться латиница");
        }
    }

    public String saveUploadedTemplate(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Только PDF файлы поддерживаются");
        }
        
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());
        System.out.println("✅ Шаблон сохранен: " + filePath);
        return filePath.toString();
    }

    // Генерация диплома на основе загруженного шаблона
    public String generateDiplomaFromTemplate(String templatePath, String fullName, 
                                            float posX, float posY, float fontSize) throws IOException {
        System.out.println("🎯 Генерация из шаблона: " + templatePath);
        System.out.println("📝 Текст для вставки: '" + fullName + "'");
        
        if (!Files.exists(Paths.get(templatePath))) {
            throw new IOException("Шаблон не найден: " + templatePath);
        }

        String outputFileName = UUID.randomUUID() + "_diploma.pdf";
        String outputPath = outputDir + outputFileName;

        // Загружаем шаблон PDF
        try (PDDocument document = PDDocument.load(new File(templatePath))) {
            PDPage page = document.getPage(0);
            
            // Добавляем текст поверх шаблона
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                
                // Пробуем использовать кириллический шрифт
                PDType0Font cyrillicFont = null;
                try {
                    cyrillicFont = PDType0Font.load(document, new File("fonts/NotoSans-Regular.ttf"));
                    contentStream.setFont(cyrillicFont, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(fullName); // Кириллица напрямую!
                    contentStream.endText();
                    System.out.println("✅ Кириллический текст добавлен: " + fullName);
                } catch (Exception e) {
                    // Fallback на латиницу
                    System.out.println("❌ Ошибка кириллицы: " + e.getMessage());
                    String latinName = convertToLatinSafe(fullName);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(latinName);
                    contentStream.endText();
                    System.out.println("🔄 Использована латиница: " + latinName);
                }
            }

            document.save(outputPath);
            System.out.println("✅ Диплом из шаблона сохранен: " + outputPath);
        }

        return outputPath;
    }

    // Генерация простого диплома (без шаблона)
    public String generateSimpleDiploma(String fullName, float posX, float posY, float fontSize) throws IOException {
        System.out.println("🎯 Генерация простого диплома");
        
        String outputFileName = UUID.randomUUID() + "_diploma.pdf";
        String outputPath = outputDir + outputFileName;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // Пробуем кириллический шрифт для всего диплома
                PDType0Font cyrillicFont = null;
                boolean cyrillicSuccess = false;
                
                try {
                    cyrillicFont = PDType0Font.load(document, new File("fonts/NotoSans-Regular.ttf"));
                    cyrillicSuccess = true;
                    System.out.println("✅ Используем кириллический шрифт для диплома");
                } catch (Exception e) {
                    System.out.println("❌ Не удалось загрузить кириллический шрифт: " + e.getMessage());
                }
                
                if (cyrillicSuccess && cyrillicFont != null) {
                    // Кириллическая версия диплома
                    contentStream.setFont(cyrillicFont, 24);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText("ДИПЛОМ");
                    contentStream.endText();

                    contentStream.setFont(cyrillicFont, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(fullName);
                    contentStream.endText();

                    contentStream.setFont(cyrillicFont, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 400);
                    contentStream.showText("Вручается за успешное прохождение");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 370);
                    contentStream.showText("учебного курса");
                    contentStream.endText();

                    contentStream.setFont(cyrillicFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 200);
                    contentStream.showText("Дата: " + java.time.LocalDate.now());
                    contentStream.endText();
                } else {
                    // Латиница как fallback
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText("DIPLOMA");
                    contentStream.endText();

                    String latinName = convertToLatinSafe(fullName);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(latinName);
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 400);
                    contentStream.showText("Awarded for successful completion");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 370);
                    contentStream.showText("of the training course");
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, 200);
                    contentStream.showText("Date: " + java.time.LocalDate.now());
                    contentStream.endText();
                }
            }

            document.save(outputPath);
            System.out.println("✅ Простой диплом сохранен: " + outputPath);
        }

        return outputPath;
    }

    // Основной метод генерации
    public String generateDiploma(String templatePath, String fullName, float posX, float posY, float fontSize) throws IOException {
        if (templatePath != null && !templatePath.isEmpty() && Files.exists(Paths.get(templatePath))) {
            return generateDiplomaFromTemplate(templatePath, fullName, posX, posY, fontSize);
        } else {
            return generateSimpleDiploma(fullName, posX, posY, fontSize);
        }
    }

    // Конвертация в латиницу (для fallback)
    private String convertToLatinSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Student";
        }
        
        return text.replace("Иванов", "Ivanov")
                  .replace("Петров", "Petrov")
                  .replace("Сидоров", "Sidorov")
                  .replace("Иван", "Ivan")
                  .replace("Петр", "Peter")
                  .replace("Алексей", "Alexey")
                  .replace("Сергей", "Sergey")
                  .replace("Анна", "Anna")
                  .replace("Мария", "Maria")
                  .replace("Елена", "Elena")
                  .replace("Ольга", "Olga")
                  .replace("Наталья", "Natalia")
                  .replace("Дмитрий", "Dmitry")
                  .replace("Александр", "Alexander")
                  .replace("Владимир", "Vladimir")
                  .replace("Михаил", "Mikhail")
                  .replace("Тест", "Test")
                  .replace("Студент", "Student");
    }

    // Остальные методы без изменений...
    public List<String> generateMultipleDiplomas(String templatePath, List<String> names, 
                                                float posX, float posY, float fontSize) throws IOException {
        System.out.println("🎯 Пакетная генерация " + names.size() + " дипломов");
        
        List<String> resultPaths = new ArrayList<>();
        
        for (String name : names) {
            if (!name.trim().isEmpty()) {
                String diplomaPath = generateDiploma(templatePath, name.trim(), posX, posY, fontSize);
                resultPaths.add(diplomaPath);
                System.out.println("✅ Сгенерирован диплом для: " + name);
            }
        }
        
        System.out.println("✅ Всего сгенерировано: " + resultPaths.size() + " дипломов");
        return resultPaths;
    }

    public String createDiplomasZip(String templatePath, List<String> names, 
                                   float posX, float posY, float fontSize) throws IOException {
        System.out.println("🗜️ Создание ZIP архива для " + names.size() + " дипломов");
        
        String zipFileName = UUID.randomUUID() + "_diplomas.zip";
        String zipPath = outputDir + zipFileName;
        
        List<String> diplomaPaths = generateMultipleDiplomas(templatePath, names, posX, posY, fontSize);
        
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (int i = 0; i < diplomaPaths.size(); i++) {
                String diplomaPath = diplomaPaths.get(i);
                String name = names.get(i).trim();
                File diplomaFile = new File(diplomaPath);
                
                if (diplomaFile.exists()) {
                    String safeName = name.replaceAll("[^a-zA-Z0-9а-яА-Я]", "_");
                    ZipEntry zipEntry = new ZipEntry("diploma_" + safeName + ".pdf");
                    zos.putNextEntry(zipEntry);
                    Files.copy(diplomaFile.toPath(), zos);
                    zos.closeEntry();
                    diplomaFile.delete();
                    System.out.println("✅ Добавлен в ZIP: " + name);
                }
            }
        }
        
        System.out.println("✅ ZIP архив создан: " + zipPath + " (" + new File(zipPath).length() + " bytes)");
        return zipPath;
    }

    public File getDiplomaFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Файл не найден: " + filePath);
        }
        System.out.println("📤 Отправка файла: " + filePath + " (" + file.length() + " bytes)");
        return file;
    }

    public boolean testPdfGeneration() {
        try {
            String testPath = generateSimpleDiploma("Тест Студент", 100, 500, 16);
            File testFile = new File(testPath);
            boolean exists = testFile.exists();
            if (exists) {
                testFile.delete();
            }
            System.out.println("🧪 Тест PDF: " + (exists ? "УСПЕХ" : "ОШИБКА"));
            return exists;
        } catch (Exception e) {
            System.out.println("❌ Ошибка теста PDF: " + e.getMessage());
            return false;
        }
    }

    public List<String> getUploadedTemplates() {
        List<String> templates = new ArrayList<>();
        try {
            Files.list(Paths.get(uploadDir))
                 .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                 .forEach(path -> templates.add(path.toString()));
            System.out.println("📁 Найдено шаблонов: " + templates.size());
        } catch (IOException e) {
            System.out.println("Ошибка при получении списка шаблонов: " + e.getMessage());
        }
        return templates;
    }

    public String getTemplateName(String templatePath) {
        if (templatePath == null || templatePath.isEmpty()) {
            return "";
        }
        return Paths.get(templatePath).getFileName().toString();
    }
}
