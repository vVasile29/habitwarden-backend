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

    @PostMapping("/getHabitDoneData")
    public Mono<HabitDoneData> getHabitDoneData(@RequestBody HabitDoneDataRequest habitDoneData) {
        return dateDataService.getHabitDoneData(habitDoneData.getUserName(),
                habitDoneData.getHabitName(), LocalDate.parse(habitDoneData.getDate()));
    }

    @PostMapping("/saveDateData")
    public Mono<DateData> saveDateData(@RequestBody DateDataRequest dateData) {
        // TODO parsing doesnt work after new day in german time and UTC still in other day
        return dateDataService.getDateData(LocalDate.parse(dateData.getDate())).flatMap(data -> {
            List<UserHabitData> userHabitDataList = data.getUserHabitData().stream().toList();
            if (userHabitDataList.stream().noneMatch(userHabitData -> userHabitData.getUserName().equals(dateData.getUserName()))) {
                System.out.println("no dateData for this user found, adding user and data");
                return dateDataService.addUserHabitData(dateData);
            }

            List<HabitDoneData> habitDoneDataList = userHabitDataList.stream()
                    .filter(userHabitData -> userHabitData.getUserName().equals(dateData.getUserName()))
                    .flatMap(userHabitData -> userHabitData.getHabitDoneData().stream()).toList();
            if (habitDoneDataList.stream().noneMatch(habitDoneData -> habitDoneData.getHabitName().equals(dateData.getHabitName()))) {
                System.out.println("habit not yet found on this dateData, has been added with data");
                return dateDataService.addHabitDoneData(dateData);
            }

            return dateDataService.addLieOnDone(dateData);
        }).switchIfEmpty(Mono.defer(() -> dateDataService.saveDateData(dateData)));
    }

    @PostMapping("/getStreak")
    public Mono<Integer> getStreak(@RequestBody HabitDoneDataRequest habitDoneData) {
        return calculateStreak(habitDoneData.getUserName(), habitDoneData.getHabitName(), LocalDate.parse(habitDoneData.getDate()), 0)
                .defaultIfEmpty(0);
    }

    private Mono<Integer> calculateStreak(String userName, String habitName, LocalDate date, int streak) {
        Mono<Habit> habitMono = habitService.getHabitByName(habitName);

        return dateDataService.getHabitDoneData(userName, habitName, date.minusDays(streak + 1))
                .flatMap(data -> habitMono.flatMap(habit -> {
                    if (data.getLieOnDone().size() == habit.getTimesPerDay()) {
                        return calculateStreak(userName, habitName, date, streak + 1);
                    } else {
                        return Mono.just(streak);
                    }
                }))
                .defaultIfEmpty(streak);
    }

}
