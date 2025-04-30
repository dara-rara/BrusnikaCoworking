package com.example.BrusnikaCoworking.config.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserRole implements Serializable {
    private String userType; // Student, Teacher etc.
    private String roleId;
    private String qualification;
    private String instituteTitle;
    private String groupTitle;
    private Integer course;
    private String compensation;
    private Boolean isForeign;
}
