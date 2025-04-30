package com.example.BrusnikaCoworking.config.jwt.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KeylockUserPrincipal implements Serializable {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    private String middleName;
    private List<UserRole> roles;
}