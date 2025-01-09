package com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval;

import java.util.List;

public record ReservalAdminForm(String date,
                                String timeStart,
                                String timeEnd,
                                List<Integer> tables) {
}