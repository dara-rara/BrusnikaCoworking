package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.State;

public record NotificationForm (Long id,
                                String dateReserval,
                                String timeStartReserval,
                                String timeEndReserval,
                                Integer table,
                                String timeSend,
                                Type type,
                                State state,
                                String invit){
}
