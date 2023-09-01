package com.example.habitwarden_backend.service;

import com.example.habitwarden_backend.domain.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateDataService {

    private final DateDataRepository dateDataRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final HabitService habitService;

    public Flux<DateData> getAllDateData() {
        return dateDataRepository.findAll();
    }

    public Mono<DateData> getDateData(LocalDate date) {
        return dateDataRepository.findByDate(date);
    }

    public Flux<UserHabitDataWithDate> getAllUserHabitDataWithDate(String userName) {
        return getAllDateData()
                .flatMap(dateData -> Flux.fromIterable(dateData.getUserHabitData())
                        .filter(userHabitData -> userHabitData.getUserName().equals(userName))
                        .map(habitDoneData -> new UserHabitDataWithDate(dateData.getDate(), habitDoneData))
                );
    }

    public Flux<UserHabitDataWithDate> getAllUserHabitDataWithDatePredicate(String userName, Boolean lookForCompleted) {
        Mono<Integer> tasksToDoPerDay = habitService.getHabitTasksToDoPerDay();
        return tasksToDoPerDay.flatMapMany(toDo ->
                getAllDateData()
                        .flatMap(dateData -> Flux.fromIterable(dateData.getUserHabitData())
                                .filter(userHabitData -> userHabitData.getUserName().equals(userName))
                                .filter(userHabitData -> {
                                    int habitDoneDataInfoSizeSum = userHabitData.getHabitDoneData()
                                            .stream()
                                            .map(habitDoneData -> habitDoneData.getHabitDoneDataInfo().size())
                                            .reduce(0, Integer::sum);
                                    return (habitDoneDataInfoSizeSum == toDo) == lookForCompleted;
                                })
                                .map(habitDoneData -> new UserHabitDataWithDate(dateData.getDate(), habitDoneData)))
        );
    }

    public Mono<HabitDoneData> getCurrentHabitDoneDataOfUser(String userName, String habitName, LocalDate date) {
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

    public Mono<HabitDoneData> getLastHabitDoneDataOfUser(String userName, String habitName) {
        return getDateData(LocalDate.now()) // Assuming you want to get the latest data based on the current date
                .flatMap(dateDataDocument -> Flux.fromIterable(dateDataDocument.getUserHabitData())
                        .filter(userHabitData -> userHabitData.getUserName().equals(userName))
                        .next()
                )
                .flatMap(userHabitData -> Flux.fromIterable(userHabitData.getHabitDoneData())
                        .filter(habitDoneData -> habitDoneData.getHabitName().equals(habitName))
                        .collectList()
                        .mapNotNull(habitDoneDataList -> {
                            if (habitDoneDataList.isEmpty()) {
                                return null; // Return null if no matching habit data is found
                            }
                            return habitDoneDataList.get(habitDoneDataList.size() - 1); // Get the last element in the list
                        })
                );
    }

    public Mono<DateData> saveDateData(DateDataRequest dateData) {
        return dateDataRepository.save(DateData.ofRequest(dateData));
    }

    public Mono<DateData> addUserHabitData(DateDataRequest dateData) {
        HabitDoneData newHabitDoneData = new HabitDoneData(dateData.getHabitName(), List.of(new HabitDoneDataInfo(LocalDateTime.now(), dateData.getDone(), dateData.getLieOnDone(), dateData.getWantedToQuit())));
        UserHabitData newUserHabitData = new UserHabitData(dateData.getUserName(), List.of(newHabitDoneData));

        Query query = Query.query(Criteria.where("date").is(LocalDate.now()));
        return reactiveMongoTemplate.findOne(query, DateData.class)
                .flatMap(myDateData -> {
                    myDateData.getUserHabitData().add(newUserHabitData);
                    return reactiveMongoTemplate.save(myDateData);
                });
    }

    public Mono<DateData> addHabitDoneData(DateDataRequest dateData) {
        String userName = dateData.getUserName();
        String habitName = dateData.getHabitName();
        Boolean done = dateData.getDone();
        Boolean lieOnDone = dateData.getLieOnDone();
        Boolean wantedToQuit = dateData.getWantedToQuit();

        // Create a new HabitDoneData object with the provided habitId and lieOnDone
        HabitDoneData newHabitDoneData = new HabitDoneData(habitName, List.of(new HabitDoneDataInfo(LocalDateTime.now(), done, lieOnDone, wantedToQuit)));

        Query query = Query.query(Criteria.where("date").is(LocalDate.now()));
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

    public Mono<DateData> addHabitDoneDataInfo(DateDataRequest dateData) {
        String userName = dateData.getUserName();
        String habitName = dateData.getHabitName();
        Boolean done = dateData.getDone();
        Boolean lieOnDone = dateData.getLieOnDone();
        Boolean wantedToQuit = dateData.getWantedToQuit();

        // Find the DateData document with the given userName and habitName
        Query query = Query.query(Criteria.where("date").is(LocalDate.now()));
        return reactiveMongoTemplate.findOne(query, DateData.class)
                .flatMap(dateDataDocument -> {
                    // Update the existing HabitDoneDataInfo with the new lieOnDone value
                    List<UserHabitData> updatedUserHabitDataList = dateDataDocument.getUserHabitData().stream()
                            .map(userHabitData -> {
                                if (userHabitData.getUserName().equals(userName)) {
                                    List<HabitDoneData> updatedHabitDoneDataList = userHabitData.getHabitDoneData().stream()
                                            .map(habitDoneData -> {
                                                if (habitDoneData.getHabitName().equals(habitName)) {
                                                    List<HabitDoneDataInfo> habitDoneDataInfoList = habitDoneData.getHabitDoneDataInfo();
                                                    habitDoneDataInfoList.add(new HabitDoneDataInfo(LocalDateTime.now(), done, lieOnDone, wantedToQuit));
                                                    return new HabitDoneData(habitName, habitDoneDataInfoList);
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

//    problem, what if user opens twice
//    public Mono<Integer> calculatePointsToRemoveFromAbsence(HabitDoneDataRequest habitDoneData) {
//        String userName = habitDoneData.getUserName();
//        String habitName = habitDoneData.getHabitName();
//
//        Mono<Habit> habitMono = habitService.getHabitByName(habitName);
//        Mono<HabitDoneData> lastHabitDoneDataMono = getLastHabitDoneDataOfUser(userName, habitName);
//
//        return Mono.zip(habitMono, lastHabitDoneDataMono)
//                .flatMap(tuple -> {
//                    Habit habit = tuple.getT1();
//                    HabitDoneData lastHabitDoneData = tuple.getT2();
//
//                    int pointsToLoseFromAbsence = 0;
//
//                    LocalDate lastHabitDoneDate = lastHabitDoneData.getHabitDoneDataInfo().get(0).getDoneTime().toLocalDate();
//                    LocalDate currentDate = LocalDate.now();
//
//                    if (lastHabitDoneDate.isEqual(currentDate)) {
//                        return Mono.just(0);
//                    }
//
//                    int amountMissedOnLastHabitDoneDate = habit.getTimesPerDay() + lastHabitDoneData.getHabitDoneDataInfo().size();
//                    pointsToLoseFromAbsence += habit.getPointsPerTask() * amountMissedOnLastHabitDoneDate;
//
//                    long amountOfDays = ChronoUnit.DAYS.between(lastHabitDoneDate.plusDays(1), currentDate.minusDays(1));
//                    pointsToLoseFromAbsence += habit.getPointsPerDay() * amountOfDays;
//
//                    return Mono.just(pointsToLoseFromAbsence);
//                });
//    }

}
