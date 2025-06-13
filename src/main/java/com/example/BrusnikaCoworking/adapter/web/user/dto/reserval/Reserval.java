package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;

import com.example.BrusnikaCoworking.domain.reserval.TypeDesing;

public record Reserval(Long id,
                       String dateReserval,
                       String timeStartReserval,
                       String timeEndReserval,
                       String sendTime,
                       Integer table,
                       TypeDesing type,
                       String invit) {
}