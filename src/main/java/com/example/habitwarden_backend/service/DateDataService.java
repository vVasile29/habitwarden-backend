package com.example.habitwarden_backend.service;

import com.example.habitwarden_backend.domain.DateData;
import com.example.habitwarden_backend.domain.DateDataRequest;
import com.example.habitwarden_backend.domain.HabitDoneData;
import com.example.habitwarden_backend.domain.UserHabitData;
import com.example.habitwarden_backend.repository.DateDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateDataService {

    private final DateDataRepository dateDataRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<DateData> getAllDateData() {
        return dateDataRepository.findAll();
    }

    public Mono<DateData> getDateData(LocalDate date) {
        return dateDataRepository.findByDate(date);
    }

    public Mono<HabitDoneData> getHabitDoneData(String userName, String habitName, LocalDate date) {
        return getDateData(date)
                .flatMap(dateDataDocument -> Flux.fromIterable(dateDataDocument.getUserHabitData())
                        .filter(userHabitData -> userHabitData.getUserName().equals(userName))
                        .next()
                )
                .flatMap(userHabitData -> Flux.fromIterable(userHabitData.getHabitDoneData())
                        .filter(habitDoneData -> habitDoneData.getHabitName().equals(habitName))
                        .next()
                );
    }

    public Mono<DateData> saveDateData(DateDataRequest dateData) {
        return dateDataRepository.save(DateData.ofRequest(dateData));
    }

    public Mono<DateData> addUserHabitData(DateDataRequest dateData) {
        HabitDoneData newHabitDoneData = new HabitDoneData(dateData.getHabitName(), List.of(dateData.getLieOnDone()));
        UserHabitData newUserHabitData = new UserHabitData(dateData.getUserName(), List.of(newHabitDoneData));

        Query query = Query.query(Criteria.where("date").is(LocalDate.parse(dateData.getDate())));
        return reactiveMongoTemplate.findOne(query, DateData.class)
                .flatMap(myDateData -> {
                    myDateData.getUserHabitData().add(newUserHabitData);
                    return reactiveMongoTemplate.save(myDateData);
                });
    }

    // TODO figure out if this can be made better/shorter
    public Mono<DateData> addHabitDoneData(DateDataRequest dateData) {
        String userName = dateData.getUserName();
        String habitName = dateData.getHabitName();
        Boolean lieOnDone = dateData.getLieOnDone();

        // Create a new HabitDoneData object with the provided habitId and lieOnDone
        HabitDoneData newHabitDoneData = new HabitDoneData(habitName, List.of(lieOnDone));

        Query query = Query.query(Criteria.where("date").is(LocalDate.parse(dateData.getDate())));
        return reactiveMongoTemplate.findOne(query, DateData.class)
                .flatMap(dateDataDocument -> {
                    // Update the existing UserHabitData with the new HabitDoneData
                    List<UserHabitData> updatedUserHabitDataList = dateDataDocument.getUserHabitData().stream()
                            .map(userHabitData -> {
                                if (userHabitData.getUserName().equals(userName)) {
                                    List<HabitDoneData> updatedHabitDoneDataList = new ArrayList<>(userHabitData.getHabitDoneData());
                                    updatedHabitDoneDataList.add(newHabitDoneData);
                                    return new UserHabitData(userName, updatedHabitDoneDataList);
                                }
                                return userHabitData;
                            })
                            .collect(Collectors.toList());

                    // Update the DateData document with the modified UserHabitData list
                    Update update = Update.update("userHabitData", updatedUserHabitDataList);
                    return reactiveMongoTemplate.updateFirst(query, update, DateData.class)
                            .then(Mono.just(dateDataDocument));
                });
    }

    public Mono<DateData> addLieOnDone(DateDataRequest dateData) {
        String userName = dateData.getUserName();
        String habitName = dateData.getHabitName();
        Boolean lieOnDone = dateData.getLieOnDone();

        // Find the DateData document with the given userName and habitName
        Query query = Query.query(Criteria.where("userHabitData.userName").is(userName)
                .and("userHabitData.habitDoneData.habitName").is(habitName));
        return reactiveMongoTemplate.findOne(query, DateData.class)
                .flatMap(dateDataDocument -> {
                    // Update the existing HabitDoneData with the new lieOnDone value
                    List<UserHabitData> updatedUserHabitDataList = dateDataDocument.getUserHabitData().stream()
                            .map(userHabitData -> {
                                if (userHabitData.getUserName().equals(userName)) {
                                    List<HabitDoneData> updatedHabitDoneDataList = userHabitData.getHabitDoneData().stream()
                                            .map(habitDoneData -> {
                                                if (habitDoneData.getHabitName().equals(habitName)) {
                                                    List<Boolean> updatedLieOnDoneList = new ArrayList<>(habitDoneData.getLieOnDone());
                                                    updatedLieOnDoneList.add(lieOnDone);
                                                    return new HabitDoneData(habitName, updatedLieOnDoneList);
                                                }
                                                return habitDoneData;
                                            })
                                            .collect(Collectors.toList());

                                    return new UserHabitData(userName, updatedHabitDoneDataList);
                                }
                                return userHabitData;
                            })
                            .collect(Collectors.toList());

                    // Update the DateData document with the modified UserHabitData list
                    Update update = Update.update("userHabitData", updatedUserHabitDataList);
                    return reactiveMongoTemplate.updateFirst(query, update, DateData.class)
                            .then(Mono.just(dateDataDocument));
                });
    }

}
