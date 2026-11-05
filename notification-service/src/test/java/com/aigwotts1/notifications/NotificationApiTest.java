package com.aigwotts1.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationApiTest {
    @Autowired MockMvc mvc;

    @Test
    void recordsAndListsNotifications() throws Exception {
        mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":42,\"title\":\"Learn microservices\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(42))
                .andExpect(jsonPath("$.message").value("Task completed: Learn microservices"))
                .andExpect(jsonPath("$.receivedAt").isNotEmpty());
        mvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(42));
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        for (String body : new String[]{"{}", "{\"taskId\":0,\"title\":\"Task\"}",
                "{\"taskId\":1,\"title\":\"   \"}",
                "{\"taskId\":1,\"title\":\"" + "a".repeat(121) + "\"}"}) {
            mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
