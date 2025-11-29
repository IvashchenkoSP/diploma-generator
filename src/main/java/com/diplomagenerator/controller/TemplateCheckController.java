package com.diplomagenerator.controller;

import com.diplomagenerator.service.DiplomaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TemplateCheckController {

    private final DiplomaService diplomaService;

    public TemplateCheckController(DiplomaService diplomaService) {
        this.diplomaService = diplomaService;
    }

    @GetMapping("/check-templates")
    public ResponseEntity<Map<String, Object>> checkTemplates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> templates = diplomaService.getUploadedTemplates();
            response.put("totalTemplates", templates.size());
            
            List<Map<String, Object>> templateDetails = templates.stream()
                    .map(template -> {
                        Map<String, Object> details = new HashMap<>();
                        details.put("path", template);
                        details.put("name", diplomaService.getTemplateName(template));
                        details.put("exists", Files.exists(Paths.get(template)));
                        details.put("isFile", Files.isRegularFile(Paths.get(template)));
                        if (Files.exists(Paths.get(template))) {
                            try {
                                details.put("size", Files.size(Paths.get(template)));
                            } catch (Exception e) {
                                details.put("size", "unknown");
                            }
                        }
                        return details;
                    })
                    .toList();
            
            response.put("templates", templateDetails);
            response.put("status", "success");
            
            System.out.println("🔍 Проверка шаблонов: найдено " + templates.size() + " файлов");
            templateDetails.forEach(detail -> {
                System.out.println("   - " + detail.get("name") + 
                                 " (exists: " + detail.get("exists") + 
                                 ", isFile: " + detail.get("isFile") + ")");
            });
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            System.out.println("❌ Ошибка проверки шаблонов: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
