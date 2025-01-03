package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

public record ReservalActive(Long id,
                             String dateReserval,
                             String timeStartReserval,
                             String timeEndReserval,
                             Integer table) {
}
