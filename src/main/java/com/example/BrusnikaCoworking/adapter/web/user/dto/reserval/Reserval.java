package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;

import com.example.BrusnikaCoworking.domain.reserval.State;

public record Reserval(Long id,
                       String dateReserval,
                       String timeStartReserval,
                       String timeEndReserval,
                       String sendTime,
                       Integer table,
                       State stateReserval,
                       State stateGroup,
                       String invit) {
}