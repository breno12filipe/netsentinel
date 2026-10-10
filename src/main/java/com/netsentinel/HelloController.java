package com.netsentinel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "<h1>NetSentinel está online!</h1><p>Seu servidor Spring Boot respondeu com sucesso.</p>";
    }
}