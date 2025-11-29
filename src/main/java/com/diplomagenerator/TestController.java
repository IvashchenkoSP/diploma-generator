package com.diplomagenerator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "✅ Тестовый контроллер работает!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "🚀 Приложение запущено и работает";
    }
}
