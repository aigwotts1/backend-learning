package com.aigwotts1.tasktracker.notification;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class NotificationClientTest {
    @Test
    void postsTheCompletionContract() {
        var builder = RestClient.builder().baseUrl("http://notifications:8081");
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new NotificationClient(builder.build());
        server.expect(requestTo("http://notifications:8081/api/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"taskId\":42,\"title\":\"Learn HTTP\"}"))
                .andRespond(withStatus(HttpStatus.CREATED));
        client.send(new TaskCompletedEvent(42, "Learn HTTP"));
        server.verify();
    }

    @Test
    void propagatesHttpFailuresToTheListener() {
        var builder = RestClient.builder().baseUrl("http://notifications:8081");
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new NotificationClient(builder.build());
        server.expect(requestTo("http://notifications:8081/api/notifications"))
                .andRespond(withServerError());
        assertThatThrownBy(() -> client.send(new TaskCompletedEvent(42, "Learn HTTP")))
                .isInstanceOf(RestClientException.class);
        server.verify();
    }
}
