package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;

import java.util.List;

public record ReservalForm(String date,
                           String timeStart,
                           String timeEnd,
                           List<Integer> tables,
                           List<String> usernames) {
}