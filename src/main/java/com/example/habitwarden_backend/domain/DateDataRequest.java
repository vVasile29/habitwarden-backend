package com.example.habitwarden_backend.domain;

import lombok.Data;

@Data
public class DateDataRequest {

    String userName;
    Boolean done;
    Boolean lieOnDone;
    Boolean wantedToQuit;
    String habitName;

}
