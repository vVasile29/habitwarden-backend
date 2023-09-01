package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.*;
import com.example.habitwarden_backend.service.DateDataService;
import com.example.habitwarden_backend.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dateData")
public class DateDataController {

    private final DateDataService dateDataService;
    private final HabitService habitService;

    @GetMapping("/getAllDateData")
    public Flux<DateData> getAllDateData() {
        return dateDataService.getAllDateData();
    }

    @GetMapping("/getAllUserHabitDataWithDate/{userName}")
    public Flux<UserHabitDataWithDate> getAllUserHabitDataWithDate(@PathVariable String userName) {
        return dateDataService.getAllUserHabitDataWithDate(userName);
    }

    @GetMapping("/getAllUserHabitDataWithDateCompleted/{userName}")
    public Flux<UserHabitDataWithDate> getAllUserHabitDataWithDateCompleted(@PathVariable String userName) {
        return dateDataService.getAllUserHabitDataWithDatePredicate(userName, true);
    }

    @GetMapping("/getAllUserHabitDataWithDateIncompleted/{userName}")
    public Flux<UserHabitDataWithDate> getAllUserHabitDataWithDateIncompleted(@PathVariable String userName) {
        return dateDataService.getAllUserHabitDataWithDatePredicate(userName, false);
    }

    @PostMapping("/getCurrentHabitDoneDataOfUser")
    public Mono<HabitDoneData> getCurrentHabitDoneDataOfUser(@RequestBody HabitDoneDataRequest habitDoneData) {
        return dateDataService.getCurrentHabitDoneDataOfUser(habitDoneData.getUserName(),
                habitDoneData.getHabitName(), LocalDate.now());
    }

    @PostMapping("/getLastHabitDoneDataOfUser")
    public Mono<HabitDoneData> getHabitDoneData(@RequestBody HabitDoneDataRequest habitDoneData) {
        return dateDataService.getLastHabitDoneDataOfUser(habitDoneData.getUserName(), habitDoneData.getHabitName());
    }

    @PostMapping("/saveDateData")
    public Mono<DateData> saveDateData(@RequestBody DateDataRequest dateData) {
        return dateDataService.getDateData(LocalDate.now()).flatMap(data -> {
            List<UserHabitData> userHabitDataList = data.getUserHabitData().stream().toList();
            if (userHabitDataList.stream().noneMatch(userHabitData -> userHabitData.getUserName().equals(dateData.getUserName()))) {
                return dateDataService.addUserHabitData(dateData);
            }

            List<HabitDoneData> habitDoneDataList = userHabitDataList.stream()
                    .filter(userHabitData -> userHabitData.getUserName().equals(dateData.getUserName()))
                    .flatMap(userHabitData -> userHabitData.getHabitDoneData().stream()).toList();
            if (habitDoneDataList.stream().noneMatch(habitDoneData -> habitDoneData.getHabitName().equals(dateData.getHabitName()))) {
                return dateDataService.addHabitDoneData(dateData);
            }

            return dateDataService.addHabitDoneDataInfo(dateData);
        }).switchIfEmpty(Mono.defer(() -> dateDataService.saveDateData(dateData)));
    }

    @PostMapping("/getStreak")
    public Mono<Integer> getStreak(@RequestBody HabitDoneDataRequest habitDoneData) {
        return calculateStreak(habitDoneData.getUserName(), habitDoneData.getHabitName(), LocalDate.now(), 0)
                .defaultIfEmpty(0);
    }

    private Mono<Integer> calculateStreak(String userName, String habitName, LocalDate date, int streak) {
        Mono<Habit> habitMono = habitService.getHabitByName(habitName);

        return dateDataService.getCurrentHabitDoneDataOfUser(userName, habitName, date.minusDays(streak + 1))
                .flatMap(data -> habitMono.flatMap(habit -> {
                    if (data.getHabitDoneDataInfo().size() == habit.getTimesPerDay()) {
                        return calculateStreak(userName, habitName, date, streak + 1);
                    } else {
                        return Mono.just(streak);
                    }
                }))
                .defaultIfEmpty(streak);
    }

//    @PostMapping("/calculatePointsToRemoveFromAbsence")
//    public Mono<Integer> removePointsFromAbsence(@RequestBody HabitDoneDataRequest habitDoneData) {
//        return dateDataService.calculatePointsToRemoveFromAbsence(habitDoneData);
//    }

}
