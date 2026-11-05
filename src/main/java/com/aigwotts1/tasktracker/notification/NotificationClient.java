package com.aigwotts1.tasktracker.notification;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {
    private final RestClient client;

    public NotificationClient(RestClient notificationRestClient) {
        this.client = notificationRestClient;
    }

    public void send(TaskCompletedEvent event) {
        client.post().uri("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve().toBodilessEntity();
    }
}
