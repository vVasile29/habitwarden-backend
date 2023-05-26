package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.Habit;
import com.example.habitwarden_backend.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits")
public class HabitController {

    private final HabitService habitService;

    @GetMapping("/getAllHabits")
    public Flux<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/getHabit/{name}")
    public Mono<Habit> getHabitByName(@PathVariable String name) {
        return habitService.getHabitByName(name);
    }

}

