package com.example.habitwarden_backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class HomeController {

    @GetMapping("/")
    public Mono<String> home() {
        return Mono.just("HOME");
    }
}
