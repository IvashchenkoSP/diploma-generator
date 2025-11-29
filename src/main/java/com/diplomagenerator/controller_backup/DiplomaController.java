package com.diplomagenerator.controller;

import com.diplomagenerator.service.DiplomaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DiplomaController {

    private final DiplomaService diplomaService;

    public DiplomaController(DiplomaService diplomaService) {
        this.diplomaService = diplomaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        System.out.println("✅ Главная страница загружена");
        
        boolean pdfWorks = diplomaService.testPdfGeneration();
        model.addAttribute("pdfWorks", pdfWorks);
        model.addAttribute("message", "🎓 Генератор дипломов готов!");
        
        // Получаем шаблоны с красивыми именами
        List<String> templates = diplomaService.getUploadedTemplates();
        List<String> templateNames = templates.stream()
                .map(diplomaService::getTemplateName)
                .collect(Collectors.toList());
        
        model.addAttribute("templates", templates);
        model.addAttribute("templateNames", templateNames);
        model.addAttribute("hasTemplates", !templates.isEmpty());
        
        model.addAttribute("defaultX", 100);
        model.addAttribute("defaultY", 500);
        model.addAttribute("defaultFontSize", 16);
        
        return "index";
    }

    @PostMapping("/upload-template")
    public String uploadTemplate(@RequestParam("templateFile") MultipartFile file, Model model) {
        System.out.println("📤 Загрузка файла: " + file.getOriginalFilename());
        
        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "Файл не выбран");
                return "index";
            }

            String savedTemplatePath = diplomaService.saveUploadedTemplate(file);
            model.addAttribute("message", "✅ Шаблон загружен: " + file.getOriginalFilename());
            model.addAttribute("templatePath", savedTemplatePath);
            
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки: " + e.getMessage());
        }
        
        // Обновляем список шаблонов
        List<String> templates = diplomaService.getUploadedTemplates();
        List<String> templateNames = templates.stream()
                .map(diplomaService::getTemplateName)
                .collect(Collectors.toList());
        
        model.addAttribute("templates", templates);
        model.addAttribute("templateNames", templateNames);
        model.addAttribute("hasTemplates", !templates.isEmpty());
        model.addAttribute("defaultX", 100);
        model.addAttribute("defaultY", 500);
        model.addAttribute("defaultFontSize", 16);
        model.addAttribute("pdfWorks", diplomaService.testPdfGeneration());
        
        return "index";
    }

    @PostMapping("/generate-single")
    public ResponseEntity<Resource> generateSingleDiploma(
            @RequestParam(value = "templatePath", required = false) String templatePath,
            @RequestParam("fullName") String fullName,
            @RequestParam("posX") float posX,
            @RequestParam("posY") float posY,
            @RequestParam("fontSize") float fontSize) {
        
        System.out.println("🎯 Запрос на генерацию диплома для: " + fullName);
        System.out.println("📁 Используемый шаблон: " + (templatePath != null ? diplomaService.getTemplateName(templatePath) : "НЕТ (простой диплом)"));
        
        try {
            String diplomaPath = diplomaService.generateDiploma(templatePath, fullName, posX, posY, fontSize);
            File diplomaFile = diplomaService.getDiplomaFile(diplomaPath);
            Resource resource = new FileSystemResource(diplomaFile);
            
            String fileName = "diploma_" + fullName.replace(" ", "_") + ".pdf";
            
            System.out.println("✅ Диплом готов для скачивания: " + fileName);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            System.out.println("❌ Ошибка генерации диплома: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate-multiple")
    public ResponseEntity<Resource> generateMultipleDiplomas(
            @RequestParam(value = "templatePath", required = false) String templatePath,
            @RequestParam("names") String names,
            @RequestParam("posX") float posX,
            @RequestParam("posY") float posY,
            @RequestParam("fontSize") float fontSize) {
        
        System.out.println("🎯 Запрос на пакетную генерацию");
        System.out.println("�� Используемый шаблон: " + (templatePath != null ? diplomaService.getTemplateName(templatePath) : "НЕТ (простой диплом)"));
        
        try {
            List<String> nameList = Arrays.stream(names.split("\\r?\\n"))
                    .filter(name -> !name.trim().isEmpty())
                    .collect(Collectors.toList());
            
            if (nameList.isEmpty()) {
                throw new IllegalArgumentException("Список имен пуст");
            }
            
            System.out.println("📝 Будут сгенерированы дипломы для: " + nameList);
            
            String zipPath = diplomaService.createDiplomasZip(templatePath, nameList, posX, posY, fontSize);
            File zipFile = diplomaService.getDiplomaFile(zipPath);
            
            Resource resource = new FileSystemResource(zipFile);
            
            System.out.println("✅ ZIP архив готов для скачивания");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diplomas.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            System.out.println("❌ Ошибка пакетной генерации: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/test-pdf")
    public ResponseEntity<String> testPdf() {
        boolean works = diplomaService.testPdfGeneration();
        String result = "PDF Generation: " + (works ? "WORKS" : "FAILED");
        System.out.println("🧪 " + result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-download")
    public ResponseEntity<Resource> testDownload() {
        System.out.println("✅ Тестовый download endpoint вызван");
        
        try {
            // Генерируем тестовый диплом
            String testPath = diplomaService.generateSimpleDiploma("Test User", 100, 500, 16);
            File testFile = diplomaService.getDiplomaFile(testPath);
            Resource resource = new FileSystemResource(testFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_diploma.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            System.out.println("❌ Ошибка тестового download: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
