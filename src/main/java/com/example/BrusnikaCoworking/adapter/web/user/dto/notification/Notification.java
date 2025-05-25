package com.example.BrusnikaCoworking.adapter.web.user.dto.notification;

import java.util.List;

public record Notification(List<NotificationForm> notificationFormsFalse,
                           List<NotificationForm> notificationFormsTrue) {
}
//непрочитанные и прочитанные уведомления