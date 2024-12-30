package com.example.BrusnikaCoworking.adapter.web.user.dto.profile;

import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ReservalProfile;

import java.util.List;

public record Profile(String username,
                      String realname,
                      List<ReservalProfile> reservals) {
}
