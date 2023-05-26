package com.example.habitwarden_backend.repository;

import com.example.habitwarden_backend.domain.Habit;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository

public interface HabitRepository extends ReactiveMongoRepository<Habit, ObjectId> {
    Mono<Habit> findByName(String name);
}
