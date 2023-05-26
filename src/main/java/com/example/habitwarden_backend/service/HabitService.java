package com.example.habitwarden_backend.service;

import com.example.habitwarden_backend.domain.Habit;
import com.example.habitwarden_backend.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;

    public Flux<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public Mono<Habit> getHabitByName(String name) {
        return habitRepository.findByName(name);
    }

}
