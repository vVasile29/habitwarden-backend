package com.example.habitwarden_backend.repository;

import com.example.habitwarden_backend.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, ObjectId> {

    Mono<User> findByName(String name);

    Mono<Boolean> existsByName(String name);

}
