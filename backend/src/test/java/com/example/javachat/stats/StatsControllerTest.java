package com.example.javachat.stats;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javachat.chat.PrivateMessage;
import com.example.javachat.chat.PrivateMessageRepository;
import com.example.javachat.chat.PublicMessage;
import com.example.javachat.chat.PublicMessageRepository;
import com.example.javachat.friend.FriendRelation;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
import com.example.javachat.user.User;
import com.example.javachat.user.UserRepository;
import com.example.javachat.user.UserRole;
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
        "spring.datasource.url=jdbc:h2:mem:stats-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
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
class StatsControllerTest {

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
    private PublicMessageRepository publicMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User alice;

    @BeforeEach
    void setUp() {
        privateMessageRepository.deleteAll();
        publicMessageRepository.deleteAll();
        friendRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser("admin", "Admin", UserRole.ADMIN);
        alice = createUser("alice", "Alice", UserRole.USER);
        createUser("bob", "Bob", UserRole.USER);
        acceptFriendship(admin, alice);

        privateMessageRepository.save(new PrivateMessage(admin.getId(), alice.getId(), "hello alice"));
        privateMessageRepository.save(new PrivateMessage(alice.getId(), admin.getId(), "hello admin"));
        publicMessageRepository.save(new PublicMessage(admin.getId(), "global hello"));
        publicMessageRepository.save(new PublicMessage(alice.getId(), "public hello"));
    }

    @Test
    void adminCanReadGlobalOverview() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                        .header("Authorization", "Bearer " + tokenFor("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.scope").value("GLOBAL"))
                .andExpect(jsonPath("$.data.registeredUserCount").value(3))
                .andExpect(jsonPath("$.data.todayPrivateMessageCount").value(2))
                .andExpect(jsonPath("$.data.todayPublicMessageCount").value(2))
                .andExpect(jsonPath("$.data.totalPrivateMessageCount").value(2))
                .andExpect(jsonPath("$.data.totalPublicMessageCount").value(2))
                .andExpect(jsonPath("$.data.acceptedFriendCount").value(1));
    }

    @Test
    void normalUserReadsPersonalOverview() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                        .header("Authorization", "Bearer " + tokenFor("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("PERSONAL"))
                .andExpect(jsonPath("$.data.registeredUserCount").value(1))
                .andExpect(jsonPath("$.data.todayPrivateMessageCount").value(2))
                .andExpect(jsonPath("$.data.todayPublicMessageCount").value(1))
                .andExpect(jsonPath("$.data.totalPrivateMessageCount").value(2))
                .andExpect(jsonPath("$.data.totalPublicMessageCount").value(1))
                .andExpect(jsonPath("$.data.acceptedFriendCount").value(1));
    }

    @Test
    void overviewRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    private User createUser(String username, String nickname, UserRole role) {
        User user = new User(username, passwordEncoder.encode("123456"), nickname);
        user.setAvatarUrl("/default-avatar.png");
        user.setRole(role);
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
