package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

import com.example.BrusnikaCoworking.domain.reserval.State;

public record NotificationForm (Long id,
                                String timeSend,
                                String title,
                                String text,
                                State state){
}
