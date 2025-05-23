package com.example.BrusnikaCoworking.adapter.web.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LogupUser {
    private String username;
    private String realname;
    private LocalDateTime time = LocalDateTime.now();


    public LogupUser(String username, String realname) {
        this.username = username;
        this.realname = realname;
    }
}
