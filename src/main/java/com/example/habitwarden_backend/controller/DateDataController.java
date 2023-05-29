package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.DateData;
import com.example.habitwarden_backend.domain.DateDataRequest;
import com.example.habitwarden_backend.domain.HabitDoneData;
import com.example.habitwarden_backend.domain.UserHabitData;
import com.example.habitwarden_backend.service.DateDataService;
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

    @GetMapping("/getAllDateData")
    public Flux<DateData> getAllDateData() {
        return dateDataService.getAllDateData();
    }

    @GetMapping("/getDateData")
    public Mono<DateData> getDateData(@RequestBody DateDataRequest dateData) {
        return dateDataService.getDateData(LocalDate.parse(dateData.getDate()));
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
                    .flatMap(userHabitData -> userHabitData.getHabitDoneData().stream()).toList();
            if (habitDoneDataList.stream().noneMatch(habitDoneData -> habitDoneData.getHabitName().equals(dateData.getHabitName()))) {
                System.out.println("habit not yet found on this dateData, has been added with data");
                return dateDataService.addHabitDoneData(dateData);
            }

            return dateDataService.addLieOnDone(dateData);
        }).switchIfEmpty(Mono.defer(() -> dateDataService.saveDateData(dateData)));
    }
}
