package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;

import com.example.BrusnikaCoworking.domain.reserval.State;

public record NotificationForm (Long id,
                                String dateReserval,
                                String timeStartReserval,
                                String timeEndReserval,
                                String timeSend,
                                State group,
                                String username){
}
