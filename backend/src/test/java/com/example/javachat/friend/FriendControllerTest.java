package com.example.javachat.friend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javachat.user.User;
import com.example.javachat.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:friend-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=true",
        "spring.flyway.enabled=false",
        "app.jwt.secret=12345678901234567890123456789012"
})
@AutoConfigureMockMvc
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User alice;

    @BeforeEach
    void setUp() {
        friendRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser("admin", "Admin");
        alice = createUser("alice", "Alice");
        createUser("bob", "Bob");
    }

    @Test
    void searchUsersExcludesCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "a")
                        .header("Authorization", "Bearer " + tokenFor("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content[*].username").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("admin"))))
                .andExpect(jsonPath("$.data.content[*].username").value(org.hamcrest.Matchers.hasItem("alice")));
    }

    @Test
    void friendRequestCanBeAcceptedAndDeletedBidirectionally() throws Exception {
        String adminToken = tokenFor("admin");
        String aliceToken = tokenFor("alice");

        MvcResult requestResult = mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendId\":" + alice.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.requester.username").value("admin"))
                .andReturn();
        Long requestId = objectMapper
                .readTree(requestResult.getResponse().getContentAsString())
                .at("/data/requestId")
                .asLong();

        mockMvc.perform(get("/api/friends/requests")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].requestId").value(requestId));

        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        assertThat(friendRepository.existsByUserIdAndFriendIdAndStatus(
                admin.getId(),
                alice.getId(),
                FriendStatus.ACCEPTED
        )).isTrue();
        assertThat(friendRepository.existsByUserIdAndFriendIdAndStatus(
                alice.getId(),
                admin.getId(),
                FriendStatus.ACCEPTED
        )).isTrue();

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].user.username").value("alice"))
                .andExpect(jsonPath("$.data[0].online").value(false))
                .andExpect(jsonPath("$.data[0].unreadCount").value(0));

        mockMvc.perform(delete("/api/friends/{friendId}", alice.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void friendRequestRejectsSelfAndDuplicateRequests() throws Exception {
        String adminToken = tokenFor("admin");

        mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendId\":" + admin.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));

        mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendId\":" + alice.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendId\":" + alice.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void friendRequestCanBeRejected() throws Exception {
        MvcResult requestResult = mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", "Bearer " + tokenFor("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendId\":" + alice.getId() + "}"))
                .andExpect(status().isOk())
                .andReturn();
        Long requestId = objectMapper
                .readTree(requestResult.getResponse().getContentAsString())
                .at("/data/requestId")
                .asLong();

        mockMvc.perform(post("/api/friends/requests/{requestId}/reject", requestId)
                        .header("Authorization", "Bearer " + tokenFor("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + tokenFor("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    private User createUser(String username, String nickname) {
        User user = new User(username, passwordEncoder.encode("123456"), nickname);
        user.setAvatarUrl("/default-avatar.png");
        return userRepository.save(user);
    }

    private String tokenFor(String username) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "123456"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", not(blankOrNullString())))
                .andReturn();
        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return body.at("/data/token").asText();
    }
}
