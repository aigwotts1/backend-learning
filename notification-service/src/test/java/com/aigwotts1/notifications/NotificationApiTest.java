package com.aigwotts1.notifications;

import com.aigwotts1.notifications.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false", "spring.kafka.admin.auto-create=false"})
@AutoConfigureMockMvc
class NotificationApiTest {
    @Autowired MockMvc mvc;
    @Autowired NotificationService service;

    @Test
    void listsRecordedNotificationsAndDisallowsHttpCreation() throws Exception {
        service.record(UUID.randomUUID(), 42, "Learn Kafka");
        mvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(42))
                .andExpect(jsonPath("$[0].message").value("Task completed: Learn Kafka"));
        mvc.perform(post("/api/notifications")).andExpect(status().isMethodNotAllowed());
    }
}
