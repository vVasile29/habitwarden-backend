package com.example.habitwarden_backend.repository;

import com.example.habitwarden_backend.domain.DateData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface DateDataRepository extends ReactiveMongoRepository<DateData, Object> {
    Mono<DateData> findByDate(LocalDate date);
}
