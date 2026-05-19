package com.example.javachat.chat;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javachat.friend.FriendRelation;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
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
        "spring.datasource.url=jdbc:h2:mem:chat-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
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
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        privateMessageRepository.deleteAll();
        friendRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser("admin", "Admin");
        alice = createUser("alice", "Alice");
        bob = createUser("bob", "Bob");
        acceptFriendship(admin, alice);
    }

    @Test
    void friendCanSendMessageAndReadHistoryInAscendingOrder() throws Exception {
        String adminToken = tokenFor("admin");

        mockMvc.perform(post("/api/chats/private/{friendId}/messages", alice.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.senderId").value(admin.getId()))
                .andExpect(jsonPath("$.data.receiverId").value(alice.getId()))
                .andExpect(jsonPath("$.data.content").value("hello alice"))
                .andExpect(jsonPath("$.data.messageType").value("TEXT"));

        mockMvc.perform(post("/api/chats/private/{friendId}/messages", alice.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"second message\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/chats/private/{friendId}/messages", alice.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].content").value("hello alice"))
                .andExpect(jsonPath("$.data.content[1].content").value("second message"));
    }

    @Test
    void nonFriendCannotSendOrReadPrivateMessages() throws Exception {
        String adminToken = tokenFor("admin");

        mockMvc.perform(post("/api/chats/private/{friendId}/messages", bob.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello bob\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));

        mockMvc.perform(get("/api/chats/private/{friendId}/messages", bob.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));
    }

    @Test
    void unreadCountIncreasesAndMarkReadClearsIt() throws Exception {
        String adminToken = tokenFor("admin");
        String aliceToken = tokenFor("alice");

        mockMvc.perform(post("/api/chats/private/{friendId}/messages", alice.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"unread one\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/chats/private/{friendId}/messages", alice.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"unread two\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].user.username").value("admin"))
                .andExpect(jsonPath("$.data[0].unreadCount").value(2));

        mockMvc.perform(post("/api/chats/private/{friendId}/read", admin.getId())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readCount").value(2))
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].unreadCount").value(0));
    }

    @Test
    void privateMessageContentMustNotBeBlank() throws Exception {
        mockMvc.perform(post("/api/chats/private/{friendId}/messages", alice.getId())
                        .header("Authorization", "Bearer " + tokenFor("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    private User createUser(String username, String nickname) {
        User user = new User(username, passwordEncoder.encode("123456"), nickname);
        user.setAvatarUrl("/default-avatar.png");
        return userRepository.save(user);
    }

    private void acceptFriendship(User user, User friend) {
        friendRepository.save(new FriendRelation(user.getId(), friend.getId(), FriendStatus.ACCEPTED));
        friendRepository.save(new FriendRelation(friend.getId(), user.getId(), FriendStatus.ACCEPTED));
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
