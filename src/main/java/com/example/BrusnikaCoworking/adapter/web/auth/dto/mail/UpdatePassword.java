package com.example.BrusnikaCoworking.adapter.web.auth.dto.mail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class UpdatePassword {
    private String username;
    private LocalDateTime time = LocalDateTime.now();


    public UpdatePassword(String username) {
        this.username = username;
    }
}