package com.aigwotts1.tasktracker;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

// MockMvc exercises real Spring controllers, services, and PostgreSQL without an HTTP server.
    @Test
    void createsAndListsTasks() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" Learn Spring Boot \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.completed").value(false));
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Learn Spring Boot"));
    }

    @Test
    void rejectsInvalidTitles() throws Exception {
        for (String body : new String[]{"{}", "{\"title\":\"   \"}",
                "{\"title\":\"" + "a".repeat(121) + "\"}"}) {
            mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void supportsTaskLifecycle() throws Exception {
        long id = createTask("Learn CRUD");
        mvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Learn CRUD"));

        mvc.perform(put("/api/tasks/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" Learn transactions \",\"completed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn transactions"))
                .andExpect(jsonPath("$.completed").value(false));

        // Completing twice leaves the task complete: the operation is idempotent.
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(patch("/api/tasks/{id}/complete", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.title").value("Learn transactions"));
        }

        mvc.perform(put("/api/tasks/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Reopen task\",\"completed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));
        mvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get("/api/tasks/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void returnsProblemDetailsForMissingTasks() throws Exception {
        String path = "/api/tasks/" + Long.MAX_VALUE;
        for (HttpMethod method : new HttpMethod[]{HttpMethod.GET, HttpMethod.PUT,
                HttpMethod.PATCH, HttpMethod.DELETE}) {
            String requestPath = method == HttpMethod.PATCH ? path + "/complete" : path;
            var request = request(method, requestPath);
            if (method == HttpMethod.PUT) {
                request.contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing task\",\"completed\":false}");
            }
            mvc.perform(request)
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.detail").value(
                            "Task with ID " + Long.MAX_VALUE + " was not found."))
                    .andExpect(jsonPath("$.instance").value(requestPath));
        }
    }

    @Test
    void rejectsInvalidUpdatesWithoutChangingTask() throws Exception {
        long id = createTask("Keep this title");
        for (String body : new String[]{
                "{}", "{\"title\":\"   \",\"completed\":false}",
                "{\"title\":\"" + "a".repeat(121) + "\",\"completed\":false}",
                "{\"title\":\"Valid title\"}", "{\"title\":\"Valid title\",\"completed\":null}"}) {
            mvc.perform(put("/api/tasks/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isMap());
        }
        mvc.perform(get("/api/tasks/{id}", id))
                .andExpect(jsonPath("$.title").value("Keep this title"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void explainsValidationErrorsByField() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("must not be blank"));
    }

    @Test
    void handlesMalformedJsonAndNonNumericIds() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(get("/api/tasks/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private long createTask(String title) throws Exception {
        var result = mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("title", title))))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("/api/tasks/" + id);
        assertThat(id).isPositive();
        return id;
    }
}
