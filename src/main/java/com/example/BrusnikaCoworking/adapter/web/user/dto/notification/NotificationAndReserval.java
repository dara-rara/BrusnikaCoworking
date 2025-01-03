package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

import java.util.List;

public record NotificationAndReserval (List<ReservalActive> reservals,
                                       List<NotificationForm> notifications){
}
