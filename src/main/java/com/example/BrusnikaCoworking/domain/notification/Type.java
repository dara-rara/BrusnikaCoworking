package com.example.BrusnikaCoworking.domain.notification;

public enum Type {
    GROUP,//для подтверждения групп. брони
    MEMENTO,//для напоминания
    CODE,//для подтверждения присутствия кодом
    CANCEL,//для отмены брони
    CREATE,//для создания брони
    FINE,//пропуск брони - штраф
}
