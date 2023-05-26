package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.DateData;
import com.example.habitwarden_backend.service.DateDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dateData")
public class DateDataController {

    private final DateDataService dateDataService;

    @GetMapping("/getAllDateData")
    public Flux<DateData> getAllDateData() {
        return dateDataService.getAllDateData();
    }

}
