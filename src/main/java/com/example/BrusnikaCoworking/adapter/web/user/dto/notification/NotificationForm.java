package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

import com.example.BrusnikaCoworking.domain.notification.Type;

public record NotificationForm (Long id,
                                String dateReserval,
                                String timeStartReserval,
                                String timeEndReserval,
                                Integer table,
                                String timeSend,
                                Type type,
                                Boolean state,
                                String invit){
}
