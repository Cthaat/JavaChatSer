package com.example.javachat.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import com.example.javachat.chat.PrivateMessageRepository;
import com.example.javachat.chat.PublicMessageRepository;
import com.example.javachat.friend.FriendRelation;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
import com.example.javachat.security.JwtTokenProvider;
import com.example.javachat.user.User;
import com.example.javachat.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:websocket-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=true",
                "spring.flyway.enabled=false",
                "app.jwt.secret=12345678901234567890123456789012"
        }
)
class ChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TestRestTemplate restTemplate;

    private User admin;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        privateMessageRepository.deleteAll();
        publicMessageRepository.deleteAll();
        friendRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser("admin", "Admin");
        alice = createUser("alice", "Alice");
        bob = createUser("bob", "Bob");
        acceptFriendship(admin, alice);
    }

    @Test
    void websocketPushesPrivateMessagesAndFriendStatus() throws Exception {
        QueueingWebSocketHandler adminHandler = new QueueingWebSocketHandler(objectMapper);
        QueueingWebSocketHandler aliceHandler = new QueueingWebSocketHandler(objectMapper);

        WebSocketSession adminSession = connect(jwtTokenProvider.createToken(admin), adminHandler);
        WebSocketSession aliceSession = connect(jwtTokenProvider.createToken(alice), aliceHandler);

        JsonNode onlineNotice = adminHandler.awaitType(WebSocketMessageType.FRIEND_STATUS);
        assertThat(onlineNotice.at("/data/userId").asLong()).isEqualTo(alice.getId());
        assertThat(onlineNotice.at("/data/online").asBoolean()).isTrue();

        adminSession.sendMessage(new TextMessage("""
                {
                  "type": "PRIVATE_MESSAGE",
                  "receiverId": %d,
                  "content": "hello websocket"
                }
                """.formatted(alice.getId())));

        JsonNode adminMessage = adminHandler.awaitType(WebSocketMessageType.PRIVATE_MESSAGE);
        JsonNode aliceMessage = aliceHandler.awaitType(WebSocketMessageType.PRIVATE_MESSAGE);
        assertThat(adminMessage.at("/data/content").asText()).isEqualTo("hello websocket");
        assertThat(aliceMessage.at("/data/content").asText()).isEqualTo("hello websocket");
        assertThat(aliceMessage.at("/data/senderId").asLong()).isEqualTo(admin.getId());
        assertThat(aliceMessage.at("/data/receiverId").asLong()).isEqualTo(alice.getId());
        assertThat(privateMessageRepository.count()).isEqualTo(1);

        adminSession.close();
        JsonNode offlineNotice = aliceHandler.awaitType(WebSocketMessageType.FRIEND_STATUS);
        assertThat(offlineNotice.at("/data/userId").asLong()).isEqualTo(admin.getId());
        assertThat(offlineNotice.at("/data/online").asBoolean()).isFalse();

        aliceSession.close();
    }

    @Test
    void websocketBroadcastsPublicMessagesAndRespondsToPing() throws Exception {
        QueueingWebSocketHandler adminHandler = new QueueingWebSocketHandler(objectMapper);
        QueueingWebSocketHandler aliceHandler = new QueueingWebSocketHandler(objectMapper);

        WebSocketSession adminSession = connect(jwtTokenProvider.createToken(admin), adminHandler);
        WebSocketSession aliceSession = connect(jwtTokenProvider.createToken(alice), aliceHandler);

        adminSession.sendMessage(new TextMessage("""
                {
                  "type": "PUBLIC_MESSAGE",
                  "content": "hello public room"
                }
                """));

        JsonNode adminMessage = adminHandler.awaitType(WebSocketMessageType.PUBLIC_MESSAGE);
        JsonNode aliceMessage = aliceHandler.awaitType(WebSocketMessageType.PUBLIC_MESSAGE);
        assertThat(adminMessage.at("/data/content").asText()).isEqualTo("hello public room");
        assertThat(aliceMessage.at("/data/content").asText()).isEqualTo("hello public room");
        assertThat(publicMessageRepository.count()).isEqualTo(1);

        aliceSession.sendMessage(new TextMessage("{\"type\":\"PING\"}"));
        assertThat(aliceHandler.awaitType(WebSocketMessageType.PONG).path("data").isNull()).isTrue();

        adminSession.close();
        aliceSession.close();
    }

    @Test
    void restPublicMessageBroadcastsToOnlineWebSocketUsers() throws Exception {
        QueueingWebSocketHandler adminHandler = new QueueingWebSocketHandler(objectMapper);
        QueueingWebSocketHandler aliceHandler = new QueueingWebSocketHandler(objectMapper);

        String adminToken = jwtTokenProvider.createToken(admin);
        WebSocketSession adminSession = connect(adminToken, adminHandler);
        WebSocketSession aliceSession = connect(jwtTokenProvider.createToken(alice), aliceHandler);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/chats/public/messages",
                new HttpEntity<>("{\"content\":\"rest public room\"}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody()).at("/data/content").asText())
                .isEqualTo("rest public room");
        assertThat(adminHandler.awaitType(WebSocketMessageType.PUBLIC_MESSAGE).at("/data/content").asText())
                .isEqualTo("rest public room");
        assertThat(aliceHandler.awaitType(WebSocketMessageType.PUBLIC_MESSAGE).at("/data/content").asText())
                .isEqualTo("rest public room");
        assertThat(publicMessageRepository.count()).isEqualTo(1);

        adminSession.close();
        aliceSession.close();
    }

    @Test
    void restFriendRequestNotifiesOnlineReceiver() throws Exception {
        QueueingWebSocketHandler bobHandler = new QueueingWebSocketHandler(objectMapper);

        WebSocketSession bobSession = connect(jwtTokenProvider.createToken(bob), bobHandler);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenProvider.createToken(admin));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/friends/requests",
                new HttpEntity<>("{\"friendId\":" + bob.getId() + "}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode notification = bobHandler.awaitType(WebSocketMessageType.FRIEND_REQUEST);
        assertThat(notification.at("/data/status").asText()).isEqualTo("PENDING");
        assertThat(notification.at("/data/requester/username").asText()).isEqualTo("admin");

        bobSession.close();
    }

    @Test
    void websocketHandshakeRequiresJwtToken() {
        QueueingWebSocketHandler handler = new QueueingWebSocketHandler(objectMapper);

        assertThatThrownBy(() -> connect(null, handler))
                .isInstanceOf(Exception.class);
    }

    private WebSocketSession connect(String token, QueueingWebSocketHandler handler) throws Exception {
        String query = token == null ? "" : "?token=" + token;
        StandardWebSocketClient client = new StandardWebSocketClient();
        return client.execute(
                        handler,
                        new WebSocketHttpHeaders(),
                        URI.create("ws://localhost:" + port + "/ws/chat" + query)
                )
                .get(5, TimeUnit.SECONDS);
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

    private static final class QueueingWebSocketHandler extends TextWebSocketHandler {

        private final ObjectMapper objectMapper;
        private final BlockingQueue<JsonNode> messages = new LinkedBlockingQueue<>();

        private QueueingWebSocketHandler(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            messages.add(objectMapper.readTree(message.getPayload()));
        }

        private JsonNode awaitType(WebSocketMessageType type) throws InterruptedException {
            List<JsonNode> received = new ArrayList<>();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                JsonNode message = messages.poll(200, TimeUnit.MILLISECONDS);
                if (message == null) {
                    continue;
                }
                received.add(message);
                if (type.name().equals(message.path("type").asText())) {
                    return message;
                }
            }
            fail("Expected WebSocket message type %s, received %s", type, received);
            return null;
        }
    }
}
