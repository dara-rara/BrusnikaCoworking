package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;

import java.util.List;

public record FreeTables(String date,
                         String timeStart,
                         String timeEnd,
                         List<Integer> tables) {
}