package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Document("dateData")
public class DateData {

    ObjectId id;
    LocalDate date;
    List<UserHabitData> userHabitData;

    public static DateData ofRequest(DateDataRequest dateDataRequest) {
        List<HabitDoneData> habitDoneDataList = new ArrayList<>();
        habitDoneDataList.add(new HabitDoneData(dateDataRequest.getHabitName(), List.of(dateDataRequest.getLieOnDone())));

        List<UserHabitData> userHabitDataList = new ArrayList<>();
        userHabitDataList.add(new UserHabitData(dateDataRequest.getUserName(), habitDoneDataList));

        return new DateData(
                null,
                LocalDate.parse(dateDataRequest.getDate()),
                userHabitDataList
        );
    }

}
