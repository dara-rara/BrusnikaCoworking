package com.example.BrusnikaCoworking.adapter.web.user.dto.reserval;


import java.util.List;

public record ReservalDateCategories(List<Reserval> todayReserval,
                                     List<Reserval> last7DaysReserval,
                                     List<Reserval> lastMonthReserval,
                                     List<Reserval> oldReserval) {
}
