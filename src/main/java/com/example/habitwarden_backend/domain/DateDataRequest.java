package com.example.habitwarden_backend.domain;

import lombok.Data;

@Data
public class DateDataRequest {

    String userName;
    String habitName;
    String date;
    Boolean done;
    Boolean lieOnDone;

}
