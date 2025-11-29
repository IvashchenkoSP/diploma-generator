package com.diplomagenerator.controller;

import com.diplomagenerator.service.DiplomaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class PreviewController {

    private final DiplomaService diplomaService;

    public PreviewController(DiplomaService diplomaService) {
        this.diplomaService = diplomaService;
    }

    @GetMapping("/preview")
    public String previewPage(Model model) {
        System.out.println("✅ Страница предпросмотра загружена");
        
        model.addAttribute("defaultX", 100);
        model.addAttribute("defaultY", 500);
        model.addAttribute("defaultFontSize", 16);
        model.addAttribute("defaultText", "Иван Иванов"); // Кириллица по умолчанию!
        
        List<String> templates = diplomaService.getUploadedTemplates();
        List<String> templateNames = templates.stream()
                .map(diplomaService::getTemplateName)
                .toList();
        
        System.out.println("📁 Доступно шаблонов: " + templates.size());
        
        model.addAttribute("templates", templates);
        model.addAttribute("templateNames", templateNames);
        model.addAttribute("hasTemplates", !templates.isEmpty());
        
        return "preview";
    }

    @GetMapping("/generate-preview")
    public ResponseEntity<InputStreamResource> generatePreview(
            @RequestParam("text") String text,
            @RequestParam("posX") float posX,
            @RequestParam("posY") float posY,
            @RequestParam("fontSize") float fontSize,
            @RequestParam(value = "templatePath", required = false) String templatePath) {
        
        System.out.println("🎯 Запрос предпросмотра: text='" + text + "', X=" + posX + ", Y=" + posY + ", fontSize=" + fontSize);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDDocument document;
            boolean usingTemplate = false;
            
            if (templatePath != null && !templatePath.trim().isEmpty()) {
                Path templateFile = Paths.get(templatePath);
                if (Files.exists(templateFile) && Files.isRegularFile(templateFile)) {
                    try {
                        document = PDDocument.load(templateFile.toFile());
                        usingTemplate = true;
                        System.out.println("✅ Шаблон загружен: " + templatePath);
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка загрузки шаблона: " + e.getMessage());
                        document = createDocumentWithGrid();
                    }
                } else {
                    System.out.println("❌ Файл шаблона не найден: " + templatePath);
                    document = createDocumentWithGrid();
                }
            } else {
                document = createDocumentWithGrid();
            }
            
            PDPage page = document.getPage(0);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                
                if (!usingTemplate) {
                    drawTargetArea(contentStream, posX, posY, 200, 30);
                }
                
                // Пробуем кириллический шрифт
                try {
                    PDType0Font cyrillicFont = PDType0Font.load(document, new File("fonts/NotoSans-Regular.ttf"));
                    contentStream.setFont(cyrillicFont, fontSize);
                    contentStream.setNonStrokingColor(0, 0, 0);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(text); // Кириллица напрямую!
                    contentStream.endText();
                    System.out.println("✅ Кириллический текст в предпросмотре: " + text);
                } catch (Exception e) {
                    // Fallback на стандартный шрифт
                    System.out.println("🔄 Fallback на стандартный шрифт: " + e.getMessage());
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    contentStream.setNonStrokingColor(0, 0, 0);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(posX, posY);
                    contentStream.showText(text);
                    contentStream.endText();
                }
                
                // Координаты
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.setNonStrokingColor(255, 0, 0);
                contentStream.beginText();
                contentStream.newLineAtOffset(posX, posY - 20);
                contentStream.showText("X: " + posX + ", Y: " + posY);
                contentStream.endText();
                
                // Информация о режиме
                contentStream.setNonStrokingColor(0, 100, 0);
                contentStream.beginText();
                contentStream.newLineAtOffset(10, 20);
                if (usingTemplate) {
                    String templateName = diplomaService.getTemplateName(templatePath);
                    contentStream.showText("Шаблон: " + templateName);
                } else {
                    contentStream.showText("Режим: Сетка");
                }
                contentStream.endText();
                
            } catch (Exception e) {
                System.out.println("❌ Ошибка при добавлении текста: " + e.getMessage());
            }

            document.save(baos);
            document.close();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            
            System.out.println("✅ Предпросмотр успешно сгенерирован");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
                    
        } catch (Exception e) {
            System.out.println("❌ Критическая ошибка при генерации предпросмотра: " + e.getMessage());
            return createErrorResponse("Ошибка генерации предпросмотра: " + e.getMessage());
        }
    }

    // Вспомогательные методы без изменений...
    private PDDocument createDocumentWithGrid() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        drawGrid(document, page);
        return document;
    }

    private void drawGrid(PDDocument document, PDPage page) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            
            contentStream.setNonStrokingColor(255, 255, 255);
            contentStream.addRect(0, 0, width, height);
            contentStream.fill();
            
            contentStream.setLineWidth(0.5f);
            contentStream.setStrokingColor(200, 200, 200);
            
            for (float x = 50; x < width; x += 50) {
                contentStream.moveTo(x, 0);
                contentStream.lineTo(x, height);
                contentStream.stroke();
            }
            
            for (float y = 50; y < height; y += 50) {
                contentStream.moveTo(0, y);
                contentStream.lineTo(width, y);
                contentStream.stroke();
            }
            
            contentStream.setStrokingColor(255, 0, 0);
            contentStream.setLineWidth(1f);
            
            contentStream.moveTo(width / 2, 0);
            contentStream.lineTo(width / 2, height);
            contentStream.stroke();
            
            contentStream.moveTo(0, height / 2);
            contentStream.lineTo(width, height / 2);
            contentStream.stroke();
            
            contentStream.setNonStrokingColor(0, 0, 0);
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.beginText();
            contentStream.newLineAtOffset(5, height - 10);
            contentStream.showText("A4: 595 x 842 points");
            contentStream.endText();
        }
    }

    private void drawTargetArea(PDPageContentStream contentStream, float x, float y, float width, float height) throws IOException {
        contentStream.setLineWidth(2f);
        contentStream.setStrokingColor(0, 100, 0);
        contentStream.setLineDashPattern(new float[]{5, 5}, 0);
        
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.lineTo(x + width, y - height);
        contentStream.lineTo(x, y - height);
        contentStream.closePath();
        contentStream.stroke();
        
        contentStream.setLineDashPattern(new float[]{}, 0);
    }

    private ResponseEntity<InputStreamResource> createErrorResponse(String errorMessage) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.setNonStrokingColor(255, 0, 0);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 400);
                contentStream.showText("Ошибка предпросмотра");
                contentStream.endText();
                
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setNonStrokingColor(0, 0, 0);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 370);
                contentStream.showText(errorMessage);
                contentStream.endText();
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"error.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
