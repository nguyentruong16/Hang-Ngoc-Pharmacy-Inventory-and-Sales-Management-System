package com.example.project.dto.response;

import com.example.project.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Integer id;
    private Integer accountId;
    private String message;
    private Instant createdAt;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAccountID() != null ? notification.getAccountID().getId() : null,
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}