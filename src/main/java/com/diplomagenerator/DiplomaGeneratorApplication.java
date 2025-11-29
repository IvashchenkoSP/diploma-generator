package com.diplomagenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiplomaGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiplomaGeneratorApplication.class, args);
        System.out.println("✅ Приложение запущено! http://localhost:8080");
    }
}
