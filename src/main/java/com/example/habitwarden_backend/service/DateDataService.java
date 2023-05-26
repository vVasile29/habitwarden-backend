package com.example.habitwarden_backend.service;

import com.example.habitwarden_backend.domain.DateData;
import com.example.habitwarden_backend.repository.DateDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class DateDataService {

    private final DateDataRepository dateDataRepository;

    public Flux<DateData> getAllDateData() {
        return dateDataRepository.findAll();
    }

}
