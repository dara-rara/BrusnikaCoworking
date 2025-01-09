package com.example.BrusnikaCoworking.adapter.web.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginUser{
    private String username;
    private String password;
}
